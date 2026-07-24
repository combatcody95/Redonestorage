package net.gigabit101.redonestorage.client;

import net.gigabit101.redonestorage.networking.AdvancedWirelessTransmitterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AdvancedWirelessTransmitterScreen
    extends AbstractContainerScreen<AdvancedWirelessTransmitterMenu> {
    public AdvancedWirelessTransmitterScreen(final AdvancedWirelessTransmitterMenu menu,
                                             final Inventory inventory,
                                             final Component title) {
        super(menu, inventory, title);
        imageWidth = 214;
        imageHeight = 186;
        inventoryLabelY = 92;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF202020);
        graphics.fill(leftPos + 4, topPos + 16, leftPos + imageWidth - 4, topPos + 98, 0xFF8B8B8B);
        graphics.fill(leftPos + 4, topPos + 100, leftPos + 170, topPos + imageHeight - 4, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("gui.redonestorage.range", menu.getRange()),
            8, 28, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(menu.isActive()
            ? "gui.redonestorage.connected" : "gui.redonestorage.disconnected"), 8, 42, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFF, false);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
