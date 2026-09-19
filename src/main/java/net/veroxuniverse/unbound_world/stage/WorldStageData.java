package net.veroxuniverse.unbound_world.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.ModList;
import net.veroxuniverse.unbound_world.compat.curios.CuriosRestrictionHandler;
import net.veroxuniverse.unbound_world.network.ModNetworking;

import java.util.ArrayList;
import java.util.List;

public class WorldStageData {

    private int unlockedOrder = -1;

    public static final MapCodec<WorldStageData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("unlocked_order").forGetter(WorldStageData::getUnlockedOrder)
            ).apply(instance, WorldStageData::new)
    );

    public WorldStageData() {
    }

    public WorldStageData(int unlockedOrder) {
        this.unlockedOrder = unlockedOrder;
        StageManager.setUnlockedOrder(unlockedOrder);
    }

    public int getUnlockedOrder() {
        return this.unlockedOrder;
    }

    public static WorldStageData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getData(ModAttachments.WORLD_STAGE_DATA);
    }

    public void setUnlockedOrder(ServerLevel level, int order) {
        this.unlockedOrder = order;
        StageManager.setUnlockedOrder(order);

        ServerLevel overworld = level.getServer().overworld();
        overworld.setData(ModAttachments.WORLD_STAGE_DATA, this);

        ModNetworking.sendToAll(order);

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.isCreative()) continue;

            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (StageManager.isItemLocked(itemId)) {
                        ItemAttributeModifiers itemModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                        itemModifiers.forEach(slot, (attributeHolder, modifier) -> {
                            AttributeInstance inst = player.getAttributes().getInstance(attributeHolder);
                            if (inst != null) {
                                inst.removeModifier(modifier.id());
                            }
                        });

                        player.setItemSlot(slot, ItemStack.EMPTY);

                        if (!player.getInventory().add(stack)) {
                            player.drop(stack, false);
                        }

                        player.sendOverlayMessage(Component.translatable("message.unbound_world.item_locked"));
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
        AttributeInstance armorInst = player.getAttributes().getInstance(Attributes.ARMOR);
        if (armorInst != null) {
            armorInst.getModifiers().stream().toList().forEach(mod -> armorInst.removeModifier(mod.id()));
        }

        AttributeInstance toughnessInst = player.getAttributes().getInstance(Attributes.ARMOR_TOUGHNESS);
        if (toughnessInst != null) {
            toughnessInst.getModifiers().stream().toList().forEach(mod -> toughnessInst.removeModifier(mod.id()));
        }

        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (!StageManager.isItemLocked(itemId)) {
                    ItemAttributeModifiers itemModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
                    itemModifiers.forEach(slot, (attributeHolder, modifier) -> {
                        AttributeInstance inst = player.getAttributes().getInstance(attributeHolder);
                        if (inst != null && !inst.hasModifier(modifier.id())) {
                            inst.addTransientModifier(modifier);
                        }
                    });
                }
            }
        }

        if (player.connection != null) {
            List<AttributeInstance> attributesToSync = new ArrayList<>();
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
}