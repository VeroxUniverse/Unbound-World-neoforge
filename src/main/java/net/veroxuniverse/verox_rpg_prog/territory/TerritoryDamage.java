package net.veroxuniverse.verox_rpg_prog.territory;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;

public final class TerritoryDamage {

    public static final ResourceKey<DamageType> SEALED_LAND = ResourceKey.create(Registries.DAMAGE_TYPE, RPGProgression.id("sealed_land"));

    private TerritoryDamage() {}

    public static DamageSource source(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SEALED_LAND));
    }
}