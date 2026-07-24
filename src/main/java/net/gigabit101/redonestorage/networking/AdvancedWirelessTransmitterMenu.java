package net.gigabit101.redonestorage.networking;

import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;
import net.gigabit101.redonestorage.content.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AdvancedWirelessTransmitterMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOTS = 4;

    private final BlockPos pos;
    private final UpgradeContainer upgrades;
    private final DataSlot range;
    private final DataSlot active;

    public AdvancedWirelessTransmitterMenu(final int containerId,
                                           final Inventory inventory,
                                           final BlockPos pos) {
        this(containerId, inventory,
            inventory.player.level().getBlockEntity(pos) instanceof AdvancedWirelessTransmitterBlockEntity transmitter
                ? transmitter : null,
            pos);
    }

    public AdvancedWirelessTransmitterMenu(final int containerId,
                                           final Inventory inventory,
                                           final AdvancedWirelessTransmitterBlockEntity transmitter) {
        this(containerId, inventory, transmitter, transmitter.getBlockPos());
    }

    private AdvancedWirelessTransmitterMenu(final int containerId,
                                            final Inventory inventory,
                                            final AdvancedWirelessTransmitterBlockEntity transmitter,
                                            final BlockPos pos) {
        super(ModContent.ADVANCED_WIRELESS_TRANSMITTER_MENU.get(), containerId);
        this.pos = pos.immutable();
        this.upgrades = transmitter != null
            ? transmitter.getUpgradeContainer()
            : new UpgradeContainer(UpgradeDestinations.WIRELESS_TRANSMITTER, UPGRADE_SLOTS);
        if (transmitter == null) {
            this.range = DataSlot.standalone();
            this.active = DataSlot.standalone();
        } else {
            this.range = new DataSlot() {
                @Override
                public int get() {
                    return transmitter.getRange();
                }

                @Override
                public void set(final int value) {
                }
            };
            this.active = new DataSlot() {
                @Override
                public int get() {
                    return transmitter.isActive() ? 1 : 0;
                }

                @Override
                public void set(final int value) {
                }
            };
        }
        addDataSlot(range);
        addDataSlot(active);

        for (int i = 0; i < UPGRADE_SLOTS; ++i) {
            addSlot(new UpgradeSlot(upgrades, i, 187, 18 + i * 18));
        }
        addPlayerInventory(inventory, 8, 104);
    }

    public int getRange() {
        return range.get();
    }

    public boolean isActive() {
        return active.get() != 0;
    }

    @Override
    public boolean stillValid(final Player player) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D
            && player.level().getBlockEntity(pos) instanceof AdvancedWirelessTransmitterBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        final Slot slot = slots.get(index);
        final ItemStack source = slot.getItem();
        final ItemStack copy = source.copy();
        if (index < UPGRADE_SLOTS) {
            if (!moveItemStackTo(source, UPGRADE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, 0, UPGRADE_SLOTS, false)) {
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
}
