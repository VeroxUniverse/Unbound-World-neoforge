package net.veroxuniverse.unbound_world.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class DimensionLockHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        if (entity instanceof Player player && player.isCreative()) return;

        Identifier dimensionId = event.getDimension().identifier();

        if (!StageManager.isDimensionLocked(dimensionId)) return;

        event.setCanceled(true);

        if (entity instanceof Player player) {
            StageManager.getRequiredStageForDimension(dimensionId).ifPresentOrElse(
                    stage -> sendLockedMessage(player, stage),
                    () -> player.sendOverlayMessage(
                            Component.translatable("message.unbound_world.dimension_sealed_generic")
                    )
            );
        }
    }

    private static void sendLockedMessage(Player player, StageDefinition requiredStage) {
        player.sendOverlayMessage(
                Component.translatable("message.unbound_world.dimension_sealed", Component.translatable(requiredStage.translationKey()))
        );
    }
}