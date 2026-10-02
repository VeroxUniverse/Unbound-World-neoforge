package net.veroxuniverse.verox_rpg_prog.client.network;

import net.minecraft.client.Minecraft;
import net.veroxuniverse.verox_rpg_prog.client.territory.ClientTerritoryBorders;
import net.veroxuniverse.verox_rpg_prog.client.territory.ClientTerritoryState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.veroxuniverse.verox_rpg_prog.network.SyncStageDefinitionsPayload;
import net.veroxuniverse.verox_rpg_prog.network.SyncStagePayload;
import net.veroxuniverse.verox_rpg_prog.network.TerritoryBorderFlashPayload;
import net.veroxuniverse.verox_rpg_prog.network.TerritoryBordersPayload;
import net.veroxuniverse.verox_rpg_prog.network.TerritoryStatePayload;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

public final class ClientPayloadHandler {

    private ClientPayloadHandler() {}

    public static void handleStage(SyncStagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            StageManager.setUnlockedOrder(payload.unlockedOrder());
            refreshWorldRendering();
        });
    }

    public static void handleStageDefinitions(SyncStageDefinitionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().hasSingleplayerServer()) return;

            StageManager.reloadStages(payload.stages());
            refreshWorldRendering();
        });
    }

    public static void handleTerritoryState(TerritoryStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientTerritoryState.setTint(payload.tintColor()));
    }

    public static void handleTerritoryBorders(TerritoryBordersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientTerritoryBorders.setBoxes(payload.boxes()));
    }

    public static void handleTerritoryBorderFlash(TerritoryBorderFlashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientTerritoryBorders.flash(payload.pos()));
    }

    private static void refreshWorldRendering() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.levelRenderer != null && minecraft.level != null) {
            minecraft.levelRenderer.allChanged();
        }
    }
}