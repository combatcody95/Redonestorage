package net.gigabit101.redonestorage;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RedoneStorageConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue FRAME_COST;
    public static final ModConfigSpec.IntValue HEAT_COST;
    public static final ModConfigSpec.IntValue CPU_COST;
    public static final ModConfigSpec.IntValue STORAGE_COST;
    public static final ModConfigSpec.IntValue MIN_SIZE;
    public static final ModConfigSpec.IntValue MAX_SIZE;
    public static final ModConfigSpec.IntValue TRANSMITTER_RANGE;
    public static final ModConfigSpec.IntValue TRANSMITTER_RANGE_PER_UPGRADE;
    public static final ModConfigSpec.IntValue TRANSMITTER_ENERGY_USAGE;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("multiblock_crafter");
        FRAME_COST = BUILDER.comment("FE/t used by each frame block.")
            .defineInRange("frame_energy_usage", 0, 0, 10000);
        HEAT_COST = BUILDER.comment("FE/t used by each heat-exchanger block.")
            .defineInRange("heat_energy_usage", 0, 0, 10000);
        CPU_COST = BUILDER.comment("FE/t used by each crafting CPU block.")
            .defineInRange("cpu_energy_usage", 5, 0, 10000);
        STORAGE_COST = BUILDER.comment("FE/t used by each pattern-storage block.")
            .defineInRange("storage_energy_usage", 10, 0, 10000);
        MIN_SIZE = BUILDER.comment("Minimum outside dimension on each axis.")
            .defineInRange("minimum_dimension", 3, 3, 32);
        MAX_SIZE = BUILDER.comment("Maximum outside dimension on each axis.")
            .defineInRange("maximum_dimension", 16, 3, 64);
        BUILDER.pop();

        BUILDER.push("advanced_wireless_transmitter");
        TRANSMITTER_RANGE = BUILDER.comment("Base wireless range in blocks.")
            .defineInRange("base_range", 1000, 1, 1000000);
        TRANSMITTER_RANGE_PER_UPGRADE = BUILDER.comment("Range added by each Refined Storage range upgrade.")
            .defineInRange("range_per_upgrade", 1000, 0, 1000000);
        TRANSMITTER_ENERGY_USAGE = BUILDER.comment("Base FE/t usage.")
            .defineInRange("energy_usage", 100, 0, 1000000);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private RedoneStorageConfig() {
    }
}
