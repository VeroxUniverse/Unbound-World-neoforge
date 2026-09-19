package net.veroxuniverse.unbound_world.client.gui.guide;

import net.minecraft.client.gui.Font;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class GuideViewBuilder {

    private GuideViewBuilder() {}

    public record BuildResult(List<GuideRow> rows, List<FormattedCharSequence> headerLines) {}

    public static BuildResult buildStageList(Font font, int left, int top, Consumer<StageDefinition> onSelectStage) {
        List<GuideRow> rows = new ArrayList<>();

        List<StageDefinition> sortedStages = StageManager.getAllStages().stream()
                .sorted(Comparator.comparingInt(StageDefinition::order))
                .toList();

        int currentOrder = StageManager.getUnlockedOrder();
        int baseY = top + GuideLayout.CONTENT_TOP_OFFSET;
        int rowX = left + GuideLayout.CONTENT_LEFT_OFFSET + 2;

        for (StageDefinition stage : sortedStages) {
            boolean isUnlocked = currentOrder >= stage.order();
            String prefix = isUnlocked ? "§a✔ §r" : "§c✖ §r";
            Component rowText = Component.literal(prefix).append(Component.translatable(stage.translationKey()));

            rows.add(GuideRow.clickableText(
                    rowX,
                    baseY,
                    rowText,
                    null,
                    () -> onSelectStage.accept(stage),
                    List.of(Component.translatable("gui.unbound_world.click_to_open"))
            ));

            baseY += GuideLayout.ROW_HEIGHT;
        }

        return new BuildResult(rows, List.of());
    }

    public static BuildResult buildStageDetail(Font font, int left, int top, StageDefinition stage, Consumer<StageDefinition.BossInfo> onSelectBoss) {
        List<GuideRow> rows = new ArrayList<>();
        List<FormattedCharSequence> headerLines = new ArrayList<>(
                font.split(Component.translatable(stage.translationKey() + ".summary"), GuideLayout.HEADER_TEXT_WIDTH)
        );

        int rowX = left + GuideLayout.CONTENT_LEFT_OFFSET;
        int baseY = top + GuideLayout.CONTENT_TOP_OFFSET;

        if (stage.mainBoss().isPresent()) {
            StageDefinition.BossInfo mainBoss = stage.mainBoss().get();
            Component rowText = Component.translatable("gui.unbound_world.main_boss_prefix").append(getBossDisplayName(mainBoss));
            ItemStack icon = resolveIcon(mainBoss.displayItem());

            rows.add(GuideRow.clickableText(
                    rowX,
                    baseY,
                    rowText,
                    icon,
                    () -> onSelectBoss.accept(mainBoss),
                    List.of(Component.translatable("gui.unbound_world.click_to_see_details"))
            ));

            baseY += GuideLayout.ROW_HEIGHT;
        }

        if (!stage.optionalBosses().isEmpty()) {
            rows.add(GuideRow.label(rowX, baseY, Component.translatable("gui.unbound_world.boss_progression_label")));
            baseY += GuideLayout.ROW_HEIGHT;
        }

        for (StageDefinition.BossInfo optBoss : stage.optionalBosses()) {
            Component bossDisplayName = getBossDisplayName(optBoss);
            ItemStack icon = resolveIcon(optBoss.displayItem());

            rows.add(GuideRow.clickableText(
                    rowX + 14,
                    baseY,
                    bossDisplayName,
                    icon,
                    () -> onSelectBoss.accept(optBoss),
                    List.of(Component.translatable("gui.unbound_world.click_to_see_details"))
            ));

            baseY += GuideLayout.ROW_HEIGHT;
        }

        List<GuideRow.IconEntry> lockedEntries = new ArrayList<>();
        List<Identifier> locked = new ArrayList<>();
        locked.addAll(stage.lockedItems());
        locked.addAll(stage.lockedBlocks());

        for (Identifier loc : locked) {
            Item item = BuiltInRegistries.ITEM.get(loc).map(Holder::value).orElse(Items.AIR);
            if (item != Items.AIR) {
                lockedEntries.add(new GuideRow.IconEntry(new ItemStack(item), List.of(Component.translatable("gui.unbound_world.locked_until_defeated"))));
            }
        }

        for (StageDefinition.OreDisguise ore : stage.lockedOres()) {
            Item item = BuiltInRegistries.ITEM.get(ore.oreBlock()).map(Holder::value).orElse(Items.AIR);
            if (item != Items.AIR) {
                lockedEntries.add(new GuideRow.IconEntry(new ItemStack(item), List.of(Component.translatable("gui.unbound_world.locked_ore_notice"))));
            }
        }

        if (!lockedEntries.isEmpty()) {
            baseY += 4;
            rows.add(GuideRow.label(rowX, baseY, Component.translatable("gui.unbound_world.sealed_items_label")));
            baseY += GuideLayout.ROW_HEIGHT;
            appendIconRows(rows, left, baseY, lockedEntries);
        }

        return new BuildResult(rows, headerLines);
    }

    public static BuildResult buildBossDrops(Font font, int left, int top, StageDefinition.BossInfo boss) {
        List<GuideRow> rows = new ArrayList<>();
        List<FormattedCharSequence> headerLines = new ArrayList<>();

        String locationKey = boss.locationTranslationKey();
        if (locationKey != null && !locationKey.isBlank()) {
            headerLines.addAll(font.split(Component.translatable(locationKey), GuideLayout.HEADER_TEXT_WIDTH));
        }

        // +2px: the drop grid sits directly under the header with no label row above it
        // (unlike the "Sealed Items & Blocks" grid, which already has its own spacing after
        // its label), so it needed its own small nudge down. Does not touch the scroll clip
        // box (CONTENT_TOP_OFFSET/CONTENT_BOTTOM_OFFSET) - only where content starts inside it.
        int baseY = top + GuideLayout.CONTENT_TOP_OFFSET + 2;

        String mode = boss.drops().mode();
        boolean includeVanilla = !"replace".equalsIgnoreCase(mode);
        boolean includeCustom = !"none".equalsIgnoreCase(mode);

        List<GuideRow.IconEntry> entries = new ArrayList<>();

        if (includeVanilla) {
            entries.addAll(BossLootPreview.resolve(boss.entityId(), boss.guaranteedDrops()));
        }

        if (includeCustom) {
            for (StageDefinition.DropEntry drop : boss.drops().drops()) {
                Item item = BuiltInRegistries.ITEM.get(drop.item()).map(Holder::value).orElse(Items.AIR);
                if (item != Items.AIR) {
                    int count = drop.countMin() == drop.countMax() ? drop.countMin() : drop.countMax();
                    ItemStack stack = new ItemStack(item, Math.max(1, count));

                    List<Component> tooltip = List.of(
                            Component.translatable("gui.unbound_world.chance_label", (int) (drop.chance() * 100)),
                            Component.translatable("gui.unbound_world.amount_label", drop.countMin(), drop.countMax())
                    );

                    entries.add(new GuideRow.IconEntry(stack, tooltip));
                }
            }
        }

        appendIconRows(rows, left, baseY, entries);

        return new BuildResult(rows, headerLines);
    }

    private static void appendIconRows(List<GuideRow> rows, int left, int baseY, List<GuideRow.IconEntry> entries) {
        for (int i = 0; i < entries.size(); i += GuideLayout.ICONS_PER_ROW) {
            List<GuideRow.IconEntry> chunk = new ArrayList<>(entries.subList(i, Math.min(i + GuideLayout.ICONS_PER_ROW, entries.size())));
            rows.add(GuideRow.iconStrip(left + GuideLayout.ICON_INDENT_LEFT, baseY, chunk));
            baseY += GuideLayout.ROW_HEIGHT;
        }
    }

    private static ItemStack resolveIcon(Identifier displayItem) {
        Item iconItem = BuiltInRegistries.ITEM.get(displayItem).map(Holder::value).orElse(null);
        return iconItem != null ? new ItemStack(iconItem) : new ItemStack(Items.BARRIER);
    }

    public static Component getBossDisplayName(StageDefinition.BossInfo bossInfo) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(bossInfo.entityId()).map(Holder::value).orElse(null);
        return entityType != null ? entityType.getDescription() : Component.literal(bossInfo.entityId().toString());
    }
}