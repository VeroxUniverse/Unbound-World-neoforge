package net.veroxuniverse.unbound_world.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.client.gui.guide.GuideLayout;
import net.veroxuniverse.unbound_world.client.gui.guide.GuideRow;
import net.veroxuniverse.unbound_world.client.gui.guide.GuideViewBuilder;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;

import java.util.ArrayList;
import java.util.List;

public class UnboundGuideScreen extends Screen {

    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "textures/gui/unbound_guide.png");
    private static final ResourceLocation GUI_TEXTURE_MENU = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "textures/gui/unbound_guide_menu.png");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");

    private enum ViewMode { STAGE_LIST, STAGE_DETAIL, BOSS_DROPS }

    private ViewMode currentMode = ViewMode.STAGE_LIST;
    private StageDefinition selectedStage = null;
    private StageDefinition.BossInfo selectedBoss = null;

    private List<GuideRow> contentRows = List.of();
    private List<FormattedCharSequence> headerLines = List.of();

    private double scrollAmount = 0.0;
    private int maxScroll = 0;
    private boolean draggingScrollbar = false;

    public UnboundGuideScreen() {
        super(Component.translatable("gui.unbound_world.guide_title"));
    }

    @Override
    protected void init() {
        this.scrollAmount = 0.0;

        int left = (this.width - GuideLayout.GUI_WIDTH) / 2;
        int top = (this.height - GuideLayout.GUI_HEIGHT) / 2;

        GuideViewBuilder.BuildResult result = switch (this.currentMode) {
            case STAGE_LIST -> GuideViewBuilder.buildStageList(this.font, left, top, this::onSelectStage);
            case STAGE_DETAIL -> this.selectedStage == null
                    ? new GuideViewBuilder.BuildResult(List.of(), List.of())
                    : GuideViewBuilder.buildStageDetail(this.font, left, top, this.selectedStage, this::onSelectBoss);
            case BOSS_DROPS -> this.selectedBoss == null
                    ? new GuideViewBuilder.BuildResult(List.of(), List.of())
                    : GuideViewBuilder.buildBossDrops(this.font, left, top, this.selectedBoss);
        };

        this.contentRows = result.rows();
        this.headerLines = result.headerLines();

        this.finalizeScroll(top);
    }

    private void onSelectStage(StageDefinition stage) {
        this.selectedStage = stage;
        this.currentMode = ViewMode.STAGE_DETAIL;
        this.init();
    }

    private void onSelectBoss(StageDefinition.BossInfo boss) {
        this.selectedBoss = boss;
        this.currentMode = ViewMode.BOSS_DROPS;
        this.init();
    }

    private void onNavigateBack() {
        switch (this.currentMode) {
            case STAGE_LIST -> this.onClose();
            case STAGE_DETAIL -> {
                this.currentMode = ViewMode.STAGE_LIST;
                this.selectedStage = null;
                this.init();
            }
            case BOSS_DROPS -> {
                this.currentMode = ViewMode.STAGE_DETAIL;
                this.selectedBoss = null;
                this.init();
            }
        }
    }

    private void finalizeScroll(int top) {
        int contentTop = top + GuideLayout.CONTENT_TOP_OFFSET;
        int contentBottom = top + GuideLayout.CONTENT_BOTTOM_OFFSET;
        int viewportHeight = contentBottom - contentTop;

        int maxBottom = contentTop;
        for (GuideRow row : this.contentRows) {
            maxBottom = Math.max(maxBottom, row.baseY() + GuideLayout.ROW_HEIGHT);
        }

        this.maxScroll = Math.max(0, (maxBottom - contentTop) - viewportHeight);
        this.layoutRows(top);
    }

    private void layoutRows(int top) {
        int contentTop = top + GuideLayout.CONTENT_TOP_OFFSET;
        int contentBottom = top + GuideLayout.CONTENT_BOTTOM_OFFSET;

        for (GuideRow row : this.contentRows) {
            int y = row.baseY() - (int) Math.round(this.scrollAmount);
            row.setCurrentY(y);
            row.setVisible(y + GuideLayout.ROW_HEIGHT > contentTop && y < contentBottom);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - GuideLayout.GUI_WIDTH) / 2;
        int top = (this.height - GuideLayout.GUI_HEIGHT) / 2;

        boolean isMainMenu = this.currentMode == ViewMode.STAGE_LIST;
        ResourceLocation activeTexture = isMainMenu ? GUI_TEXTURE_MENU : GUI_TEXTURE;

        graphics.blit(activeTexture, left, top, 0, 0, GuideLayout.GUI_WIDTH, GuideLayout.GUI_HEIGHT);

        this.renderTitleArea(graphics, left, top);

        List<Component> hoveredTooltip = null;

        if (!isMainMenu) {
            int boxWidth = GuideLayout.BACK_BOX_RIGHT - GuideLayout.BACK_BOX_LEFT;
            int boxHeight = GuideLayout.BACK_BOX_BOTTOM - GuideLayout.BACK_BOX_TOP;
            int centerX = left + GuideLayout.BACK_BOX_LEFT + boxWidth / 2;
            int centerY = top + GuideLayout.BACK_BOX_TOP + boxHeight / 2 - 3;

            graphics.drawCenteredString(this.font, Component.literal("«"), centerX, centerY, 0xFFFFFF);

            if (isHovering(left + GuideLayout.BACK_BOX_LEFT, top + GuideLayout.BACK_BOX_TOP, boxWidth, boxHeight, mouseX, mouseY)) {
                hoveredTooltip = List.of(Component.translatable("gui.unbound_world.click_to_go_back"));
            }
        }

        int headerY = top + GuideLayout.HEADER_AREA_TOP;
        for (FormattedCharSequence line : this.headerLines) {
            graphics.drawString(this.font, line, left + GuideLayout.CONTENT_LEFT_OFFSET, headerY, GuideLayout.SUBTEXT_COLOR, false);
            headerY += GuideLayout.HEADER_LINE_HEIGHT;
        }

        int contentTop = top + GuideLayout.CONTENT_TOP_OFFSET;
        int contentBottom = top + GuideLayout.CONTENT_BOTTOM_OFFSET;

        this.layoutRows(top);

        graphics.enableScissor(left + GuideLayout.CONTENT_LEFT_OFFSET, contentTop, left + GuideLayout.CONTENT_RIGHT_OFFSET, contentBottom);
        for (GuideRow row : this.contentRows) {
            if (!row.visible()) continue;

            if (row.isIconStrip()) {
                for (int i = 0; i < row.iconStrip().size(); i++) {
                    GuideRow.IconEntry entry = row.iconStrip().get(i);
                    int iconX = row.x() + i * 18;
                    int iconY = row.currentY();

                    graphics.blit(GUI_TEXTURE, iconX, iconY, 0, 176, 18, 18);
                    graphics.renderFakeItem(entry.stack(), iconX + 1, iconY + 1);
                    graphics.renderItemDecorations(this.font, entry.stack(), iconX + 1, iconY + 1);

                    if (mouseX >= iconX + 1 && mouseX < iconX + 17 && mouseY >= iconY + 1 && mouseY < iconY + 17) {
                        List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), entry.stack()));
                        tooltip.addAll(entry.tooltip());
                        hoveredTooltip = tooltip;
                    }
                }
            } else {
                boolean hovered = row.onClick() != null && isHovering(row.x(), row.currentY(), row.width(this.font), GuideLayout.ROW_HEIGHT, mouseX, mouseY);

                if (row.icon() != null) {
                    graphics.renderFakeItem(row.icon(), row.x(), row.currentY() + 1);
                }

                int textX = row.icon() != null ? row.x() + 18 : row.x();
                int textY = row.currentY() + (GuideLayout.ROW_HEIGHT - 8) / 2;
                Component displayText = hovered ? row.text().copy().withStyle(Style.EMPTY.withUnderlined(true)) : row.text();
                graphics.drawString(this.font, displayText, textX, textY, GuideLayout.TITLE_COLOR, false);

                if (hovered && row.tooltip() != null) {
                    hoveredTooltip = row.tooltip();
                }
            }
        }
        graphics.disableScissor();

        this.renderScrollbarThumb(graphics, left, top);

        if (hoveredTooltip != null) {
            graphics.renderComponentTooltip(this.font, hoveredTooltip, mouseX, mouseY);
        }
    }

    private void renderTitleArea(GuiGraphics graphics, int left, int top) {
        if (this.currentMode == ViewMode.STAGE_LIST) {
            graphics.drawString(this.font, Component.translatable("gui.unbound_world.guide_title").copy().withStyle(Style.EMPTY.withBold(true)), left + 8, top + 7, GuideLayout.TITLE_COLOR, false);

            int currentOrder = StageManager.getUnlockedOrder();
            Component statusValue = currentOrder < 0
                    ? Component.translatable("gui.unbound_world.sealed_world").withStyle(Style.EMPTY.withColor(GuideLayout.SEALED_COLOR))
                    : Component.translatable("gui.unbound_world.stage_number", currentOrder).withStyle(Style.EMPTY.withColor(GuideLayout.STAGE_ACTIVE_COLOR));

            Component prefix = Component.translatable("gui.unbound_world.world_stage_prefix");
            graphics.drawString(this.font, prefix, left + 8, top + 19, GuideLayout.SUBTEXT_COLOR, false);
            graphics.drawString(this.font, statusValue, left + 8 + this.font.width(prefix), top + 19, GuideLayout.SUBTEXT_COLOR, false);

            graphics.drawString(this.font, Component.translatable("gui.unbound_world.progressions_label"), left + 8, top + GuideLayout.MAIN_HEADER_AREA_TOP, GuideLayout.TITLE_COLOR, false);

        } else if (this.currentMode == ViewMode.STAGE_DETAIL && this.selectedStage != null) {
            graphics.drawString(this.font, Component.translatable(this.selectedStage.translationKey()).copy().withStyle(Style.EMPTY.withBold(true)), left + 8, top + 7, GuideLayout.TITLE_COLOR, false);

        } else if (this.currentMode == ViewMode.BOSS_DROPS && this.selectedBoss != null) {
            Component bossName = GuideViewBuilder.getBossDisplayName(this.selectedBoss).copy().withStyle(Style.EMPTY.withBold(true));
            graphics.drawString(this.font, bossName.copy().append(Component.translatable("gui.unbound_world.details_suffix")), left + 8, top + 7, GuideLayout.TITLE_COLOR, false);
        }
    }

    private static boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void renderScrollbarThumb(GuiGraphics graphics, int left, int top) {
        if (this.maxScroll <= 0) return;

        int trackTop = top + GuideLayout.SCROLLBAR_TRACK_TOP;
        int trackBottom = top + GuideLayout.SCROLLBAR_TRACK_BOTTOM;
        int trackHeight = trackBottom - trackTop;
        int trackWidth = (GuideLayout.SCROLLBAR_TRACK_RIGHT - GuideLayout.SCROLLBAR_TRACK_LEFT) + 1;

        double scrollFraction = this.scrollAmount / this.maxScroll;
        int thumbY = trackTop + (int) Math.round((trackHeight - GuideLayout.SCROLLER_HEIGHT) * scrollFraction);
        int thumbX = left + GuideLayout.SCROLLBAR_TRACK_LEFT + (trackWidth - GuideLayout.SCROLLER_WIDTH) / 2;

        graphics.blitSprite(SCROLLER_SPRITE, thumbX, thumbY, GuideLayout.SCROLLER_WIDTH, GuideLayout.SCROLLER_HEIGHT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.maxScroll > 0) {
            this.scrollAmount -= scrollY * (GuideLayout.ROW_HEIGHT / 2.0);
            this.scrollAmount = Math.max(0, Math.min(this.maxScroll, this.scrollAmount));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (this.width - GuideLayout.GUI_WIDTH) / 2;
            int top = (this.height - GuideLayout.GUI_HEIGHT) / 2;

            if (this.currentMode != ViewMode.STAGE_LIST) {
                if (isHovering(left + GuideLayout.BACK_BOX_LEFT, top + GuideLayout.BACK_BOX_TOP, GuideLayout.BACK_BOX_RIGHT - GuideLayout.BACK_BOX_LEFT, GuideLayout.BACK_BOX_BOTTOM - GuideLayout.BACK_BOX_TOP, (int) mouseX, (int) mouseY)) {
                    this.onNavigateBack();
                    return true;
                }
            }

            int trackLeft = left + GuideLayout.SCROLLBAR_TRACK_LEFT;
            int trackRight = left + GuideLayout.SCROLLBAR_TRACK_RIGHT;
            int trackTop = top + GuideLayout.SCROLLBAR_TRACK_TOP;
            int trackBottom = top + GuideLayout.SCROLLBAR_TRACK_BOTTOM;

            if (this.maxScroll > 0 && mouseX >= trackLeft && mouseX <= trackRight && mouseY >= trackTop && mouseY <= trackBottom) {
                this.draggingScrollbar = true;
                this.updateScrollFromMouse(mouseY, trackTop, trackBottom - trackTop);
                return true;
            }

            for (GuideRow row : this.contentRows) {
                if (row.visible() && row.onClick() != null
                        && isHovering(row.x(), row.currentY(), row.width(this.font), GuideLayout.ROW_HEIGHT, (int) mouseX, (int) mouseY)) {
                    row.onClick().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            int top = (this.height - GuideLayout.GUI_HEIGHT) / 2;
            int trackTop = top + GuideLayout.SCROLLBAR_TRACK_TOP;
            int trackBottom = top + GuideLayout.SCROLLBAR_TRACK_BOTTOM;
            this.updateScrollFromMouse(mouseY, trackTop, trackBottom - trackTop);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateScrollFromMouse(double mouseY, int trackTop, int trackHeight) {
        double usableTrack = trackHeight - GuideLayout.SCROLLER_HEIGHT;
        double fraction = usableTrack <= 0 ? 0 : (mouseY - trackTop - (GuideLayout.SCROLLER_HEIGHT / 2.0)) / usableTrack;
        fraction = Math.max(0, Math.min(1, fraction));
        this.scrollAmount = fraction * this.maxScroll;
        int top = (this.height - GuideLayout.GUI_HEIGHT) / 2;
        this.layoutRows(top);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}