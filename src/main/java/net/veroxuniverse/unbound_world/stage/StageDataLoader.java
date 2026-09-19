package net.veroxuniverse.unbound_world.stage;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class StageDataLoader extends SimpleJsonResourceReloadListener<StageDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageDataLoader.class);

    public StageDataLoader() {
        super(StageDefinition.CODEC, FileToIdConverter.json("stages"));
    }

    @Override
    protected void apply(Map<Identifier, StageDefinition> stages, ResourceManager resourceManager, ProfilerFiller profiler) {
        StageManager.reloadStages(stages);
        LOGGER.info("Loaded {} world progression stages.", stages.size());
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(UnboundWorld.MOD_ID, "stages"), new StageDataLoader());
    }
}