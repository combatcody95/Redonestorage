package net.gigabit101.redonestorage.content;

import net.gigabit101.redonestorage.Constants;
import net.gigabit101.redonestorage.multiblock.MultiblockCrafterBlock;
import net.gigabit101.redonestorage.multiblock.MultiblockCrafterBlockEntity;
import net.gigabit101.redonestorage.multiblock.MultiblockCrafterMenu;
import net.gigabit101.redonestorage.networking.AdvancedWirelessTransmitterBlock;
import net.gigabit101.redonestorage.networking.AdvancedWirelessTransmitterBlockEntity;
import net.gigabit101.redonestorage.networking.AdvancedWirelessTransmitterMenu;
import net.gigabit101.redonestorage.storage.RedoneStorageDiskItem;
import net.gigabit101.redonestorage.wireless.SuperWirelessGridItem;
import net.gigabit101.redonestorage.wireless.SuperWirelessCraftingGridMenu;
import com.refinedmods.refinedstorage.common.grid.WirelessGridData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModContent {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(BuiltInRegistries.MENU, Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    private static final BlockBehaviour.Properties MULTIBLOCK_PROPERTIES =
        BlockBehaviour.Properties.of().strength(2.0F).requiresCorrectToolForDrops();

    public static final DeferredHolder<Block, MultiblockCrafterBlock> MULTIBLOCK_FRAME =
        BLOCKS.register("multiblock_frame", () -> new MultiblockCrafterBlock(MultiblockCrafterBlock.PartType.FRAME,
            MULTIBLOCK_PROPERTIES));
    public static final DeferredHolder<Block, MultiblockCrafterBlock> MULTIBLOCK_HEAT =
        BLOCKS.register("multiblock_heat", () -> new MultiblockCrafterBlock(MultiblockCrafterBlock.PartType.HEAT,
            MULTIBLOCK_PROPERTIES));
    public static final DeferredHolder<Block, MultiblockCrafterBlock> MULTIBLOCK_CPU =
        BLOCKS.register("multiblock_cpu", () -> new MultiblockCrafterBlock(MultiblockCrafterBlock.PartType.CPU,
            MULTIBLOCK_PROPERTIES));
    public static final DeferredHolder<Block, MultiblockCrafterBlock> MULTIBLOCK_STORAGE =
        BLOCKS.register("multiblock_storage", () -> new MultiblockCrafterBlock(MultiblockCrafterBlock.PartType.STORAGE,
            MULTIBLOCK_PROPERTIES));
    public static final DeferredHolder<Block, AdvancedWirelessTransmitterBlock> ADVANCED_WIRELESS_TRANSMITTER =
        BLOCKS.register("advanced_wireless_transmitter", AdvancedWirelessTransmitterBlock::new);

    public static final DeferredHolder<Item, Item> SMALL_ITEM_DISK_PART = part("small_item_disk_part");
    public static final DeferredHolder<Item, Item> MEDIUM_ITEM_DISK_PART = part("medium_item_disk_part");
    public static final DeferredHolder<Item, Item> LARGE_ITEM_DISK_PART = part("large_item_disk_part");
    public static final DeferredHolder<Item, Item> LARGER_ITEM_DISK_PART = part("larger_item_disk_part");
    public static final DeferredHolder<Item, Item> SMALL_FLUID_DISK_PART = part("small_fluid_disk_part");
    public static final DeferredHolder<Item, Item> MEDIUM_FLUID_DISK_PART = part("medium_fluid_disk_part");
    public static final DeferredHolder<Item, Item> LARGE_FLUID_DISK_PART = part("large_fluid_disk_part");
    public static final DeferredHolder<Item, Item> LARGER_FLUID_DISK_PART = part("larger_fluid_disk_part");

    public static final DeferredHolder<Item, RedoneStorageDiskItem> SMALL_ITEM_DISK = disk(
        "small_item_disk", 256_000L, RedoneStorageDiskItem.Kind.ITEM, SMALL_ITEM_DISK_PART);
    public static final DeferredHolder<Item, RedoneStorageDiskItem> MEDIUM_ITEM_DISK = disk(
        "medium_item_disk", 1_024_000L, RedoneStorageDiskItem.Kind.ITEM, MEDIUM_ITEM_DISK_PART);
    public static final DeferredHolder<Item, RedoneStorageDiskItem> LARGE_ITEM_DISK = disk(
        "large_item_disk", 4_096_000L, RedoneStorageDiskItem.Kind.ITEM, LARGE_ITEM_DISK_PART);
    public static final DeferredHolder<Item, RedoneStorageDiskItem> LARGER_ITEM_DISK = disk(
        "larger_item_disk", 16_384_000L, RedoneStorageDiskItem.Kind.ITEM, LARGER_ITEM_DISK_PART);

    public static final DeferredHolder<Item, RedoneStorageDiskItem> SMALL_FLUID_DISK = disk(
        "small_fluid_disk", 16_384_000L, RedoneStorageDiskItem.Kind.FLUID, SMALL_FLUID_DISK_PART);
    public static final DeferredHolder<Item, RedoneStorageDiskItem> MEDIUM_FLUID_DISK = disk(
        "medium_fluid_disk", 65_536_000L, RedoneStorageDiskItem.Kind.FLUID, MEDIUM_FLUID_DISK_PART);
    public static final DeferredHolder<Item, RedoneStorageDiskItem> LARGE_FLUID_DISK = disk(
        "large_fluid_disk", 262_144_000L, RedoneStorageDiskItem.Kind.FLUID, LARGE_FLUID_DISK_PART);
    public static final DeferredHolder<Item, RedoneStorageDiskItem> LARGER_FLUID_DISK = disk(
        "larger_fluid_disk", 1_048_576_000L, RedoneStorageDiskItem.Kind.FLUID, LARGER_FLUID_DISK_PART);

    public static final DeferredHolder<Item, Item> RAW_SUPER_ADVANCED_PROCESSOR = part("raw_super_advanced_processor");
    public static final DeferredHolder<Item, Item> SUPER_ADVANCED_PROCESSOR = part("super_advanced_processor");

    public static final DeferredHolder<Item, SuperWirelessGridItem> SUPER_WIRELESS_GRID = ITEMS.register(
        "super_wireless_crafting_grid",
        () -> new SuperWirelessGridItem(false)
    );
    public static final DeferredHolder<Item, SuperWirelessGridItem> CREATIVE_SUPER_WIRELESS_GRID = ITEMS.register(
        "creative_super_wireless_crafting_grid",
        () -> new SuperWirelessGridItem(true)
    );

    public static final DeferredHolder<Item, BlockItem> MULTIBLOCK_FRAME_ITEM = blockItem("multiblock_frame", MULTIBLOCK_FRAME);
    public static final DeferredHolder<Item, BlockItem> MULTIBLOCK_HEAT_ITEM = blockItem("multiblock_heat", MULTIBLOCK_HEAT);
    public static final DeferredHolder<Item, BlockItem> MULTIBLOCK_CPU_ITEM = blockItem("multiblock_cpu", MULTIBLOCK_CPU);
    public static final DeferredHolder<Item, BlockItem> MULTIBLOCK_STORAGE_ITEM = blockItem("multiblock_storage", MULTIBLOCK_STORAGE);
    public static final DeferredHolder<Item, BlockItem> ADVANCED_WIRELESS_TRANSMITTER_ITEM =
        blockItem("advanced_wireless_transmitter", ADVANCED_WIRELESS_TRANSMITTER);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockCrafterBlockEntity>>
        MULTIBLOCK_CRAFTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("multiblock_crafter", () ->
            BlockEntityType.Builder.of(
                MultiblockCrafterBlockEntity::new,
                MULTIBLOCK_FRAME.get(), MULTIBLOCK_HEAT.get(), MULTIBLOCK_CPU.get(), MULTIBLOCK_STORAGE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedWirelessTransmitterBlockEntity>>
        ADVANCED_WIRELESS_TRANSMITTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("advanced_wireless_transmitter", () ->
            BlockEntityType.Builder.of(AdvancedWirelessTransmitterBlockEntity::new,
                ADVANCED_WIRELESS_TRANSMITTER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<MultiblockCrafterMenu>> MULTIBLOCK_CRAFTER_MENU =
        MENUS.register("multiblock_crafter", () -> IMenuTypeExtension.create(
            (id, inventory, buffer) -> new MultiblockCrafterMenu(id, inventory, buffer.readBlockPos())
        ));
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedWirelessTransmitterMenu>>
        ADVANCED_WIRELESS_TRANSMITTER_MENU = MENUS.register("advanced_wireless_transmitter", () ->
            IMenuTypeExtension.create((id, inventory, buffer) ->
                new AdvancedWirelessTransmitterMenu(id, inventory, buffer.readBlockPos())));
    public static final DeferredHolder<MenuType<?>, MenuType<SuperWirelessCraftingGridMenu>>
        SUPER_WIRELESS_CRAFTING_GRID_MENU = MENUS.register("super_wireless_crafting_grid", () ->
            IMenuTypeExtension.create((id, inventory, buffer) -> new SuperWirelessCraftingGridMenu(
                id,
                inventory,
                WirelessGridData.STREAM_CODEC.decode(buffer)
            )));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register(
        "creative_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.redonestorage"))
            .icon(() -> new ItemStack(MULTIBLOCK_STORAGE_ITEM.get()))
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(holder -> output.accept(holder.get())))
            .build());

    public static final List<DeferredHolder<Block, MultiblockCrafterBlock>> MULTIBLOCK_BLOCKS = List.of(
        MULTIBLOCK_FRAME, MULTIBLOCK_HEAT, MULTIBLOCK_CPU, MULTIBLOCK_STORAGE
    );

    private ModContent() {
    }

    private static DeferredHolder<Item, Item> part(final String id) {
        return ITEMS.register(id, () -> new Item(new Item.Properties()));
    }

    private static DeferredHolder<Item, RedoneStorageDiskItem> disk(
        final String id,
        final long capacity,
        final RedoneStorageDiskItem.Kind kind,
        final DeferredHolder<Item, ? extends Item> part
    ) {
        return ITEMS.register(id, () -> new RedoneStorageDiskItem(capacity, kind, part));
    }

    private static <T extends Block> DeferredHolder<Item, BlockItem> blockItem(
        final String id,
        final DeferredHolder<Block, T> block
    ) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(final IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
    }

    public static boolean isMultiblockPart(final Block block) {
        return block instanceof MultiblockCrafterBlock;
    }
}
