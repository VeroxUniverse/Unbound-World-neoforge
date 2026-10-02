package net.veroxuniverse.verox_rpg_prog.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.client.network.ClientPayloadHandler;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.1.0");
        registrar.playToClient(
                SyncStagePayload.TYPE,
                SyncStagePayload.STREAM_CODEC,
                ClientPayloadHandler::handleStage
        );
        registrar.playToClient(
                SyncStageDefinitionsPayload.TYPE,
                SyncStageDefinitionsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleStageDefinitions
        );
        registrar.playToClient(
                TerritoryStatePayload.TYPE,
                TerritoryStatePayload.STREAM_CODEC,
                ClientPayloadHandler::handleTerritoryState
        );
        registrar.playToClient(
                TerritoryBordersPayload.TYPE,
                TerritoryBordersPayload.STREAM_CODEC,
                ClientPayloadHandler::handleTerritoryBorders
        );
        registrar.playToClient(
                TerritoryBorderFlashPayload.TYPE,
                TerritoryBorderFlashPayload.STREAM_CODEC,
                ClientPayloadHandler::handleTerritoryBorderFlash
        );
    }
}