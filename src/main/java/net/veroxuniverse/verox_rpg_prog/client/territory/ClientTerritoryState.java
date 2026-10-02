package net.veroxuniverse.verox_rpg_prog.client.territory;

public final class ClientTerritoryState {

    private static final float FADE_PER_TICK = 0.05F;

    private static int targetColor;
    private static int displayColor;
    private static float strength;

    private ClientTerritoryState() {}

    public static void setTint(int color) {
        targetColor = color;
        if (color != 0) {
            displayColor = color;
        }
    }

    public static void tick(float deltaTicks) {
        float target = targetColor != 0 ? 1.0F : 0.0F;
        if (strength < target) {
            strength = Math.min(target, strength + FADE_PER_TICK * deltaTicks);
        } else if (strength > target) {
            strength = Math.max(target, strength - FADE_PER_TICK * deltaTicks);
        }
    }

    public static int getDisplayColor() {
        return displayColor;
    }

    public static float getStrength() {
        return strength;
    }

    public static void reset() {
        targetColor = 0;
        strength = 0.0F;
    }
}