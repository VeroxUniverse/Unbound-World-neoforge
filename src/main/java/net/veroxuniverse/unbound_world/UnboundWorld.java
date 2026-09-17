package net.veroxuniverse.unbound_world;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.veroxuniverse.unbound_world.command.StageCommand;
import net.veroxuniverse.unbound_world.handler.BlockRestrictionHandler;
import net.veroxuniverse.unbound_world.handler.ItemRestrictionHandler;
import net.veroxuniverse.unbound_world.stage.StageManager;
import net.veroxuniverse.unbound_world.stage.WorldStageSavedData;
import org.slf4j.Logger;

@Mod(UnboundWorld.MOD_ID)
public class UnboundWorld {
    public static final String MOD_ID = "unbound_world";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UnboundWorld(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(new BlockRestrictionHandler());
        NeoForge.EVENT_BUS.register(new ItemRestrictionHandler());
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onLevelLoad);

        LOGGER.info("Unbound World initialized successfully.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new StageManager());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        StageCommand.register(event.getDispatcher());
    }

    private void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == ServerLevel.OVERWORLD) {
            WorldStageSavedData data = WorldStageSavedData.get(serverLevel);
            LOGGER.info("Loaded Unbound World Progression Stage Order: {}", data.getUnlockedOrder());
        }
    }
}