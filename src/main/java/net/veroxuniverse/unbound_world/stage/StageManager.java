package net.veroxuniverse.unbound_world.stage;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class StageManager {

    private static int currentUnlockedOrder = -1;

    private static final Map<ResourceLocation, StageDefinition> STAGES = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> ITEM_RESTRICTIONS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> BLOCK_RESTRICTIONS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> BOSS_TRIGGERS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> MAIN_BOSS_TRIGGERS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition.BossInfo> ALL_BOSS_INFOS = new HashMap<>();

    public static synchronized void reloadStages(Map<ResourceLocation, StageDefinition> newStages) {
        STAGES.clear();
        ITEM_RESTRICTIONS.clear();
        BLOCK_RESTRICTIONS.clear();
        MAIN_BOSS_TRIGGERS.clear();
        ALL_BOSS_INFOS.clear();

        STAGES.putAll(newStages);

        for (StageDefinition stage : STAGES.values()) {
            for (ResourceLocation item : stage.lockedItems()) {
                ITEM_RESTRICTIONS.put(item, stage);
            }
            for (ResourceLocation block : stage.lockedBlocks()) {
                BLOCK_RESTRICTIONS.put(block, stage);
            }
            stage.mainBoss().ifPresent(boss -> {
                MAIN_BOSS_TRIGGERS.put(boss.entityId(), stage);
                ALL_BOSS_INFOS.put(boss.entityId(), boss);
            });
            for (StageDefinition.BossInfo optBoss : stage.optionalBosses()) {
                ALL_BOSS_INFOS.put(optBoss.entityId(), optBoss);
            }
        }
    }

    public static void setUnlockedOrder(int order) {
        currentUnlockedOrder = order;
    }

    public static int getUnlockedOrder() {
        return currentUnlockedOrder;
    }

    public static boolean isItemLocked(ResourceLocation itemId) {
        StageDefinition req = ITEM_RESTRICTIONS.get(itemId);
        return req != null && req.order() > currentUnlockedOrder;
    }

    public static boolean isBlockLocked(ResourceLocation blockId) {
        StageDefinition req = BLOCK_RESTRICTIONS.get(blockId);
        return req != null && req.order() > currentUnlockedOrder;
    }

    public static Optional<StageDefinition> getRequiredStageForItem(ResourceLocation itemId) {
        return Optional.ofNullable(ITEM_RESTRICTIONS.get(itemId));
    }

    public static Optional<StageDefinition> getRequiredStageForBlock(ResourceLocation blockId) {
        return Optional.ofNullable(BLOCK_RESTRICTIONS.get(blockId));
    }

    public static Collection<StageDefinition> getAllStages() {
        return STAGES.values();
    }

    public static Optional<StageDefinition> getStageForBoss(ResourceLocation entityId) {
        return Optional.ofNullable(MAIN_BOSS_TRIGGERS.get(entityId));
    }

    public static Optional<StageDefinition.BossInfo> getBossInfo(ResourceLocation entityId) {
        return Optional.ofNullable(ALL_BOSS_INFOS.get(entityId));
    }
}