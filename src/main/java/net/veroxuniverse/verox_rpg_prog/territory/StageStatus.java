package net.veroxuniverse.verox_rpg_prog.territory;

import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

public enum StageStatus {
    CLEARED,
    CURRENT,
    LOCKED;

    public static StageStatus of(StageDefinition stage) {
        int currentOrder = StageManager.getUnlockedOrder();
        if (stage.order() <= currentOrder) return CLEARED;
        if (stage.order() == currentOrder + 1) return CURRENT;
        return LOCKED;
    }
}