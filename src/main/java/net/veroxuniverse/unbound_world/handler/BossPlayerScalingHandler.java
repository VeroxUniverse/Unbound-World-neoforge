package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;
import net.veroxuniverse.unbound_world.util.AttributeScalingUtil;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class BossPlayerScalingHandler {

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath(UnboundWorld.MOD_ID, "boss_player_scaling_health");
    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(UnboundWorld.MOD_ID, "boss_player_scaling_damage");
    private static final Identifier KNOCKBACK_RESISTANCE_MODIFIER_ID = Identifier.fromNamespaceAndPath(UnboundWorld.MOD_ID, "boss_player_scaling_knockback_resistance");
    private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath(UnboundWorld.MOD_ID, "boss_player_scaling_armor");

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity boss)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType());
        StageDefinition.BossInfo bossInfo = StageManager.getBossInfo(entityId).orElse(null);
        if (bossInfo == null) return;

        StageDefinition.PlayerScaling scaling = bossInfo.playerScaling();
        if (scaling.isNoOp()) return;

        int nearbyPlayers = (int) serverLevel.players().stream()
                .filter(ServerPlayer::isAlive)
                .filter(player -> player.distanceToSqr(boss) <= scaling.radius() * scaling.radius())
                .count();

        int extraPlayers = Math.max(0, nearbyPlayers - 1);
        if (extraPlayers <= 0) return;

        AttributeScalingUtil.applyFractionModifier(boss, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, scaling.healthPerExtraPlayer() * extraPlayers);
        AttributeScalingUtil.applyFractionModifier(boss, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, scaling.damagePerExtraPlayer() * extraPlayers);
        AttributeScalingUtil.applyFractionModifier(boss, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE_MODIFIER_ID, scaling.knockbackResistancePerExtraPlayer() * extraPlayers);
        AttributeScalingUtil.applyFractionModifier(boss, Attributes.ARMOR, ARMOR_MODIFIER_ID, scaling.armorPerExtraPlayer() * extraPlayers);

        boss.setHealth(boss.getMaxHealth());
    }
}