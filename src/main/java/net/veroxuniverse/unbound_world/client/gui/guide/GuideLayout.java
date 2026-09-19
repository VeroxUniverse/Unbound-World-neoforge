package net.veroxuniverse.unbound_world.client.gui.guide;

public final class GuideLayout {
    private GuideLayout() {}

    public static final int GUI_WIDTH = 256;
    public static final int GUI_HEIGHT = 166;

    public static final int CONTENT_LEFT_OFFSET = 8;
    public static final int CONTENT_RIGHT_OFFSET = 228;
    public static final int CONTENT_TOP_OFFSET = 47;
    public static final int CONTENT_BOTTOM_OFFSET = 156;
    public static final int ROW_HEIGHT = 18;

    public static final int ICON_INDENT_LEFT = 10;
    public static final int ICON_INDENT_RIGHT = 2;
    public static final int ICONS_PER_ROW = (CONTENT_RIGHT_OFFSET - ICON_INDENT_LEFT - ICON_INDENT_RIGHT) / 18;

    public static final int HEADER_AREA_TOP = 16;
    public static final int MAIN_HEADER_AREA_TOP = 34;
    public static final int HEADER_LINE_HEIGHT = 9;
    public static final int HEADER_TEXT_WIDTH = CONTENT_RIGHT_OFFSET - CONTENT_LEFT_OFFSET - 4;

    public static final int SCROLLBAR_TRACK_LEFT = 234;
    public static final int SCROLLBAR_TRACK_TOP = 47;
    public static final int SCROLLBAR_TRACK_RIGHT = 247;
    public static final int SCROLLBAR_TRACK_BOTTOM = 157;
    public static final int SCROLLER_WIDTH = 12;
    public static final int SCROLLER_HEIGHT = 15;

    public static final int BACK_BOX_LEFT = 234;
    public static final int BACK_BOX_TOP = 28;
    public static final int BACK_BOX_RIGHT = 247;
    public static final int BACK_BOX_BOTTOM = 41;

    public static final int TITLE_COLOR = 0xFF404040;
    public static final int SUBTEXT_COLOR = 0xFF707070;
    public static final int SEALED_COLOR = 0xFFAA3333;
    public static final int STAGE_ACTIVE_COLOR = 0xFF3F9E4D;

    public static final int LOOT_SAMPLE_COUNT = 2000;
}