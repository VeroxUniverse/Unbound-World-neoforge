package net.veroxuniverse.verox_rpg_prog.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class BookScreenOpener {

    private static final float OPEN_PITCH = 0.8F;

    private BookScreenOpener() {}

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, OPEN_PITCH));
        minecraft.setScreen(new StageBookScreen());
    }
}