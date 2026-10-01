package net.veroxuniverse.verox_rpg_prog.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.network.SyncStagePayload;
import net.veroxuniverse.verox_rpg_prog.stage.WorldStageSavedData;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public class PlayerSyncHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            int currentOrder = WorldStageSavedData.get(serverPlayer.serverLevel()).getUnlockedOrder();
            PacketDistributor.sendToPlayer(serverPlayer, new SyncStagePayload(currentOrder));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            int currentOrder = WorldStageSavedData.get(serverPlayer.serverLevel()).getUnlockedOrder();
            PacketDistributor.sendToPlayer(serverPlayer, new SyncStagePayload(currentOrder));
        }
    }
}