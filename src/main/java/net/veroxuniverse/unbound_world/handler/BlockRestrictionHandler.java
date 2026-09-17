package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.veroxuniverse.unbound_world.stage.StageManager;

import java.util.List;

public class BlockRestrictionHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            if (player.isCreative()) return;

            Block block = event.getState().getBlock();
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

            if (isBlockLocked(blockId)) {
                event.setCanceled(true);

                player.level().playSound(
                        null,
                        event.getPos(),
                        SoundEvents.ANVIL_PLACE,
                        SoundSource.BLOCKS,
                        0.6F,
                        1.8F
                );
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 2, false, false, false));
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.block_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.isCreative()) return;

            Block block = event.getPlacedBlock().getBlock();
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

            if (isBlockLocked(blockId)) {
                event.setCanceled(true);
                player.displayClientMessage(
                        Component.translatable("message.unbound_world.block_place_locked"),
                        true
                );
            }
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockItem blockItem) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());

            if (isBlockLocked(blockId)) {
                List<Component> tooltip = event.getToolTip();

                int insertIndex = Math.min(1, tooltip.size());

                tooltip.add(insertIndex++, Component.empty());
                tooltip.add(insertIndex++, Component.translatable("tooltip.unbound_world.locked_block_title"));

                final int finalIndex = insertIndex;
                StageManager.getRequiredStageForBlock(blockId).ifPresentOrElse(
                        stage -> tooltip.add(finalIndex, Component.translatable(
                                "tooltip.unbound_world.locked_block_stage",
                                Component.translatable(stage.translationKey())
                        )),
                        () -> tooltip.add(finalIndex, Component.translatable(
                                "tooltip.unbound_world.locked_block_stage",
                                Component.literal("Unknown Stage")
                        ))
                );

                tooltip.add(insertIndex + 1, Component.translatable("tooltip.unbound_world.locked_block_status"));
            }
        }
    }

    public static boolean isBlockLocked(ResourceLocation blockId) {
        return StageManager.isBlockLocked(blockId);
    }
}