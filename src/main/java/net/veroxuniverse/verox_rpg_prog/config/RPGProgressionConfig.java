package net.veroxuniverse.verox_rpg_prog.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RPGProgressionConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue LOOT_BAGS_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("loot_bags");
        LOOT_BAGS_ENABLED = builder
                .comment("If true, registered bosses drop individually rolled loot bags for every nearby player instead of ground loot.")
                .define("enabled", true);
        builder.pop();

        SPEC = builder.build();
    }

    private RPGProgressionConfig() {}
}