package net.veroxuniverse.unbound_world.stage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.veroxuniverse.unbound_world.UnboundWorld;

import java.util.*;

public class StageManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, StageData> STAGES = new HashMap<>();

    private static int currentUnlockedOrder = -1;

    public StageManager() {
        super(GSON, "unbound_stages");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        STAGES.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement json = entry.getValue();

            StageData.CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> UnboundWorld.LOGGER.error("Failed to parse stage {}: {}", fileId, error))
                    .ifPresent(stage -> {
                        STAGES.put(stage.id(), stage);
                        UnboundWorld.LOGGER.info("Loaded Stage: {} (Order: {})", stage.id(), stage.order());
                    });
        }
    }

    public static boolean isItemLocked(ResourceLocation itemId) {
        for (StageData stage : STAGES.values()) {
            if (stage.order() > currentUnlockedOrder && stage.lockedItems().contains(itemId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockLocked(ResourceLocation blockId) {
        for (StageData stage : STAGES.values()) {
            if (stage.order() > currentUnlockedOrder && stage.lockedBlocks().contains(blockId)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<StageData> getRequiredStageForItem(ResourceLocation itemId) {
        return STAGES.values().stream()
                .filter(stage -> stage.lockedItems().contains(itemId))
                .findFirst();
    }

    public static Optional<StageData> getRequiredStageForBlock(ResourceLocation blockId) {
        return STAGES.values().stream()
                .filter(stage -> stage.lockedBlocks().contains(blockId))
                .findFirst();
    }

    public static void setUnlockedOrder(int order) {
        currentUnlockedOrder = order;
    }
}