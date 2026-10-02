package net.veroxuniverse.verox_rpg_prog.client.gui.book;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.veroxuniverse.verox_rpg_prog.client.gui.guide.BossLootPreview;
import net.veroxuniverse.verox_rpg_prog.client.gui.guide.GuideRow;
import net.veroxuniverse.verox_rpg_prog.client.key.ModKeyMappings;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StageBookScreen extends Screen {

    private enum View { LANDING, STAGE, BOSS }

    private enum StageStatus { CLEARED, CURRENT, LOCKED }

    @FunctionalInterface
    private interface Page {
        void render(GuiGraphics graphics, int pageX, int mouseX, int mouseY);
    }

    private record Region(int x, int y, int width, int height, Runnable action) {
        boolean contains(double px, double py) {
            return px >= this.x && px < this.x + this.width && py >= this.y && py < this.y + this.height;
        }
    }

    private record IconEntry(ItemStack stack, List<Component> extraTooltip) {}

    private record Line(FormattedCharSequence text, ItemStack icon, int color, int height, boolean centered, int indent) {
        static Line text(FormattedCharSequence text, int color) {
            return new Line(text, ItemStack.EMPTY, color, BookLayout.LINE_HEIGHT, false, 0);
        }

        static Line indented(FormattedCharSequence text, int color, int indent) {
            return new Line(text, ItemStack.EMPTY, color, BookLayout.LINE_HEIGHT, false, indent);
        }

        static Line centered(FormattedCharSequence text, int color) {
            return new Line(text, ItemStack.EMPTY, color, BookLayout.LINE_HEIGHT, true, 0);
        }

        static Line icon(ItemStack icon) {
            return new Line(null, icon, 0, BookLayout.ICON_ROW_HEIGHT + 4, true, 0);
        }

        static Line gap(int height) {
            return new Line(null, ItemStack.EMPTY, 0, height, false, 0);
        }
    }

    private record BossElement(StageDefinition.BossInfo boss, Component label, Component section) {
        static BossElement label(Component label) {
            return new BossElement(null, label, label);
        }

        static BossElement boss(StageDefinition.BossInfo boss, Component section) {
            return new BossElement(boss, null, section);
        }

        int height() {
            return this.boss != null ? BookLayout.ICON_ROW_HEIGHT : BookLayout.LINE_HEIGHT + 4;
        }
    }

    private final List<Region> regions = new ArrayList<>();
    private View view = View.LANDING;
    private int listPage;
    private List<Page> pages = List.of();
    private int spread;
    private int stageSpreadBeforeBoss;
    private StageDefinition selectedStage;
    private int bookLeft;
    private int bookTop;
    private List<Component> pendingTooltip;
    private ItemStack pendingItemTooltip = ItemStack.EMPTY;

    public StageBookScreen() {
        super(Component.translatable("gui.verox_rpg_prog.book_title"));
    }

    @Override
    protected void init() {
        this.bookLeft = this.width / 2 - BookLayout.FULL_WIDTH / 2;
        this.bookTop = this.height / 2 - BookLayout.FULL_HEIGHT / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BookLayout.TEXTURE, this.bookLeft, this.bookTop, 0, 0,
                BookLayout.FULL_WIDTH, BookLayout.FULL_HEIGHT, BookLayout.TEXTURE_WIDTH, BookLayout.TEXTURE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        this.regions.clear();
        this.pendingTooltip = null;
        this.pendingItemTooltip = ItemStack.EMPTY;

        int relativeX = mouseX - this.bookLeft;
        int relativeY = mouseY - this.bookTop;

        graphics.pose().pushPose();
        graphics.pose().translate(this.bookLeft, this.bookTop, 0);

        if (this.view == View.LANDING) {
            this.renderLanding(graphics, relativeX, relativeY);
        } else {
            this.renderPages(graphics, relativeX, relativeY);
        }

        graphics.pose().popPose();

        if (!this.pendingItemTooltip.isEmpty()) {
            List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(this.minecraft, this.pendingItemTooltip));
            if (this.pendingTooltip != null) {
                lines.addAll(this.pendingTooltip);
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        } else if (this.pendingTooltip != null) {
            graphics.renderComponentTooltip(this.font, this.pendingTooltip, mouseX, mouseY);
        }
    }

    private void renderLanding(GuiGraphics graphics, int mouseX, int mouseY) {
        List<StageDefinition> stages = sortedStages();
        int currentOrder = StageManager.getUnlockedOrder();

        this.drawTexture(graphics, BookLayout.TITLE_PLATE_X, BookLayout.TITLE_PLATE_Y,
                BookLayout.TITLE_PLATE_U, BookLayout.TITLE_PLATE_V, BookLayout.TITLE_PLATE_WIDTH, BookLayout.TITLE_PLATE_HEIGHT);
        graphics.drawString(this.font, this.title, 13, 16, BookLayout.NAMEPLATE_COLOR, false);
        graphics.drawString(this.font, currentStageName(stages, currentOrder), 24, 24, BookLayout.NAMEPLATE_COLOR, false);

        int y = BookLayout.TITLE_PLATE_Y + BookLayout.TITLE_PLATE_HEIGHT + 8;
        long clearedCount = stages.stream().filter(stage -> stage.order() <= currentOrder).count();
        graphics.drawString(this.font, Component.translatable("gui.verox_rpg_prog.book.cleared_count", clearedCount, stages.size()),
                BookLayout.LEFT_PAGE_X, y, BookLayout.TEXT_COLOR, false);
        y += BookLayout.LINE_HEIGHT + 4;

        this.drawSeparator(graphics, BookLayout.LEFT_PAGE_X, y);
        y += 8;

        for (FormattedCharSequence line : this.font.split(Component.translatable("gui.verox_rpg_prog.book.intro"), BookLayout.PAGE_WIDTH)) {
            if (y + BookLayout.LINE_HEIGHT > BookLayout.CONTENT_BOTTOM) break;
            graphics.drawString(this.font, line, BookLayout.LEFT_PAGE_X, y, BookLayout.TEXT_COLOR, false);
            y += BookLayout.LINE_HEIGHT;
        }

        this.drawPageHeader(graphics, BookLayout.RIGHT_PAGE_X, Component.translatable("gui.verox_rpg_prog.book.stages"));

        if (stages.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("gui.verox_rpg_prog.book.no_stages"),
                    BookLayout.RIGHT_PAGE_X, BookLayout.CONTENT_TOP, BookLayout.MUTED_COLOR, false);
            return;
        }

        int entriesPerPage = (BookLayout.CONTENT_BOTTOM - BookLayout.CONTENT_TOP) / BookLayout.ENTRY_HEIGHT;
        int pageCount = Math.max(1, (stages.size() + entriesPerPage - 1) / entriesPerPage);
        this.listPage = Math.min(this.listPage, pageCount - 1);

        int entryY = BookLayout.CONTENT_TOP;
        int start = this.listPage * entriesPerPage;
        int end = Math.min(stages.size(), start + entriesPerPage);

        for (int i = start; i < end; i++) {
            StageDefinition stage = stages.get(i);
            this.renderStageEntry(graphics, stage, statusOf(stage, currentOrder), BookLayout.RIGHT_PAGE_X, entryY, mouseX, mouseY);
            entryY += BookLayout.ENTRY_HEIGHT;
        }

        if (this.listPage > 0) {
            this.drawArrow(graphics, true, mouseX, mouseY, () -> this.listPage--);
        }
        if (this.listPage < pageCount - 1) {
            this.drawArrow(graphics, false, mouseX, mouseY, () -> this.listPage++);
        }
    }

    private void renderStageEntry(GuiGraphics graphics, StageDefinition stage, StageStatus status, int x, int y, int mouseX, int mouseY) {
        Region region = new Region(x, y, BookLayout.PAGE_WIDTH, BookLayout.ENTRY_HEIGHT - 1, () -> this.openStage(stage));
        if (region.contains(mouseX, mouseY)) {
            graphics.fill(x - 1, y - 1, x + BookLayout.PAGE_WIDTH, y + BookLayout.ENTRY_HEIGHT - 1, BookLayout.HOVER_FILL);
            this.pendingTooltip = List.of(statusComponent(status), Component.translatable("gui.verox_rpg_prog.click_to_open"));
        }

        this.drawTexture(graphics, x, y, markerU(status), BookLayout.MARKER_V, BookLayout.MARKER_SIZE, BookLayout.MARKER_SIZE);

        int color = status == StageStatus.LOCKED ? BookLayout.MUTED_COLOR : BookLayout.TEXT_COLOR;
        String name = Component.translatable(stage.translationKey()).getString();
        graphics.drawString(this.font, this.font.plainSubstrByWidth(name, BookLayout.PAGE_WIDTH - 12), x + 11, y, color, false);

        this.regions.add(region);
    }

    private void renderPages(GuiGraphics graphics, int mouseX, int mouseY) {
        int leftIndex = this.spread * 2;
        int rightIndex = leftIndex + 1;

        if (leftIndex < this.pages.size()) {
            this.pages.get(leftIndex).render(graphics, BookLayout.LEFT_PAGE_X, mouseX, mouseY);
        }
        if (rightIndex < this.pages.size()) {
            this.pages.get(rightIndex).render(graphics, BookLayout.RIGHT_PAGE_X, mouseX, mouseY);
        }

        int spreadCount = Math.max(1, (this.pages.size() + 1) / 2);
        if (this.spread > 0) {
            this.drawArrow(graphics, true, mouseX, mouseY, () -> this.spread--);
        }
        if (this.spread < spreadCount - 1) {
            this.drawArrow(graphics, false, mouseX, mouseY, () -> this.spread++);
        }

        this.drawBackButton(graphics, mouseX, mouseY, this.view == View.BOSS ? this::returnToStage : this::returnToLanding);
    }

    private List<Page> buildStagePages(StageDefinition stage) {
        List<Page> result = new ArrayList<>();
        StageStatus status = statusOf(stage, StageManager.getUnlockedOrder());

        List<Line> overview = new ArrayList<>();
        overview.add(Line.centered(statusComponent(status).getVisualOrderText(), statusColor(status)));
        overview.add(Line.gap(6));
        for (FormattedCharSequence line : this.font.split(Component.translatable(stage.translationKey() + ".summary"), BookLayout.PAGE_WIDTH)) {
            overview.add(Line.text(line, BookLayout.TEXT_COLOR));
        }
        if (!stage.lockedDimensions().isEmpty()) {
            overview.add(Line.gap(8));
            overview.add(Line.text(subheader("gui.verox_rpg_prog.book.sealed_dimensions").getVisualOrderText(), BookLayout.HEADER_COLOR));
            overview.add(Line.gap(2));
            for (ResourceLocation dimension : stage.lockedDimensions()) {
                overview.add(Line.indented(dimensionName(dimension).getVisualOrderText(), BookLayout.TEXT_COLOR, 4));
            }
        }
        if (!stage.territories().isEmpty()) {
            overview.add(Line.gap(8));
            overview.add(Line.text(subheader("gui.verox_rpg_prog.book.territories").getVisualOrderText(), BookLayout.HEADER_COLOR));
            overview.add(Line.gap(2));
            for (StageDefinition.Territory territory : stage.territories()) {
                for (String structure : territory.structures()) {
                    overview.add(Line.indented(territoryEntryName(structure, "structure").getVisualOrderText(), BookLayout.TEXT_COLOR, 4));
                }
                for (String biome : territory.biomes()) {
                    overview.add(Line.indented(territoryEntryName(biome, "biome").getVisualOrderText(), BookLayout.TEXT_COLOR, 4));
                }
            }
        }
        result.addAll(this.paginateLines(Component.translatable(stage.translationKey()), overview));

        List<BossElement> bosses = new ArrayList<>();
        if (stage.mainBoss().isPresent()) {
            Component mainSection = subheader("gui.verox_rpg_prog.book.main_boss");
            bosses.add(BossElement.label(mainSection));
            bosses.add(BossElement.boss(stage.mainBoss().get(), mainSection));
        }
        if (!stage.optionalBosses().isEmpty()) {
            Component optionalSection = subheader("gui.verox_rpg_prog.book.optional_bosses");
            bosses.add(BossElement.label(optionalSection));
            for (StageDefinition.BossInfo boss : stage.optionalBosses()) {
                bosses.add(BossElement.boss(boss, optionalSection));
            }
        }
        if (!bosses.isEmpty()) {
            result.addAll(this.paginateBosses(bosses));
        }

        List<IconEntry> sealed = sealedEntries(stage);
        if (!sealed.isEmpty()) {
            result.addAll(this.paginateGrid(Component.translatable("gui.verox_rpg_prog.sealed_items_label"), sealed));
        }

        return result;
    }

    private List<Page> buildBossPages(StageDefinition.BossInfo boss) {
        List<Page> result = new ArrayList<>();

        List<Line> info = new ArrayList<>();
        info.add(Line.icon(resolveItem(boss.displayItem())));
        String locationKey = boss.locationTranslationKey();
        if (locationKey != null && !locationKey.isBlank()) {
            info.add(Line.text(subheader("gui.verox_rpg_prog.book.location").getVisualOrderText(), BookLayout.HEADER_COLOR));
            info.add(Line.gap(2));
            for (FormattedCharSequence line : this.font.split(Component.translatable(locationKey), BookLayout.PAGE_WIDTH)) {
                info.add(Line.text(line, BookLayout.TEXT_COLOR));
            }
        }
        result.addAll(this.paginateLines(bossName(boss), info));

        List<IconEntry> drops = dropEntries(boss);
        if (drops.isEmpty()) {
            List<Line> empty = List.of(Line.text(Component.translatable("gui.verox_rpg_prog.book.no_drops").getVisualOrderText(), BookLayout.MUTED_COLOR));
            result.addAll(this.paginateLines(Component.translatable("gui.verox_rpg_prog.book.drops"), empty));
        } else {
            result.addAll(this.paginateGrid(Component.translatable("gui.verox_rpg_prog.book.drops"), drops));
        }

        return result;
    }

    private List<Page> paginateLines(Component header, List<Line> lines) {
        List<Page> result = new ArrayList<>();
        List<Line> current = new ArrayList<>();
        int used = 0;
        int capacity = BookLayout.CONTENT_BOTTOM - BookLayout.CONTENT_TOP;

        for (Line line : lines) {
            if (used + line.height() > capacity && !current.isEmpty()) {
                result.add(this.linePage(header, List.copyOf(current)));
                current.clear();
                used = 0;
                if (line.text() == null && line.icon().isEmpty()) continue;
            }
            current.add(line);
            used += line.height();
        }
        if (!current.isEmpty() || result.isEmpty()) {
            result.add(this.linePage(header, List.copyOf(current)));
        }
        return result;
    }

    private Page linePage(Component header, List<Line> lines) {
        return (graphics, pageX, mouseX, mouseY) -> {
            this.drawPageHeader(graphics, pageX, header);
            int y = BookLayout.CONTENT_TOP;

            for (Line line : lines) {
                if (!line.icon().isEmpty()) {
                    graphics.renderItem(line.icon(), pageX + BookLayout.PAGE_WIDTH / 2 - 8, y);
                } else if (line.text() != null) {
                    int x = line.centered()
                            ? pageX + BookLayout.PAGE_WIDTH / 2 - this.font.width(line.text()) / 2
                            : pageX + line.indent();
                    graphics.drawString(this.font, line.text(), x, y, line.color(), false);
                }
                y += line.height();
            }
        };
    }

    private List<Page> paginateBosses(List<BossElement> elements) {
        List<Page> result = new ArrayList<>();
        List<BossElement> current = new ArrayList<>();
        int used = 0;
        int capacity = BookLayout.CONTENT_BOTTOM - BookLayout.CONTENT_TOP;

        for (BossElement element : elements) {
            boolean isLabel = element.boss() == null;
            int needed = isLabel ? element.height() + BookLayout.ICON_ROW_HEIGHT : element.height();

            if (used + needed > capacity && !current.isEmpty()) {
                result.add(this.bossPage(List.copyOf(current)));
                current.clear();
                used = 0;

                if (!isLabel) {
                    BossElement continuedLabel = BossElement.label(element.section());
                    current.add(continuedLabel);
                    used += continuedLabel.height();
                }
            }
            current.add(element);
            used += element.height();
        }
        if (!current.isEmpty()) {
            result.add(this.bossPage(List.copyOf(current)));
        }
        return result;
    }

    private Page bossPage(List<BossElement> elements) {
        Component header = Component.translatable("gui.verox_rpg_prog.book.bosses");
        return (graphics, pageX, mouseX, mouseY) -> {
            this.drawPageHeader(graphics, pageX, header);
            int y = BookLayout.CONTENT_TOP;

            for (BossElement element : elements) {
                if (element.boss() != null) {
                    this.renderBossRow(graphics, element.boss(), pageX, y, mouseX, mouseY);
                } else {
                    graphics.drawString(this.font, element.label(), pageX, y + 2, BookLayout.HEADER_COLOR, false);
                }
                y += element.height();
            }
        };
    }

    private void renderBossRow(GuiGraphics graphics, StageDefinition.BossInfo boss, int x, int y, int mouseX, int mouseY) {
        Region region = new Region(x, y, BookLayout.PAGE_WIDTH, BookLayout.ICON_ROW_HEIGHT, () -> this.openBoss(boss));
        if (region.contains(mouseX, mouseY)) {
            graphics.fill(x - 1, y - 1, x + BookLayout.PAGE_WIDTH, y + BookLayout.ICON_ROW_HEIGHT - 1, BookLayout.HOVER_FILL);
            this.pendingTooltip = List.of(Component.translatable("gui.verox_rpg_prog.click_to_see_details"));
        }

        graphics.renderItem(resolveItem(boss.displayItem()), x, y);
        String name = bossName(boss).getString();
        graphics.drawString(this.font, this.font.plainSubstrByWidth(name, BookLayout.PAGE_WIDTH - 20), x + 19, y + 4, BookLayout.TEXT_COLOR, false);

        this.regions.add(region);
    }

    private List<Page> paginateGrid(Component header, List<IconEntry> entries) {
        int perRow = BookLayout.PAGE_WIDTH / BookLayout.ICON_ROW_HEIGHT;
        int rows = (BookLayout.CONTENT_BOTTOM - BookLayout.CONTENT_TOP) / BookLayout.ICON_ROW_HEIGHT;
        int perPage = Math.max(1, perRow * rows);

        List<Page> result = new ArrayList<>();
        for (int start = 0; start < entries.size(); start += perPage) {
            List<IconEntry> chunk = List.copyOf(entries.subList(start, Math.min(entries.size(), start + perPage)));
            result.add((graphics, pageX, mouseX, mouseY) -> {
                this.drawPageHeader(graphics, pageX, header);
                this.renderIconGrid(graphics, chunk, pageX, BookLayout.CONTENT_TOP, perRow, mouseX, mouseY);
            });
        }
        return result;
    }

    private void renderIconGrid(GuiGraphics graphics, List<IconEntry> entries, int x, int y, int perRow, int mouseX, int mouseY) {
        for (int i = 0; i < entries.size(); i++) {
            IconEntry entry = entries.get(i);
            int iconX = x + (i % perRow) * BookLayout.ICON_ROW_HEIGHT;
            int iconY = y + (i / perRow) * BookLayout.ICON_ROW_HEIGHT;

            graphics.renderItem(entry.stack(), iconX, iconY);
            graphics.renderItemDecorations(this.font, entry.stack(), iconX, iconY);

            if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
                this.pendingItemTooltip = entry.stack();
                this.pendingTooltip = entry.extraTooltip();
            }
        }
    }

    private void drawArrow(GuiGraphics graphics, boolean left, int mouseX, int mouseY, Runnable action) {
        int x = left ? BookLayout.ARROW_LEFT_X : BookLayout.ARROW_RIGHT_X;
        Region region = new Region(x, BookLayout.ARROW_Y, BookLayout.ARROW_WIDTH, BookLayout.ARROW_HEIGHT, action);
        boolean hovered = region.contains(mouseX, mouseY);
        int u = BookLayout.ARROW_U + (hovered ? BookLayout.ARROW_WIDTH : 0);
        int v = left ? BookLayout.ARROW_LEFT_V : BookLayout.ARROW_RIGHT_V;

        this.drawTexture(graphics, x, BookLayout.ARROW_Y, u, v, BookLayout.ARROW_WIDTH, BookLayout.ARROW_HEIGHT);
        this.regions.add(region);
    }

    private void drawBackButton(GuiGraphics graphics, int mouseX, int mouseY, Runnable action) {
        Region region = new Region(BookLayout.BACK_X, BookLayout.BACK_Y, BookLayout.BACK_WIDTH, BookLayout.BACK_HEIGHT, action);
        boolean hovered = region.contains(mouseX, mouseY);
        int u = BookLayout.BACK_U + (hovered ? BookLayout.BACK_WIDTH : 0);

        this.drawTexture(graphics, BookLayout.BACK_X, BookLayout.BACK_Y, u, BookLayout.BACK_V, BookLayout.BACK_WIDTH, BookLayout.BACK_HEIGHT);
        if (hovered) {
            this.pendingTooltip = List.of(Component.translatable("gui.verox_rpg_prog.click_to_go_back"));
        }
        this.regions.add(region);
    }

    private void drawPageHeader(GuiGraphics graphics, int pageX, Component text) {
        String clipped = this.font.plainSubstrByWidth(text.getString(), BookLayout.PAGE_WIDTH);
        int centerX = pageX + BookLayout.PAGE_WIDTH / 2;
        graphics.drawString(this.font, clipped, centerX - this.font.width(clipped) / 2, BookLayout.TOP_PADDING, BookLayout.HEADER_COLOR, false);
        this.drawSeparator(graphics, pageX, BookLayout.TOP_PADDING + 12);
    }

    private void drawSeparator(GuiGraphics graphics, int pageX, int y) {
        int x = pageX + BookLayout.PAGE_WIDTH / 2 - BookLayout.SEPARATOR_WIDTH / 2;
        this.drawTexture(graphics, x, y, BookLayout.SEPARATOR_U, BookLayout.SEPARATOR_V, BookLayout.SEPARATOR_WIDTH, BookLayout.SEPARATOR_HEIGHT);
    }

    private void drawTexture(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(BookLayout.TEXTURE, x, y, u, v, width, height, BookLayout.TEXTURE_WIDTH, BookLayout.TEXTURE_HEIGHT);
    }

    private void openStage(StageDefinition stage) {
        this.selectedStage = stage;
        this.pages = this.buildStagePages(stage);
        this.spread = 0;
        this.view = View.STAGE;
    }

    private void openBoss(StageDefinition.BossInfo boss) {
        this.stageSpreadBeforeBoss = this.spread;
        this.pages = this.buildBossPages(boss);
        this.spread = 0;
        this.view = View.BOSS;
    }

    private void returnToStage() {
        this.pages = this.buildStagePages(this.selectedStage);
        this.spread = this.stageSpreadBeforeBoss;
        this.view = View.STAGE;
    }

    private void returnToLanding() {
        this.pages = List.of();
        this.spread = 0;
        this.view = View.LANDING;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double relativeX = mouseX - this.bookLeft;
            double relativeY = mouseY - this.bookTop;

            for (Region region : List.copyOf(this.regions)) {
                if (region.contains(relativeX, relativeY)) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                    region.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyMappings.OPEN_STAGE_GUIDE.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static Component subheader(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.UNDERLINE);
    }

    private static Component statusComponent(StageStatus status) {
        return switch (status) {
            case CLEARED -> Component.translatable("gui.verox_rpg_prog.book.status.cleared");
            case CURRENT -> Component.translatable("gui.verox_rpg_prog.book.status.current");
            case LOCKED -> Component.translatable("gui.verox_rpg_prog.book.status.locked");
        };
    }

    private static StageStatus statusOf(StageDefinition stage, int currentOrder) {
        if (stage.order() <= currentOrder) return StageStatus.CLEARED;
        if (stage.order() == currentOrder + 1) return StageStatus.CURRENT;
        return StageStatus.LOCKED;
    }

    private static int statusColor(StageStatus status) {
        return switch (status) {
            case CLEARED -> BookLayout.CLEARED_COLOR;
            case CURRENT -> BookLayout.CURRENT_COLOR;
            case LOCKED -> BookLayout.LOCKED_COLOR;
        };
    }

    private static int markerU(StageStatus status) {
        return switch (status) {
            case CLEARED -> BookLayout.MARKER_CLEARED_U;
            case CURRENT -> BookLayout.MARKER_CURRENT_U;
            case LOCKED -> BookLayout.MARKER_LOCKED_U;
        };
    }

    private static List<StageDefinition> sortedStages() {
        return StageManager.getAllStages().stream()
                .sorted(Comparator.comparingInt(StageDefinition::order))
                .toList();
    }

    private static Component currentStageName(List<StageDefinition> stages, int currentOrder) {
        Optional<StageDefinition> current = stages.stream().filter(stage -> stage.order() == currentOrder).findFirst();
        return current.<Component>map(stage -> Component.translatable(stage.translationKey()))
                .orElse(Component.translatable("gui.verox_rpg_prog.sealed_world"));
    }

    private static Component bossName(StageDefinition.BossInfo boss) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(boss.entityId());
        return type.getDescription();
    }

    private static Component dimensionName(ResourceLocation dimension) {
        String fallback = dimension.getPath().replace('_', ' ');
        fallback = fallback.isEmpty() ? fallback : Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);
        return Component.translatableWithFallback("dimension." + dimension.getNamespace() + "." + dimension.getPath(), fallback);
    }

    private static Component territoryEntryName(String entry, String category) {
        boolean isTag = entry.startsWith("#");
        ResourceLocation id = ResourceLocation.tryParse(isTag ? entry.substring(1) : entry);
        if (id == null) {
            return Component.literal(entry);
        }

        String fallback = id.getPath().substring(id.getPath().lastIndexOf('/') + 1).replace('_', ' ');
        fallback = fallback.isEmpty() ? fallback : Character.toUpperCase(fallback.charAt(0)) + fallback.substring(1);

        if (isTag) {
            return Component.literal(fallback);
        }
        return Component.translatableWithFallback(category + "." + id.getNamespace() + "." + id.getPath(), fallback);
    }

    private static ItemStack resolveItem(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.BARRIER);
    }

    private static List<IconEntry> sealedEntries(StageDefinition stage) {
        List<IconEntry> entries = new ArrayList<>();
        List<Component> lockedTooltip = List.of(Component.translatable("gui.verox_rpg_prog.locked_until_defeated"));

        for (ResourceLocation id : stage.lockedItems()) {
            addItem(entries, id, lockedTooltip);
        }
        for (ResourceLocation id : stage.lockedBlocks()) {
            addItem(entries, id, lockedTooltip);
        }
        List<Component> oreTooltip = List.of(Component.translatable("gui.verox_rpg_prog.locked_ore_notice"));
        for (StageDefinition.OreDisguise ore : stage.lockedOres()) {
            addItem(entries, ore.oreBlock(), oreTooltip);
        }
        return entries;
    }

    private static List<IconEntry> dropEntries(StageDefinition.BossInfo boss) {
        List<IconEntry> entries = new ArrayList<>();
        String mode = boss.drops().mode();

        if (!"replace".equalsIgnoreCase(mode)) {
            for (GuideRow.IconEntry preview : BossLootPreview.resolve(boss.entityId(), boss.guaranteedDrops())) {
                entries.add(new IconEntry(preview.stack(), preview.tooltip()));
            }
        }

        if (!"none".equalsIgnoreCase(mode)) {
            for (StageDefinition.DropEntry drop : boss.drops().drops()) {
                Item item = BuiltInRegistries.ITEM.get(drop.item());
                if (item == Items.AIR) continue;

                ItemStack stack = new ItemStack(item, Math.max(1, drop.countMax()));
                entries.add(new IconEntry(stack, List.of(
                        Component.translatable("gui.verox_rpg_prog.chance_label", (int) (drop.chance() * 100)),
                        Component.translatable("gui.verox_rpg_prog.amount_label", drop.countMin(), drop.countMax())
                )));
            }
        }
        return entries;
    }

    private static void addItem(List<IconEntry> entries, ResourceLocation id, List<Component> tooltip) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item != Items.AIR) {
            entries.add(new IconEntry(new ItemStack(item), tooltip));
        }
    }
}