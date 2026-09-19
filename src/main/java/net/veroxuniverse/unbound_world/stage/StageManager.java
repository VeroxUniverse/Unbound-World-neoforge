package net.veroxuniverse.unbound_world.stage;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class StageManager {

    private static int currentUnlockedOrder = -1;

    private static final Map<ResourceLocation, StageDefinition> STAGES = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> ITEM_RESTRICTIONS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> BLOCK_RESTRICTIONS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> DIMENSION_RESTRICTIONS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> MAIN_BOSS_TRIGGERS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition.BossInfo> ALL_BOSS_INFOS = new HashMap<>();
    private static final Map<ResourceLocation, StageDefinition> BOSS_OWNING_STAGE = new HashMap<>();
    private static final NavigableMap<Integer, Map<ResourceLocation, StageDefinition.MobEquipmentOverride>> EQUIPMENT_BY_STAGE_ORDER = new TreeMap<>();
    private static final Map<ResourceLocation, StageDefinition> ORE_OWNING_STAGE = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> ORE_DISGUISE = new HashMap<>();

    public static synchronized void reloadStages(Map<ResourceLocation, StageDefinition> newStages) {
        STAGES.clear();
        ITEM_RESTRICTIONS.clear();
        BLOCK_RESTRICTIONS.clear();
        DIMENSION_RESTRICTIONS.clear();
        MAIN_BOSS_TRIGGERS.clear();
        ALL_BOSS_INFOS.clear();
        BOSS_OWNING_STAGE.clear();
        EQUIPMENT_BY_STAGE_ORDER.clear();
        ORE_OWNING_STAGE.clear();
        ORE_DISGUISE.clear();

        STAGES.putAll(newStages);

        for (StageDefinition stage : STAGES.values()) {
            for (ResourceLocation item : stage.lockedItems()) {
                ITEM_RESTRICTIONS.put(item, stage);
            }
            for (ResourceLocation block : stage.lockedBlocks()) {
                BLOCK_RESTRICTIONS.put(block, stage);
            }
            for (ResourceLocation dimension : stage.lockedDimensions()) {
                DIMENSION_RESTRICTIONS.put(dimension, stage);
            }
            stage.mainBoss().ifPresent(boss -> {
                MAIN_BOSS_TRIGGERS.put(boss.entityId(), stage);
                ALL_BOSS_INFOS.put(boss.entityId(), boss);
                BOSS_OWNING_STAGE.put(boss.entityId(), stage);
            });
            for (StageDefinition.BossInfo optBoss : stage.optionalBosses()) {
                ALL_BOSS_INFOS.put(optBoss.entityId(), optBoss);
                BOSS_OWNING_STAGE.put(optBoss.entityId(), stage);
            }
            for (StageDefinition.MobEquipmentOverride override : stage.mobEquipment()) {
                Map<ResourceLocation, StageDefinition.MobEquipmentOverride> forStage =
                        EQUIPMENT_BY_STAGE_ORDER.computeIfAbsent(stage.order(), k -> new HashMap<>());
                for (ResourceLocation entityId : override.entityIds()) {
                    forStage.put(entityId, override);
                }
            }
            for (StageDefinition.OreDisguise disguise : stage.lockedOres()) {
                ORE_OWNING_STAGE.put(disguise.oreBlock(), stage);
                ORE_DISGUISE.put(disguise.oreBlock(), disguise.disguiseBlock());
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

    public static boolean isDimensionLocked(ResourceLocation dimensionId) {
        StageDefinition req = DIMENSION_RESTRICTIONS.get(dimensionId);
        return req != null && req.order() > currentUnlockedOrder;
    }

    public static boolean isOreLocked(ResourceLocation oreBlockId) {
        StageDefinition req = ORE_OWNING_STAGE.get(oreBlockId);
        return req != null && req.order() > currentUnlockedOrder;
    }

    public static Optional<ResourceLocation> getDisguiseBlock(ResourceLocation oreBlockId) {
        return Optional.ofNullable(ORE_DISGUISE.get(oreBlockId));
    }

    public static boolean isBossLocked(ResourceLocation entityId) {
        StageDefinition owningStage = BOSS_OWNING_STAGE.get(entityId);
        return owningStage != null && currentUnlockedOrder < owningStage.order() - 1;
    }

    public static StageDefinition.MobAttributeScaling getCurrentMobAttributeScaling() {
        return STAGES.values().stream()
                .filter(stage -> stage.order() == currentUnlockedOrder)
                .map(StageDefinition::mobAttributeScaling)
                .findFirst()
                .orElse(StageDefinition.MobAttributeScaling.NONE);
    }

    public static Optional<StageDefinition.MobEquipmentOverride> getCurrentEquipmentOverride(ResourceLocation entityId) {
        NavigableMap<Integer, Map<ResourceLocation, StageDefinition.MobEquipmentOverride>> eligibleStages =
                EQUIPMENT_BY_STAGE_ORDER.headMap(currentUnlockedOrder, true);

        for (Map<ResourceLocation, StageDefinition.MobEquipmentOverride> forStage : eligibleStages.descendingMap().values()) {
            StageDefinition.MobEquipmentOverride override = forStage.get(entityId);
            if (override != null) {
                return Optional.of(override);
            }
        }

        return Optional.empty();
    }

    public static Optional<StageDefinition> getRequiredStageForItem(ResourceLocation itemId) {
        return Optional.ofNullable(ITEM_RESTRICTIONS.get(itemId));
    }

    public static Optional<StageDefinition> getRequiredStageForBlock(ResourceLocation blockId) {
        return Optional.ofNullable(BLOCK_RESTRICTIONS.get(blockId));
    }

    public static Optional<StageDefinition> getRequiredStageForOre(ResourceLocation oreBlockId) {
        return Optional.ofNullable(ORE_OWNING_STAGE.get(oreBlockId));
    }

    public static Optional<StageDefinition> getRequiredStageForDimension(ResourceLocation dimensionId) {
        return Optional.ofNullable(DIMENSION_RESTRICTIONS.get(dimensionId));
    }

    public static Optional<StageDefinition> getOwningStageForBoss(ResourceLocation entityId) {
        return Optional.ofNullable(BOSS_OWNING_STAGE.get(entityId));
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