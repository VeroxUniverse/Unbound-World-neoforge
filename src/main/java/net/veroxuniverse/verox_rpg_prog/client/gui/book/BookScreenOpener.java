package net.veroxuniverse.verox_rpg_prog.client.gui.book;

import net.minecraft.client.Minecraft;

public final class BookScreenOpener {

    private BookScreenOpener() {}

    public static void open() {
        Minecraft.getInstance().setScreen(new StageBookScreen());
    }
}