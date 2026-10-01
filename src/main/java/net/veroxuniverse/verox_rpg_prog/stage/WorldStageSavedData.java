package net.veroxuniverse.verox_rpg_prog.stage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_prog.api.event.StageUnlockedEvent;
import net.veroxuniverse.verox_rpg_prog.compat.curios.CuriosRestrictionHandler;
import net.veroxuniverse.verox_rpg_prog.network.SyncStagePayload;

import java.util.ArrayList;
import java.util.List;

public class WorldStageSavedData extends SavedData {
    private static final String DATA_NAME = "verox_rpg_prog_progression";
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private int currentUnlockedOrder = -1;

    public WorldStageSavedData() {
        super();
    }

    public static SavedData.Factory<WorldStageSavedData> factory() {
        return new SavedData.Factory<>(
                WorldStageSavedData::new,
                WorldStageSavedData::load,
                null
        );
    }

    public static WorldStageSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WorldStageSavedData data = new WorldStageSavedData();
        data.currentUnlockedOrder = tag.getInt("unlocked_order");
        StageManager.setUnlockedOrder(data.currentUnlockedOrder);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("unlocked_order", this.currentUnlockedOrder);
        return tag;
    }

    public static WorldStageSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public void setUnlockedOrder(ServerLevel level, int order) {
        int previousOrder = this.currentUnlockedOrder;

        this.currentUnlockedOrder = order;
        this.setDirty();
        StageManager.setUnlockedOrder(order);

        PacketDistributor.sendToAllPlayers(new SyncStagePayload(order));

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.isCreative()) continue;

            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.isEmpty()) continue;

                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!StageManager.isItemLocked(itemId)) continue;

                ItemAttributeModifiers itemModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                itemModifiers.forEach(slot, (attributeHolder, modifier) -> {
                    AttributeInstance instance = player.getAttributes().getInstance(attributeHolder);
                    if (instance != null) {
                        instance.removeModifier(modifier.id());
                    }
                });

                player.setItemSlot(slot, ItemStack.EMPTY);

                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }

                player.displayClientMessage(Component.translatable("message.verox_rpg_prog.item_locked"), true);
            }

            if (ModList.get().isLoaded("curios")) {
                CuriosRestrictionHandler.checkAndUnequipCurios(player);
            }

            refreshPlayerEquipmentAttributes(player);
        }

        if (previousOrder != order) {
            NeoForge.EVENT_BUS.post(new StageUnlockedEvent(level, previousOrder, order));
        }
    }

    public static void refreshPlayerEquipmentAttributes(ServerPlayer player) {
        AttributeInstance armorInstance = player.getAttributes().getInstance(Attributes.ARMOR);
        if (armorInstance != null) {
            armorInstance.getModifiers().stream().toList().forEach(modifier -> armorInstance.removeModifier(modifier.id()));
        }

        AttributeInstance toughnessInstance = player.getAttributes().getInstance(Attributes.ARMOR_TOUGHNESS);
        if (toughnessInstance != null) {
            toughnessInstance.getModifiers().stream().toList().forEach(modifier -> toughnessInstance.removeModifier(modifier.id()));
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (StageManager.isItemLocked(itemId)) continue;

            ItemAttributeModifiers itemModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            itemModifiers.forEach(slot, (attributeHolder, modifier) -> {
                AttributeInstance instance = player.getAttributes().getInstance(attributeHolder);
                if (instance != null && !instance.hasModifier(modifier.id())) {
                    instance.addTransientModifier(modifier);
                }
            });
        }

        if (player.connection != null) {
            List<AttributeInstance> attributesToSync = new ArrayList<>();
            if (armorInstance != null) attributesToSync.add(armorInstance);
            if (toughnessInstance != null) attributesToSync.add(toughnessInstance);

            player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), attributesToSync));
        }

        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    public int getUnlockedOrder() {
        return this.currentUnlockedOrder;
    }
}