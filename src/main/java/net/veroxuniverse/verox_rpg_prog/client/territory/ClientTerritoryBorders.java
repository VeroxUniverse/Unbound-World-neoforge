package net.veroxuniverse.verox_rpg_prog.client.territory;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.veroxuniverse.verox_rpg_prog.territory.BorderBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientTerritoryBorders {

    private static final long FLASH_DURATION_MS = 1500L;

    private static List<BorderBox> boxes = List.of();
    private static final Map<BorderBox, Long> FLASHES = new HashMap<>();

    private ClientTerritoryBorders() {}

    public static void setBoxes(List<BorderBox> newBoxes) {
        boxes = List.copyOf(newBoxes);
        FLASHES.keySet().retainAll(boxes);
    }

    public static List<BorderBox> getBoxes() {
        return boxes;
    }

    public static void flash(BlockPos pos) {
        long until = Util.getMillis() + FLASH_DURATION_MS;
        for (BorderBox box : boxes) {
            if (box.contains(pos)) {
                FLASHES.put(box, until);
            }
        }
    }

    public static float flashStrength(BorderBox box) {
        Long until = FLASHES.get(box);
        if (until == null) return 0.0F;

        long remaining = until - Util.getMillis();
        if (remaining <= 0L) {
            FLASHES.remove(box);
            return 0.0F;
        }
        return (float) remaining / FLASH_DURATION_MS;
    }

    public static void reset() {
        boxes = List.of();
        FLASHES.clear();
    }
}