package net.veroxuniverse.unbound_world.api;

import net.minecraft.resources.Identifier;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;

import java.util.Collection;
import java.util.Optional;

/**
 * Public, stable entry point for addon mods that want to read Unbound World's progression state.
 * <p>
 * Addon mods should depend on {@code unbound_world} as a compile-time mod dependency and
 * reference ONLY this class (and the events in {@link net.veroxuniverse.unbound_world.api.event}).
 * Internal classes such as {@code StageManager} may change between versions without notice;
 * this facade will not.
 */
public final class UnboundWorldApi {

    private UnboundWorldApi() {}

    /**
     * @return the currently unlocked world stage order. {@code -1} means "Sealed World" (no stage cleared yet).
     */
    public static int getUnlockedOrder() {
        return StageManager.getUnlockedOrder();
    }

    /**
     * @return all loaded stage definitions, in no particular order. Use {@link StageDefinition#order()}
     * to sort if needed.
     */
    public static Collection<StageDefinition> getAllStages() {
        return StageManager.getAllStages();
    }

    /**
     * @return the stage definition matching the given order, if one is currently loaded.
     */
    public static Optional<StageDefinition> getStageByOrder(int order) {
        return StageManager.getAllStages().stream()
                .filter(stage -> stage.order() == order)
                .findFirst();
    }

    /**
     * @return the stage definition of the currently unlocked stage, if any (empty when Sealed World).
     */
    public static Optional<StageDefinition> getCurrentStage() {
        return getStageByOrder(getUnlockedOrder());
    }

    public static boolean isItemLocked(Identifier itemId) {
        return StageManager.isItemLocked(itemId);
    }

    public static boolean isBlockLocked(Identifier blockId) {
        return StageManager.isBlockLocked(blockId);
    }

    public static boolean isOreLocked(Identifier oreBlockId) {
        return StageManager.isOreLocked(oreBlockId);
    }

    public static boolean isDimensionLocked(Identifier dimensionId) {
        return StageManager.isDimensionLocked(dimensionId);
    }

    /**
     * @return true if the given boss entity (main or optional, from any loaded stage) cannot yet
     * be damaged because its required prior stage has not been cleared.
     */
    public static boolean isBossLocked(Identifier bossEntityId) {
        return StageManager.isBossLocked(bossEntityId);
    }

    /**
     * @return the stage that owns (registers) the given boss entity, if it is a tracked boss at all.
     */
    public static Optional<StageDefinition> getOwningStageForBoss(Identifier bossEntityId) {
        return StageManager.getOwningStageForBoss(bossEntityId);
    }

    /**
     * @return the boss configuration (drops, player scaling, etc.) for the given entity, if it is
     * registered as a main or optional boss in any loaded stage.
     */
    public static Optional<StageDefinition.BossInfo> getBossInfo(Identifier bossEntityId) {
        return StageManager.getBossInfo(bossEntityId);
    }

    /**
     * @return true if the given entity id is registered as a boss (main or optional) in any loaded stage.
     */
    public static boolean isRegisteredBoss(Identifier entityId) {
        return StageManager.getBossInfo(entityId).isPresent();
    }
}