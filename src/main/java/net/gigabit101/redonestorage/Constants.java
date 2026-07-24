package net.gigabit101.redonestorage;

import net.minecraft.resources.ResourceLocation;

public final class Constants {
    public static final String MOD_ID = "redonestorage";

    private Constants() {
    }

    public static ResourceLocation id(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
