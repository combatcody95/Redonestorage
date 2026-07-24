package net.gigabit101.redonestorage.wireless;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class SuperWirelessGridItem extends WirelessGridItem {
    private static final String TAG_MODE = "redonestorage_mode";

    public enum Mode {
        CRAFTING,
        FLUID,
        MONITOR;

        private Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        private String translationKey() {
            return "mode.redonestorage." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final boolean creative;

    public SuperWirelessGridItem(final boolean creative) {
        super(creative);
        this.creative = creative;
    }

    @Override
    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        return creative ? CreativeEnergyStorage.INSTANCE : super.createEnergyStorage(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level,
                                                   final Player player,
                                                   final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                final Mode next = getMode(stack).next();
                setMode(stack, next);
                player.displayClientMessage(Component.translatable(
                    "message.redonestorage.mode",
                    Component.translatable(next.translationKey())
                ).withStyle(ChatFormatting.GOLD), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    protected void use(@Nullable final Component name,
                       final ServerPlayer player,
                       final SlotReference slotReference,
                       final NetworkItemContext context) {
        final ItemStack referencedStack = slotReference.resolve(player).orElse(ItemStack.EMPTY);
        switch (getMode(referencedStack)) {
            case FLUID -> super.use(name, player, slotReference, context);
            case MONITOR -> Items.INSTANCE.getWirelessAutocraftingMonitor()
                .use(player, referencedStack, slotReference);
            case CRAFTING -> openCraftingGrid(name, player, slotReference, context);
        }
    }

    private void openCraftingGrid(@Nullable final Component name,
                                  final ServerPlayer player,
                                  final SlotReference slotReference,
                                  final NetworkItemContext context) {
        final boolean allowed = context.resolveNetwork()
            .map(network -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network))
            .orElse(true);
        if (!allowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.CRAFTING_GRID);
            return;
        }
        final ItemStack stack = slotReference.resolve(player).orElse(ItemStack.EMPTY);
        final RedoneWirelessCraftingGrid grid = new RedoneWirelessCraftingGrid(context, stack, player.serverLevel());
        final Component correctedName = name != null
            ? name
            : Component.translatable(creative
                ? "item.redonestorage.creative_super_wireless_crafting_grid"
                : "item.redonestorage.super_wireless_crafting_grid");
        Platform.INSTANCE.getMenuOpener().openMenu(
            player,
            new SuperWirelessCraftingGridMenuProvider(correctedName, grid, slotReference)
        );
    }

    public Mode getMode(final ItemStack stack) {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return Mode.CRAFTING;
        }
        final String value = data.copyTag().getString(TAG_MODE);
        try {
            return Mode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return Mode.CRAFTING;
        }
    }

    private void setMode(final ItemStack stack, final Mode mode) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(TAG_MODE, mode.name()));
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(
            "tooltip.redonestorage.mode",
            Component.translatable(getMode(stack).translationKey())
        ).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.redonestorage.mode_help").withStyle(ChatFormatting.GRAY));
    }

}
