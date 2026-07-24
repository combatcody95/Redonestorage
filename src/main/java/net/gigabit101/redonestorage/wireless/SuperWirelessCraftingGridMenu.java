package net.gigabit101.redonestorage.wireless;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.grid.AbstractCraftingGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.CraftingGrid;
import com.refinedmods.refinedstorage.common.grid.WirelessGridData;
import net.gigabit101.redonestorage.content.ModContent;
import net.minecraft.world.entity.player.Inventory;

public final class SuperWirelessCraftingGridMenu extends AbstractCraftingGridContainerMenu {
    public SuperWirelessCraftingGridMenu(final int containerId,
                                         final Inventory inventory,
                                         final WirelessGridData data) {
        super(ModContent.SUPER_WIRELESS_CRAFTING_GRID_MENU.get(), containerId, inventory, data.gridData());
        this.disabledSlot = data.slotReference();
        resized(0, 0, 0);
    }

    SuperWirelessCraftingGridMenu(final int containerId,
                                  final Inventory inventory,
                                  final CraftingGrid grid,
                                  final SlotReference slotReference) {
        super(ModContent.SUPER_WIRELESS_CRAFTING_GRID_MENU.get(), containerId, inventory, grid);
        this.disabledSlot = slotReference;
        resized(0, 0, 0);
    }
}
