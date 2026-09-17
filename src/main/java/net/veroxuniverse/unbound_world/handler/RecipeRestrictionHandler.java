package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.veroxuniverse.unbound_world.stage.StageManager;

public class RecipeRestrictionHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.isCreative() || player.level().isClientSide) return;

        AbstractContainerMenu menu = player.containerMenu;
        if (menu instanceof CraftingMenu || menu instanceof InventoryMenu) {
            for (Slot slot : menu.slots) {
                if (slot instanceof ResultSlot) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        ResourceLocation blockId = stack.getItem() instanceof BlockItem bi
                                ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                                : null;

                        if (StageManager.isItemLocked(itemId) || (blockId != null && StageManager.isBlockLocked(blockId))) {
                            slot.set(ItemStack.EMPTY);
                            menu.broadcastChanges();
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(crafted.getItem());
        ResourceLocation blockId = crafted.getItem() instanceof BlockItem bi
                ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                : null;

        if (StageManager.isItemLocked(itemId) || (blockId != null && StageManager.isBlockLocked(blockId))) {
            crafted.setCount(0);
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                        Component.translatable("message.unbound_world.item_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        ItemStack smelted = event.getSmelting();
        if (smelted.isEmpty()) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(smelted.getItem());
        ResourceLocation blockId = smelted.getItem() instanceof BlockItem bi
                ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                : null;

        if (StageManager.isItemLocked(itemId) || (blockId != null && StageManager.isBlockLocked(blockId))) {
            smelted.setCount(0);
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                        Component.translatable("message.unbound_world.item_locked"),
                        true
                );
            }
        }
    }
}