package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class BossKillLockHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        if (!StageManager.isBossLocked(entityId)) return;

        if (event.getSource().getEntity() instanceof Player attacker && attacker.isCreative()) {
            return;
        }

        event.setCanceled(true);

        if (event.getSource().getEntity() instanceof Player attacker) {
            StageManager.getOwningStageForBoss(entityId).ifPresentOrElse(
                    stage -> sendLockedMessage(attacker, stage),
                    () -> attacker.sendOverlayMessage(
                            Component.translatable("message.unbound_world.boss_sealed_generic")
                    )
            );
        }
    }

    private static void sendLockedMessage(Player attacker, StageDefinition requiredStage) {
        attacker.sendOverlayMessage(
                Component.translatable("message.unbound_world.boss_sealed", Component.translatable(requiredStage.translationKey()))
        );
    }
}