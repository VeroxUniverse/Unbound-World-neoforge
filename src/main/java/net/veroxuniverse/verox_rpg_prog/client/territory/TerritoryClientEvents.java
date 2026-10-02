package net.veroxuniverse.verox_rpg_prog.client.territory;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;

public final class TerritoryClientEvents {

    private TerritoryClientEvents() {}

    @EventBusSubscriber(modid = RPGProgression.MOD_ID, value = Dist.CLIENT)
    public static final class ModBus {

        private ModBus() {}

        @SubscribeEvent
        public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, RPGProgression.id("territory_tint"), new TerritoryTintLayer());
        }
    }

    @EventBusSubscriber(modid = RPGProgression.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {

        private GameBus() {}

        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientTerritoryState.reset();
            ClientTerritoryBorders.reset();
        }
    }
}