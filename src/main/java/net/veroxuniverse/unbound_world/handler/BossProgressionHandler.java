package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageManager;
import net.veroxuniverse.unbound_world.stage.WorldStageData;
import net.veroxuniverse.unbound_world.util.StageNotifier;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class BossProgressionHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        DamageSource source = event.getSource();
        Player killer = null;

        if (source.getEntity() instanceof Player p) {
            killer = p;
        } else if (entity.getKillCredit() instanceof Player p) {
            killer = p;
        }

        if (killer == null) return;

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        StageManager.getStageForBoss(entityId).ifPresent(targetStage -> {
            if (entity.level() instanceof ServerLevel serverLevel) {
                WorldStageData data = WorldStageData.get(serverLevel);

                if (targetStage.order() > data.getUnlockedOrder()) {
                    data.setUnlockedOrder(serverLevel, targetStage.order());

                    StageNotifier.broadcastStageUnlocked(serverLevel.getServer(), targetStage);
                }
            }
        });
    }
}