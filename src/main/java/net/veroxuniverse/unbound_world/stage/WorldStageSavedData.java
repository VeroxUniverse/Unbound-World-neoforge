package net.veroxuniverse.unbound_world.stage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;

public class WorldStageSavedData extends SavedData {
    private static final String DATA_NAME = "unbound_world_progression";
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
        this.currentUnlockedOrder = order;
        StageManager.setUnlockedOrder(order);
        this.setDirty();

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.isCreative()) continue;

            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (StageManager.isItemLocked(itemId)) {
                        player.setItemSlot(slot, ItemStack.EMPTY);
                        if (!player.getInventory().add(stack)) {
                            player.drop(stack, false);
                        }
                        player.displayClientMessage(
                                Component.translatable("message.unbound_world.item_locked"),
                                true
                        );
                    }
                }
            }
            refreshPlayerEquipmentAttributes(player);
        }
    }

    public static void refreshPlayerEquipmentAttributes(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                stack.forEachModifier(slot, (attributeHolder, modifier) -> {
                    var instance = player.getAttributes().getInstance(attributeHolder);
                    if (instance != null) {
                        instance.removeModifier(modifier.id());
                    }
                });

                stack.forEachModifier(slot, (attributeHolder, modifier) -> {
                    var instance = player.getAttributes().getInstance(attributeHolder);
                    if (instance != null) {
                        instance.addTransientModifier(modifier);
                    }
                });
            }
        }

        if (player.connection != null) {
            player.connection.send(new ClientboundUpdateAttributesPacket(
                    player.getId(),
                    player.getAttributes().getSyncableAttributes()
            ));
        }

        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    public int getUnlockedOrder() {
        return this.currentUnlockedOrder;
    }

    public void setUnlockedOrder(int order) {
        this.currentUnlockedOrder = order;
        StageManager.setUnlockedOrder(order);
        this.setDirty();
    }
}