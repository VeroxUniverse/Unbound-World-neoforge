package net.veroxuniverse.unbound_world.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageManager;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class ModNetworking {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncStagePayload.TYPE,
                SyncStagePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        StageManager.setClientUnlockedOrder(payload.unlockedOrder())
                )
        );
    }

    public static void sendToPlayer(ServerPlayer player, int unlockedOrder) {
        PacketDistributor.sendToPlayer(player, new SyncStagePayload(unlockedOrder));
    }

    public static void sendToAll(int unlockedOrder) {
        PacketDistributor.sendToAllPlayers(new SyncStagePayload(unlockedOrder));
    }
}