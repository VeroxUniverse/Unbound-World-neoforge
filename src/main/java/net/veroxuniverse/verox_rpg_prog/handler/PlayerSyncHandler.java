package net.veroxuniverse.verox_rpg_prog.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.network.SyncStageDefinitionsPayload;
import net.veroxuniverse.verox_rpg_prog.network.SyncStagePayload;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;
import net.veroxuniverse.verox_rpg_prog.stage.WorldStageSavedData;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public class PlayerSyncHandler {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SyncStageDefinitionsPayload payload = new SyncStageDefinitionsPayload(StageManager.getStagesById());
        ServerPlayer player = event.getPlayer();

        if (player != null) {
            PacketDistributor.sendToPlayer(player, payload);
        } else {
            PacketDistributor.sendToAllPlayers(payload);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            sendOrder(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            sendOrder(serverPlayer);
        }
    }

    private static void sendOrder(ServerPlayer player) {
        int currentOrder = WorldStageSavedData.get(player.serverLevel()).getUnlockedOrder();
        PacketDistributor.sendToPlayer(player, new SyncStagePayload(currentOrder));
    }
}