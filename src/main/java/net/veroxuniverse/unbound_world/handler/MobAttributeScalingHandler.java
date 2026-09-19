package net.veroxuniverse.unbound_world.handler;

import net.minecraft.resources.ResourceLocation;
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

        // Only hostile mobs are affected. Passive/neutral animals (Animal, AbstractFish, etc.)
        // and any entity that does not extend Monster is intentionally left untouched.
        if (!(event.getEntity() instanceof Monster monster)) return;

        StageDefinition.MobAttributeScaling scaling = StageManager.getCurrentMobAttributeScaling();
        if (scaling.isNoOp()) return;

        AttributeScalingUtil.applyFractionModifier(monster, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, scaling.healthMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(monster, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, scaling.damageMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(monster, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_MODIFIER_ID, scaling.knockbackResistanceMultiplier() - 1.0f);
        AttributeScalingUtil.applyFractionModifier(monster, Attributes.ARMOR, ARMOR_MODIFIER_ID, scaling.armorMultiplier() - 1.0f);

        monster.setHealth(monster.getMaxHealth());
    }
}