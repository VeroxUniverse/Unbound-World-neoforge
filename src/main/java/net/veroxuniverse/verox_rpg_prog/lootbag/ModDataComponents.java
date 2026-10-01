package net.veroxuniverse.verox_rpg_prog.lootbag;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, RPGProgression.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LootBagContents>> LOOT_BAG_CONTENTS =
            DATA_COMPONENTS.register("loot_bag_contents", () -> DataComponentType.<LootBagContents>builder()
                    .persistent(LootBagContents.CODEC)
                    .networkSynchronized(LootBagContents.STREAM_CODEC)
                    .build());
}