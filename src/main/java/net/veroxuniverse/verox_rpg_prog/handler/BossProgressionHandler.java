package net.veroxuniverse.verox_rpg_prog.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.api.event.BossDefeatedEvent;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;
import net.veroxuniverse.verox_rpg_prog.stage.WorldStageSavedData;
import net.veroxuniverse.verox_rpg_prog.util.StageNotifier;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public class BossProgressionHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        ServerPlayer killer = resolveKiller(event.getSource(), entity);
        if (killer == null) return;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        StageManager.getStageForBoss(entityId).ifPresent(targetStage -> {
            WorldStageSavedData data = WorldStageSavedData.get(serverLevel);
            if (targetStage.order() > data.getUnlockedOrder()) {
                data.setUnlockedOrder(serverLevel, targetStage.order());
                StageNotifier.broadcastStageUnlocked(serverLevel.getServer(), targetStage);
            }
        });

        StageDefinition.BossInfo bossInfo = StageManager.getBossInfo(entityId).orElse(null);
        StageDefinition owningStage = StageManager.getOwningStageForBoss(entityId).orElse(null);
        if (bossInfo == null || owningStage == null) return;

        boolean wasMainBoss = owningStage.mainBoss()
                .map(mainBoss -> mainBoss.entityId().equals(entityId))
                .orElse(false);

        NeoForge.EVENT_BUS.post(new BossDefeatedEvent(serverLevel, entity, killer, bossInfo, owningStage, wasMainBoss));
    }

    private static ServerPlayer resolveKiller(DamageSource source, LivingEntity entity) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return player;
        }
        if (entity.getKillCredit() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }
}