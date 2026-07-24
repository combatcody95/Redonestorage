package net.gigabit101.redonestorage.networking;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.network.SimpleConnectionStrategy;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;
import net.gigabit101.redonestorage.RedoneStorageConfig;
import net.gigabit101.redonestorage.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class AdvancedWirelessTransmitterBlockEntity
    extends AbstractNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements MenuProvider, NetworkItemTargetBlockEntity {
    private static final String TAG_UPGRADES = "upgrades";

    private final UpgradeContainer upgradeContainer;

    public AdvancedWirelessTransmitterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            ModContent.ADVANCED_WIRELESS_TRANSMITTER_BLOCK_ENTITY.get(),
            pos,
            state,
            new SimpleNetworkNode(RedoneStorageConfig.TRANSMITTER_ENERGY_USAGE.get())
        );
        this.upgradeContainer = new UpgradeContainer(4, UpgradeDestinations.WIRELESS_TRANSMITTER,
            (container, upgradeEnergyUsage) -> {
                mainNetworkNode.setEnergyUsage(RedoneStorageConfig.TRANSMITTER_ENERGY_USAGE.get()
                    + upgradeEnergyUsage);
                setChanged();
            });
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final SimpleNetworkNode networkNode) {
        return new AdvancedWirelessTransmitterNetworkNodeContainer(
            this,
            networkNode,
            new SimpleConnectionStrategy(getBlockPos())
        );
    }

    public static void serverTick(final Level level,
                                  final BlockPos pos,
                                  final BlockState state,
                                  final AdvancedWirelessTransmitterBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        final Network network = blockEntity.mainNetworkNode.getNetwork();
        final long usage = RedoneStorageConfig.TRANSMITTER_ENERGY_USAGE.get()
            + blockEntity.upgradeContainer.getEnergyUsage();
        blockEntity.mainNetworkNode.setEnergyUsage(usage);
        boolean active = network != null;
        if (active && RefinedStorageApi.INSTANCE.isEnergyRequired()) {
            active = network.getComponent(EnergyNetworkComponent.class).getStored() >= usage;
        }
        blockEntity.mainNetworkNode.setActive(active);
        if (active) {
            blockEntity.mainNetworkNode.doWork();
        }
    }

    public UpgradeContainer getUpgradeContainer() {
        return upgradeContainer;
    }

    public int getRange() {
        final int upgrades = upgradeContainer.getAmount(Items.INSTANCE.getRangeUpgrade());
        final long range = (long) RedoneStorageConfig.TRANSMITTER_RANGE.get()
            + (long) upgrades * RedoneStorageConfig.TRANSMITTER_RANGE_PER_UPGRADE.get();
        return (int) Math.min(Integer.MAX_VALUE, range);
    }

    public boolean isActive() {
        return mainNetworkNode.isActive();
    }

    @Nullable
    @Override
    public Network getNetworkForItem() {
        return mainNetworkNode.isActive() ? mainNetworkNode.getNetwork() : null;
    }

    public void dropUpgrades() {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, worldPosition, upgradeContainer);
            upgradeContainer.clearContent();
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_UPGRADES, ContainerUtil.write(upgradeContainer, provider));
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), upgradeContainer, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.redonestorage.advanced_wireless_transmitter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory inventory, final Player player) {
        return new AdvancedWirelessTransmitterMenu(containerId, inventory, this);
    }
}
