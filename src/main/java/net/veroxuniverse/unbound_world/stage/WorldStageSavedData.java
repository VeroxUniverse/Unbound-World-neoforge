package net.veroxuniverse.unbound_world.stage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.veroxuniverse.unbound_world.compat.curios.CuriosRestrictionHandler;
import net.veroxuniverse.unbound_world.network.SyncStagePayload;

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
        this.setDirty();

        PacketDistributor.sendToAllPlayers(new SyncStagePayload(order));

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.isCreative()) continue;

            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (StageManager.isItemLocked(itemId)) {
                        ItemAttributeModifiers itemModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                        itemModifiers.forEach(slot, (attributeHolder, modifier) -> {
                            var inst = player.getAttributes().getInstance(attributeHolder);
                            if (inst != null) {
                                inst.removeModifier(modifier.id());
                            }
                        });

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

            if (ModList.get().isLoaded("curios")) {
                CuriosRestrictionHandler.checkAndUnequipCurios(player);
            }

            refreshPlayerEquipmentAttributes(player);
        }
    }

    public static void refreshPlayerEquipmentAttributes(ServerPlayer player) {
        var armorInst = player.getAttributes().getInstance(Attributes.ARMOR);
        if (armorInst != null) {
            armorInst.getModifiers().stream().toList().forEach(mod -> armorInst.removeModifier(mod.id()));
        }

        var toughnessInst = player.getAttributes().getInstance(Attributes.ARMOR_TOUGHNESS);
        if (toughnessInst != null) {
            toughnessInst.getModifiers().stream().toList().forEach(mod -> toughnessInst.removeModifier(mod.id()));
        }

        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!StageManager.isItemLocked(itemId)) {
                    ItemAttributeModifiers itemModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                    itemModifiers.forEach(slot, (attributeHolder, modifier) -> {
                        var inst = player.getAttributes().getInstance(attributeHolder);
                        if (inst != null && !inst.hasModifier(modifier.id())) {
                            inst.addTransientModifier(modifier);
                        }
                    });
                }
            }
        }

        if (player.connection != null) {
            var attributesToSync = new java.util.ArrayList<net.minecraft.world.entity.ai.attributes.AttributeInstance>();
            if (armorInst != null) attributesToSync.add(armorInst);
            if (toughnessInst != null) attributesToSync.add(toughnessInst);

            player.connection.send(new ClientboundUpdateAttributesPacket(
                    player.getId(),
                    attributesToSync
            ));
        }

        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    public int getUnlockedOrder() {
        return this.currentUnlockedOrder;
    }
}