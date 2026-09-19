package net.veroxuniverse.unbound_world;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.veroxuniverse.unbound_world.command.StageCommand;
import net.veroxuniverse.unbound_world.compat.curios.CuriosRestrictionHandler;
import net.veroxuniverse.unbound_world.handler.BlockRestrictionHandler;
import net.veroxuniverse.unbound_world.handler.ItemRestrictionHandler;
import net.veroxuniverse.unbound_world.stage.ModAttachments;
import net.veroxuniverse.unbound_world.stage.StageDataLoader;
import org.slf4j.Logger;

@Mod(UnboundWorld.MOD_ID)
public class UnboundWorld {
    public static final String MOD_ID = "unbound_world";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UnboundWorld(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(new BlockRestrictionHandler());
        NeoForge.EVENT_BUS.register(new ItemRestrictionHandler());
        NeoForge.EVENT_BUS.addListener(this::onAddServerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onLevelLoad);
        ModAttachments.register(modEventBus);

        if (ModList.get().isLoaded("curios")) {
            NeoForge.EVENT_BUS.register(new CuriosRestrictionHandler());
        }

        LOGGER.info("Unbound World initialized successfully.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "stage_data_loader"), new StageDataLoader());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        StageCommand.register(event.getDispatcher());
    }

    private void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == ServerLevel.OVERWORLD) {
            LOGGER.info("Unbound World level loaded successfully.");
        }
    }
}