package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.veroxuniverse.unbound_world.stage.StageManager;

public class BlockRestrictionHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (isBlockLocked(blockId)) {
            event.setCanceled(true);
            if (player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.block_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (isBlockLocked(blockId)) {
            event.setCanceled(true);
            event.setNewSpeed(0.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        BlockState state = event.getState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (isBlockLocked(blockId)) {
            event.setCanceled(true);
            if (player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.block_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.isCreative()) return;

        BlockPos pos = event.getPos();
        BlockState clickedState = event.getLevel().getBlockState(pos);
        ResourceLocation clickedBlockId = BuiltInRegistries.BLOCK.getKey(clickedState.getBlock());

        if (isBlockLocked(clickedBlockId)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            if (player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.block_locked"),
                        true
                );
            }
            return;
        }

        ItemStack heldItem = event.getItemStack();
        if (!heldItem.isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
            ResourceLocation blockToPlaceId = heldItem.getItem() instanceof BlockItem bi
                    ? BuiltInRegistries.BLOCK.getKey(bi.getBlock())
                    : null;

            boolean isBlockedItem = StageManager.isItemLocked(itemId);
            boolean isBlockedBlock = blockToPlaceId != null && isBlockLocked(blockToPlaceId);

            if (isBlockedItem || isBlockedBlock) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);

                if (player instanceof ServerPlayer sp) {
                    sp.containerMenu.sendAllDataToRemote();
                }

                if (player.level().isClientSide) {
                    player.displayClientMessage(
                            Component.translatable(isBlockedBlock ? "message.unbound_world.block_place_locked" : "message.unbound_world.item_locked"),
                            true
                    );
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isCreative()) return;

        BlockState placedState = event.getPlacedBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(placedState.getBlock());

        if (isBlockLocked(blockId)) {
            event.setCanceled(true);
            if (player instanceof ServerPlayer sp) {
                sp.containerMenu.sendAllDataToRemote();
            }
            if (player.level().isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.block_place_locked"),
                        true
                );
            }
        }
    }

    public static boolean isBlockLocked(ResourceLocation blockId) {
        return StageManager.isBlockLocked(blockId);
    }
}