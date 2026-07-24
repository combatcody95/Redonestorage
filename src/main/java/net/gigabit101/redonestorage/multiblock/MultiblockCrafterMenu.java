package net.gigabit101.redonestorage.multiblock;

import com.refinedmods.refinedstorage.common.autocrafting.PatternSlot;
import net.gigabit101.redonestorage.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public final class MultiblockCrafterMenu extends AbstractContainerMenu {
    public static final int PATTERN_COLUMNS = 13;
    public static final int PATTERN_ROWS = 6;
    private static final int PATTERN_SLOTS = PATTERN_COLUMNS * PATTERN_ROWS;

    private final Inventory playerInventory;
    private final BlockPos anchorPos;
    private final PagedPatternContainer patternContainer;
    private final DataSlot currentPage = DataSlot.standalone();

    public MultiblockCrafterMenu(final int containerId,
                                 final Inventory playerInventory,
                                 final BlockPos anchorPos) {
        this(containerId, playerInventory, findBlockEntity(playerInventory.player.level(), anchorPos), anchorPos);
    }

    public MultiblockCrafterMenu(final int containerId,
                                 final Inventory playerInventory,
                                 final MultiblockCrafterBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    private MultiblockCrafterMenu(final int containerId,
                                  final Inventory playerInventory,
                                  final MultiblockCrafterBlockEntity blockEntity,
                                  final BlockPos anchorPos) {
        super(ModContent.MULTIBLOCK_CRAFTER_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.anchorPos = anchorPos.immutable();
        this.patternContainer = new PagedPatternContainer(playerInventory.player.level(), blockEntity);
        currentPage.set(patternContainer.getInitialPage());
        patternContainer.setPage(currentPage.get());
        addDataSlot(currentPage);

        for (int row = 0; row < PATTERN_ROWS; ++row) {
            for (int column = 0; column < PATTERN_COLUMNS; ++column) {
                final int slot = row * PATTERN_COLUMNS + column;
                addSlot(new PatternSlot(patternContainer, slot, 9 + column * 18, 21 + row * 18,
                    playerInventory.player.level()));
            }
        }
        addPlayerInventory(playerInventory, 45, 141);
    }

    private static MultiblockCrafterBlockEntity findBlockEntity(final Level level, final BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MultiblockCrafterBlockEntity crafter ? crafter : null;
    }

    public int getPage() {
        return currentPage.get();
    }

    public int getPageCount() {
        return Math.max(1, patternContainer.getPageCount());
    }

    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (id < 0 || id > 3) {
            return false;
        }
        final int pageCount = getPageCount();
        final int delta = switch (id) {
            case 0 -> -1;
            case 1 -> 1;
            case 2 -> -10;
            case 3 -> 10;
            default -> 0;
        };
        final int next = Math.max(0, Math.min(pageCount - 1, currentPage.get() + delta));
        if (next != currentPage.get()) {
            currentPage.set(next);
            patternContainer.setPage(next);
            broadcastFullState();
        }
        return true;
    }

    @Override
    public void broadcastChanges() {
        patternContainer.setPage(currentPage.get());
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(final Player player) {
        if (!(player.level().getBlockEntity(anchorPos) instanceof MultiblockCrafterBlockEntity crafter)) {
            return false;
        }
        return player.distanceToSqr(anchorPos.getX() + 0.5D, anchorPos.getY() + 0.5D, anchorPos.getZ() + 0.5D)
            <= 64.0D && crafter.getStructure().valid();
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        final Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack source = slot.getItem();
        final ItemStack copy = source.copy();
        if (index < PATTERN_SLOTS) {
            if (!moveItemStackTo(source, PATTERN_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, 0, PATTERN_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private void addPlayerInventory(final Inventory inventory, final int xOffset, final int yOffset) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, 9 + row * 9 + column,
                    xOffset + column * 18, yOffset + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, xOffset + column * 18, yOffset + 58));
        }
    }

    private static final class PagedPatternContainer implements Container {
        private final Level level;
        private final MultiblockCrafterBlockEntity anchor;
        private final SimpleContainer fallback = new SimpleContainer(PATTERN_SLOTS);
        private int page;

        private PagedPatternContainer(final Level level, final MultiblockCrafterBlockEntity anchor) {
            this.level = level;
            this.anchor = anchor;
        }

        private int getInitialPage() {
            if (anchor == null) {
                return 0;
            }
            final List<BlockPos> storages = anchor.getStructure().storages();
            final int index = storages.indexOf(anchor.getBlockPos());
            return Math.max(0, index);
        }

        private int getPageCount() {
            return anchor == null ? 1 : Math.max(1, anchor.getStructure().storages().size());
        }

        private void setPage(final int page) {
            this.page = Math.floorMod(page, getPageCount());
        }

        private Container delegate() {
            if (anchor == null) {
                return fallback;
            }
            final List<BlockPos> storages = anchor.getStructure().storages();
            if (storages.isEmpty()) {
                return fallback;
            }
            final BlockPos storagePos = storages.get(Math.floorMod(page, storages.size()));
            if (level.getBlockEntity(storagePos) instanceof MultiblockCrafterBlockEntity storage
                && storage.isPatternStorage()) {
                return storage.getPatterns();
            }
            return fallback;
        }

        @Override
        public int getContainerSize() {
            return PATTERN_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            return delegate().isEmpty();
        }

        @Override
        public ItemStack getItem(final int slot) {
            return delegate().getItem(slot);
        }

        @Override
        public ItemStack removeItem(final int slot, final int amount) {
            return delegate().removeItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(final int slot) {
            return delegate().removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(final int slot, final ItemStack stack) {
            delegate().setItem(slot, stack);
        }

        @Override
        public void setChanged() {
            delegate().setChanged();
        }

        @Override
        public boolean stillValid(final Player player) {
            return anchor != null && !anchor.isRemoved();
        }

        @Override
        public void clearContent() {
            delegate().clearContent();
        }
    }
}
