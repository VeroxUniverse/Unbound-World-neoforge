package net.veroxuniverse.unbound_world.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class StageDataLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageDataLoader.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public StageDataLoader() {
        super(GSON, "stages");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, StageDefinition> loadedStages = new HashMap<>();

        elements.forEach((id, json) -> {
            StageDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> LOGGER.error("Failed to parse stage {}: {}", id, error))
                    .ifPresent(stage -> loadedStages.put(id, stage));
        });

        StageManager.reloadStages(loadedStages);
        LOGGER.info("Loaded {} world progression stages.", loadedStages.size());
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new StageDataLoader());
    }
}