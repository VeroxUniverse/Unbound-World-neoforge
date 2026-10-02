package net.veroxuniverse.verox_rpg_prog.territory;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;
import net.veroxuniverse.verox_rpg_prog.util.AttributeScalingUtil;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public final class TerritoryMobScalingHandler {

    private static final ResourceLocation HEALTH_MODIFIER_ID = RPGProgression.id("territory_scaling_health");
    private static final ResourceLocation DAMAGE_MODIFIER_ID = RPGProgression.id("territory_scaling_damage");
    private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER_ID = RPGProgression.id("territory_scaling_knockback_resistance");
    private static final ResourceLocation ARMOR_MODIFIER_ID = RPGProgression.id("territory_scaling_armor");

    private TerritoryMobScalingHandler() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.loadedFromDisk()) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        boolean isHostileMob = living instanceof Monster;
        boolean isRegisteredBoss = StageManager.getBossInfo(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType())).isPresent();
        if (!isHostileMob && !isRegisteredBoss) return;

        StageDefinition.MobAttributeScaling scaling = null;
        for (TerritoryMatcher.Match match : TerritoryMatcher.matchesAt(level, living.blockPosition())) {
            if (!match.territory().mobScaling().isNoOp()) {
                scaling = match.territory().mobScaling();
                break;
            }
        }
        if (scaling == null) return;

        AttributeScalingUtil.applyFractionModifier(living, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, scaling.healthMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(living, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, scaling.damageMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(living, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_MODIFIER_ID, scaling.knockbackResistanceMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(living, Attributes.ARMOR, ARMOR_MODIFIER_ID, scaling.armorMultiplier() - 1.0f);
        applyExtraAttributes(living, scaling.attributes());

        living.setHealth(living.getMaxHealth());
    }

    private static void applyExtraAttributes(LivingEntity living, List<StageDefinition.AttributeEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            StageDefinition.AttributeEntry entry = entries.get(i);
            Optional<Holder.Reference<Attribute>> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(entry.attribute());
            if (attribute.isEmpty()) continue;

            AttributeScalingUtil.applyModifier(
                    living,
                    attribute.get(),
                    RPGProgression.id("territory_scaling_extra_" + i),
                    entry.amount(),
                    entry.operation()
            );
        }
    }
}