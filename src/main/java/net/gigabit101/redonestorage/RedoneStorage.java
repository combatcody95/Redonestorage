package net.gigabit101.redonestorage;

import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.refinedmods.refinedstorage.neoforge.support.energy.EnergyStorageAdapter;
import net.gigabit101.redonestorage.content.ModContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(Constants.MOD_ID)
public final class RedoneStorage {
    public RedoneStorage(final IEventBus modEventBus, final ModContainer modContainer) {
        ModContent.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, RedoneStorageConfig.SPEC);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            ModContent.MULTIBLOCK_CRAFTER_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.getContainerProvider()
        );
        event.registerBlockEntity(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            ModContent.ADVANCED_WIRELESS_TRANSMITTER_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.getContainerProvider()
        );
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, context) -> new EnergyStorageAdapter(ModContent.SUPER_WIRELESS_GRID.get()
                .createEnergyStorage(stack)),
            ModContent.SUPER_WIRELESS_GRID.get()
        );
    }
}
