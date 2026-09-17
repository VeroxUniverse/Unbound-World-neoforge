package net.veroxuniverse.unbound_world.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.veroxuniverse.unbound_world.UnboundWorld;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0");
        registrar.playToClient(
                SyncStagePayload.TYPE,
                SyncStagePayload.STREAM_CODEC,
                SyncStagePayload::handle
        );
    }
}