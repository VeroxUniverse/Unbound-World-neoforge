package net.veroxuniverse.verox_rpg_prog.client.territory;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public final class TerritoryTintLayer implements LayeredDraw.Layer {

    private static final int BASE_ALPHA = 0x30;
    private static final int EDGE_ALPHA = 0x90;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        ClientTerritoryState.tick(deltaTracker.getRealtimeDeltaTicks());

        float strength = ClientTerritoryState.getStrength();
        if (strength <= 0.0F) return;

        int rgb = ClientTerritoryState.getDisplayColor() & 0xFFFFFF;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int edgeHeight = height / 3;

        int base = color(rgb, BASE_ALPHA, strength);
        int edge = color(rgb, EDGE_ALPHA, strength);
        int clear = rgb;

        graphics.fill(0, 0, width, height, base);
        graphics.fillGradient(0, 0, width, edgeHeight, edge, clear);
        graphics.fillGradient(0, height - edgeHeight, width, height, clear, edge);
    }

    private static int color(int rgb, int alpha, float strength) {
        int scaledAlpha = Math.round(alpha * strength) & 0xFF;
        return (scaledAlpha << 24) | rgb;
    }
}