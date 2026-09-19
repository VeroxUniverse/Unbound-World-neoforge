package net.veroxuniverse.unbound_world.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.network.SyncStagePayload;
import net.veroxuniverse.unbound_world.stage.WorldStageData;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class PlayerSyncHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            int currentOrder = WorldStageData.get(serverPlayer.level()).getUnlockedOrder();
            PacketDistributor.sendToPlayer(serverPlayer, new SyncStagePayload(currentOrder));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            int currentOrder = WorldStageData.get(serverPlayer.level()).getUnlockedOrder();
            PacketDistributor.sendToPlayer(serverPlayer, new SyncStagePayload(currentOrder));
        }
    }
}