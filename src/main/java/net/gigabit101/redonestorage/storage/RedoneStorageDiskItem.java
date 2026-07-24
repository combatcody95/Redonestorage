package net.gigabit101.redonestorage.storage;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.AbstractStorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.content.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Supplier;

public final class RedoneStorageDiskItem extends AbstractStorageContainerItem {
    public enum Kind {
        ITEM,
        FLUID
    }

    private final long capacity;
    private final Kind kind;
    private final Supplier<? extends Item> storagePart;

    public RedoneStorageDiskItem(final long capacity,
                                 final Kind kind,
                                 final Supplier<? extends Item> storagePart) {
        super(
            new Item.Properties().stacksTo(1).fireResistant(),
            RefinedStorageApi.INSTANCE.getStorageContainerItemHelper()
        );
        this.capacity = capacity;
        this.kind = kind;
        this.storagePart = storagePart;
    }

    @Override
    protected Long getCapacity() {
        return capacity;
    }

    @Override
    protected String formatAmount(final long amount) {
        final String formatted = NumberFormat.getIntegerInstance(Locale.US).format(amount);
        return kind == Kind.FLUID ? formatted + " mB" : formatted;
    }

    @Override
    protected SerializableStorage createStorage(final StorageRepository storageRepository) {
        return (kind == Kind.ITEM
            ? RefinedStorageApi.INSTANCE.getItemStorageType()
            : RefinedStorageApi.INSTANCE.getFluidStorageType())
            .create(capacity, storageRepository::markAsChanged);
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Items.INSTANCE.getStorageHousing(), count);
    }

    @Nullable
    @Override
    protected ItemStack createSecondaryDisassemblyByproduct(final int count) {
        return new ItemStack(storagePart.get(), count);
    }

    @Override
    public int getEntityLifespan(final ItemStack stack, final net.minecraft.world.level.Level level) {
        return Integer.MAX_VALUE;
    }
}
