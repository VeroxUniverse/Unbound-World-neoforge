package net.veroxuniverse.unbound_world.client.gui.guide;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class GuideRow {
    private final int x;
    private final int baseY;
    private final Component text;
    private final ItemStack icon;
    private final Runnable onClick;
    private final List<Component> tooltip;
    private final List<IconEntry> iconStrip;
    private int currentY;
    private boolean visible = true;

    private GuideRow(int x, int baseY, Component text, ItemStack icon, Runnable onClick, List<Component> tooltip, List<IconEntry> iconStrip) {
        this.x = x;
        this.baseY = baseY;
        this.text = text;
        this.icon = icon;
        this.onClick = onClick;
        this.tooltip = tooltip;
        this.iconStrip = iconStrip;
        this.currentY = baseY;
    }

    public static GuideRow clickableText(int x, int baseY, Component text, ItemStack icon, Runnable onClick, List<Component> tooltip) {
        return new GuideRow(x, baseY, text, icon, onClick, tooltip, null);
    }

    public static GuideRow label(int x, int baseY, Component text) {
        return new GuideRow(x, baseY, text, null, null, null, null);
    }

    public static GuideRow iconStrip(int x, int baseY, List<IconEntry> icons) {
        return new GuideRow(x, baseY, null, null, null, null, icons);
    }

    public boolean isIconStrip() {
        return this.iconStrip != null;
    }

    public int width(Font font) {
        if (this.isIconStrip()) return this.iconStrip.size() * 18;
        return (this.icon != null ? 18 : 0) + font.width(this.text);
    }

    public int x() { return this.x; }
    public int baseY() { return this.baseY; }
    public Component text() { return this.text; }
    public ItemStack icon() { return this.icon; }
    public Runnable onClick() { return this.onClick; }
    public List<Component> tooltip() { return this.tooltip; }
    public List<IconEntry> iconStrip() { return this.iconStrip; }
    public int currentY() { return this.currentY; }
    public void setCurrentY(int currentY) { this.currentY = currentY; }
    public boolean visible() { return this.visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public record IconEntry(ItemStack stack, List<Component> tooltip) {}
}