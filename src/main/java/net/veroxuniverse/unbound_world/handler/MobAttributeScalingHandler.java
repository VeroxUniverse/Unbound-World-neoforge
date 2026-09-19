package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;
import net.veroxuniverse.unbound_world.util.AttributeScalingUtil;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class MobAttributeScalingHandler {

    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "stage_scaling_health");
    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "stage_scaling_damage");
    private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "stage_scaling_knockback_resistance");
    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "stage_scaling_armor");

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (!(event.getEntity() instanceof LivingEntity living)) return;

        // Hostile mobs (Monster subclasses) are always eligible. Registered bosses are
        // eligible too, even if their vanilla class does not extend Monster (e.g. the
        // Ender Dragon extends Mob directly, Ghast and Magma Cube are not Monster either) -
        // otherwise those bosses would only ever receive the per-player boss bonus on top
        // of an un-scaled base, instead of on top of the stage's general scaling like every
        // other hostile mob.
        boolean isHostileMob = living instanceof Monster;
        boolean isRegisteredBoss = StageManager.getBossInfo(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType())).isPresent();

        if (!isHostileMob && !isRegisteredBoss) return;

        StageDefinition.MobAttributeScaling scaling = StageManager.getCurrentMobAttributeScaling();
        if (scaling.isNoOp()) return;

        AttributeScalingUtil.applyFractionModifier(living, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, scaling.healthMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(living, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, scaling.damageMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(living, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_MODIFIER_ID, scaling.knockbackResistanceMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(living, Attributes.ARMOR, ARMOR_MODIFIER_ID, scaling.armorMultiplier() - 1.0f);

        living.setHealth(living.getMaxHealth());
    }
}