package net.veroxuniverse.verox_rpg_prog.client.gui.book;

import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;

public final class BookLayout {

    public static final ResourceLocation TEXTURE = RPGProgression.id("textures/gui/stage_book.png");
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    public static final int FULL_WIDTH = 272;
    public static final int FULL_HEIGHT = 180;
    public static final int PAGE_WIDTH = 116;
    public static final int PAGE_HEIGHT = 156;
    public static final int TOP_PADDING = 18;
    public static final int LEFT_PAGE_X = 16;
    public static final int RIGHT_PAGE_X = 141;
    public static final int LINE_HEIGHT = 9;
    public static final int ENTRY_HEIGHT = 11;
    public static final int ICON_ROW_HEIGHT = 18;
    public static final int CONTENT_BOTTOM = TOP_PADDING + PAGE_HEIGHT;
    public static final int CONTENT_TOP = TOP_PADDING + 18;

    public static final int ARROW_WIDTH = 18;
    public static final int ARROW_HEIGHT = 10;
    public static final int ARROW_U = 272;
    public static final int ARROW_RIGHT_V = 0;
    public static final int ARROW_LEFT_V = 10;
    public static final int ARROW_LEFT_X = -4;
    public static final int ARROW_RIGHT_X = FULL_WIDTH - 14;
    public static final int ARROW_Y = FULL_HEIGHT - 6;

    public static final int BACK_WIDTH = 18;
    public static final int BACK_HEIGHT = 9;
    public static final int BACK_U = 308;
    public static final int BACK_V = 0;
    public static final int BACK_X = FULL_WIDTH / 2 - 9;
    public static final int BACK_Y = FULL_HEIGHT - 5;

    public static final int TITLE_PLATE_X = -8;
    public static final int TITLE_PLATE_Y = 12;
    public static final int TITLE_PLATE_U = 0;
    public static final int TITLE_PLATE_V = 180;
    public static final int TITLE_PLATE_WIDTH = 140;
    public static final int TITLE_PLATE_HEIGHT = 31;

    public static final int SEPARATOR_U = 140;
    public static final int SEPARATOR_V = 180;
    public static final int SEPARATOR_WIDTH = 110;
    public static final int SEPARATOR_HEIGHT = 3;

    public static final int MARKER_SIZE = 8;
    public static final int MARKER_V = 197;
    public static final int MARKER_LOCKED_U = 140;
    public static final int MARKER_CURRENT_U = 148;
    public static final int MARKER_CLEARED_U = 156;

    public static final int NAMEPLATE_COLOR = 0xFFDD00;
    public static final int HEADER_COLOR = 0x333333;
    public static final int TEXT_COLOR = 0x000000;
    public static final int MUTED_COLOR = 0x777777;
    public static final int CLEARED_COLOR = 0x2E7D32;
    public static final int CURRENT_COLOR = 0xB26A00;
    public static final int LOCKED_COLOR = 0xA02020;
    public static final int HOVER_FILL = 0x22000000;

    private BookLayout() {}
}