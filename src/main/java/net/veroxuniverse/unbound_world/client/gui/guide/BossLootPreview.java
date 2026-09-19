package net.veroxuniverse.unbound_world.client.gui.guide;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.veroxuniverse.unbound_world.UnboundWorld;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BossLootPreview {

    private BossLootPreview() {}

    public static List<GuideRow.IconEntry> resolve(ResourceLocation entityId, List<ResourceLocation> guaranteedDrops) {
        List<GuideRow.IconEntry> result = new ArrayList<>();

        for (ResourceLocation guaranteedId : guaranteedDrops) {
            Item item = BuiltInRegistries.ITEM.get(guaranteedId);
            if (item != null && item != Items.AIR) {
                result.add(new GuideRow.IconEntry(new ItemStack(item), List.of(
                        Component.translatable("gui.unbound_world.guaranteed_drop_label")
                )));
            }
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            UnboundWorld.LOGGER.warn("BossLootPreview: no local server available, skipping loot table preview for {}", entityId);
            return result;
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        ServerLevel serverLevel = server.overworld();
        Entity previewEntity = entityType.create(serverLevel);
        if (previewEntity == null) {
            UnboundWorld.LOGGER.warn("BossLootPreview: could not create preview entity for {}", entityId);
            return result;
        }

        ResourceKey<LootTable> lootTableKey = entityType.getDefaultLootTable();
        LootTable lootTable = server.reloadableRegistries().getLootTable(lootTableKey);
        if (lootTable == LootTable.EMPTY) {
            UnboundWorld.LOGGER.warn("BossLootPreview: loot table {} resolved to EMPTY for {}", lootTableKey.location(), entityId);
            return result;
        }

        ServerPlayer previewKiller = server.getPlayerList().getPlayers().stream().findFirst().orElse(null);

        DamageSource damageSource = previewKiller != null
                ? serverLevel.damageSources().playerAttack(previewKiller)
                : serverLevel.damageSources().generic();

        LootParams.Builder paramsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, previewEntity.position())
                .withParameter(LootContextParams.THIS_ENTITY, previewEntity)
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource);

        if (previewKiller != null) {
            paramsBuilder.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, previewKiller);
            paramsBuilder.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, previewKiller);
            paramsBuilder.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, previewKiller);
        }

        LootParams params = paramsBuilder.create(LootContextParamSets.ENTITY);

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

        if (occurrences.isEmpty() && guaranteedDrops.isEmpty()) {
            UnboundWorld.LOGGER.warn("BossLootPreview: loot table {} produced zero items across {} samples for {}", lootTableKey.location(), GuideLayout.LOOT_SAMPLE_COUNT, entityId);
        }

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