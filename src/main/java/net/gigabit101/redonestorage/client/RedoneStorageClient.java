package net.gigabit101.redonestorage.client;

import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.gigabit101.redonestorage.Constants;
import net.gigabit101.redonestorage.content.ModContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class RedoneStorageClient {
    private RedoneStorageClient() {
    }

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            final NetworkItemPropertyFunction property = new NetworkItemPropertyFunction();
            ItemProperties.register(
                ModContent.SUPER_WIRELESS_GRID.get(),
                NetworkItemPropertyFunction.NAME,
                property
            );
            ItemProperties.register(
                ModContent.CREATIVE_SUPER_WIRELESS_GRID.get(),
                NetworkItemPropertyFunction.NAME,
                property
            );
        });
    }

    @SubscribeEvent
    public static void registerScreens(final RegisterMenuScreensEvent event) {
        event.register(ModContent.MULTIBLOCK_CRAFTER_MENU.get(), MultiblockCrafterScreen::new);
        event.register(ModContent.ADVANCED_WIRELESS_TRANSMITTER_MENU.get(), AdvancedWirelessTransmitterScreen::new);
        event.register(ModContent.SUPER_WIRELESS_CRAFTING_GRID_MENU.get(), CraftingGridScreen::new);
    }
}
