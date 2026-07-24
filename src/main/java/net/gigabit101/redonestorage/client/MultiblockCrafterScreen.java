package net.gigabit101.redonestorage.client;

import net.gigabit101.redonestorage.Constants;
import net.gigabit101.redonestorage.multiblock.MultiblockCrafterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MultiblockCrafterScreen extends AbstractContainerScreen<MultiblockCrafterMenu> {
    private static final ResourceLocation GUI_SHEET = Constants.id("textures/gui/gui_sheet.png");

    private Button previousPageButton;
    private Button nextPageButton;

    public MultiblockCrafterScreen(final MultiblockCrafterMenu menu,
                                   final Inventory inventory,
                                   final Component title) {
        super(menu, inventory, title);
        imageWidth = 250;
        imageHeight = 240;
        inventoryLabelY = 130;
    }

    @Override
    protected void init() {
        super.init();
        previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button ->
                changePage(Screen.hasShiftDown() ? 2 : 0))
            .bounds(leftPos + 13, topPos + 172, 20, 20)
            .build());
        nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button ->
                changePage(Screen.hasShiftDown() ? 3 : 1))
            .bounds(leftPos + 209, topPos + 172, 20, 20)
            .build());
        updatePageButtons();
    }

    private void changePage(final int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private void updatePageButtons() {
        if (previousPageButton != null) {
            previousPageButton.active = menu.getPage() > 0;
        }
        if (nextPageButton != null) {
            nextPageButton.active = menu.getPage() + 1 < menu.getPageCount();
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTick, final int mouseX, final int mouseY) {
        drawDefaultBackground(graphics, leftPos, topPos, imageWidth, imageHeight);
        drawPlayerSlots(graphics, leftPos + imageWidth / 2, topPos + 140);
        drawPatternSlots(graphics);
    }

    private static void drawDefaultBackground(final GuiGraphics graphics,
                                              final int x,
                                              final int y,
                                              final int width,
                                              final int height) {
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;
        graphics.blit(GUI_SHEET, x, y, 0, 0, halfWidth, halfHeight);
        graphics.blit(GUI_SHEET, x + halfWidth, y, 150 - halfWidth, 0, halfWidth, halfHeight);
        graphics.blit(GUI_SHEET, x, y + halfHeight, 0, 150 - halfHeight, halfWidth, halfHeight);
        graphics.blit(GUI_SHEET, x + halfWidth, y + halfHeight,
            150 - halfWidth, 150 - halfHeight, halfWidth, halfHeight);
    }

    private static void drawPlayerSlots(final GuiGraphics graphics, final int centerX, final int y) {
        final int x = centerX - 81;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                drawSlot(graphics, x + column * 18, y + row * 18);
            }
        }
        for (int column = 0; column < 9; ++column) {
            drawSlot(graphics, x + column * 18, y + 58);
        }
    }

    private void drawPatternSlots(final GuiGraphics graphics) {
        for (int row = 0; row < MultiblockCrafterMenu.PATTERN_ROWS; ++row) {
            for (int column = 0; column < MultiblockCrafterMenu.PATTERN_COLUMNS; ++column) {
                drawSlot(graphics, leftPos + 8 + column * 18, topPos + 20 + row * 18);
            }
        }
    }

    private static void drawSlot(final GuiGraphics graphics, final int x, final int y) {
        graphics.blit(GUI_SHEET, x, y, 150, 0, 18, 18);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 4210752, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 4210752, false);
        graphics.drawString(
            font,
            Component.translatable("gui.redonestorage.page", menu.getPage() + 1, menu.getPageCount()),
            10,
            224,
            4210752,
            false
        );
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        updatePageButtons();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if ((previousPageButton != null && previousPageButton.isMouseOver(mouseX, mouseY))
            || (nextPageButton != null && nextPageButton.isMouseOver(mouseX, mouseY))) {
            graphics.renderTooltip(
                font,
                Component.translatable("gui.redonestorage.page_shift_help"),
                mouseX,
                mouseY
            );
        }
    }
}
