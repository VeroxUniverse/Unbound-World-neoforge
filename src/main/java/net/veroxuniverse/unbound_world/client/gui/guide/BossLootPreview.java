package net.veroxuniverse.unbound_world.client.gui.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BossLootPreview {

    private BossLootPreview() {}

    public static List<GuideRow.IconEntry> resolve(ResourceLocation entityId) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return List.of();
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        ServerLevel serverLevel = server.overworld();
        Entity previewEntity = entityType.create(serverLevel);
        if (previewEntity == null) {
            return List.of();
        }

        ResourceKey<LootTable> lootTableKey = entityType.getDefaultLootTable();
        LootTable lootTable = server.reloadableRegistries().getLootTable(lootTableKey);
        if (lootTable == LootTable.EMPTY) {
            return List.of();
        }

        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, previewEntity.position())
                .withParameter(LootContextParams.THIS_ENTITY, previewEntity)
                .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().generic())
                .create(LootContextParamSets.ENTITY);

        Map<Item, Integer> occurrences = new LinkedHashMap<>();
        Map<Item, Integer> totalCount = new LinkedHashMap<>();

        for (int i = 0; i < GuideLayout.LOOT_SAMPLE_COUNT; i++) {
            List<ItemStack> rolled = lootTable.getRandomItems(params);
            for (ItemStack stack : rolled) {
                if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;
                occurrences.merge(stack.getItem(), 1, Integer::sum);
                totalCount.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }

        List<GuideRow.IconEntry> result = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : occurrences.entrySet()) {
            Item item = entry.getKey();
            double chance = entry.getValue() / (double) GuideLayout.LOOT_SAMPLE_COUNT;
            double avgCount = totalCount.get(item) / (double) GuideLayout.LOOT_SAMPLE_COUNT;
            ItemStack display = new ItemStack(item, Math.max(1, (int) Math.round(avgCount)));

            List<Component> tooltip = List.of(
                    Component.translatable("gui.unbound_world.chance_label_approx", Math.round(chance * 100)),
                    Component.translatable("gui.unbound_world.avg_amount_label", String.format("%.1f", avgCount))
            );

            result.add(new GuideRow.IconEntry(display, tooltip));
        }
        return result;
    }
}