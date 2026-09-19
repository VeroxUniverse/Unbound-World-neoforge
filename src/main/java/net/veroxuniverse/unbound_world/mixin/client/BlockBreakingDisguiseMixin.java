package net.veroxuniverse.unbound_world.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelRenderer.class, remap = false)
public class BlockBreakingDisguiseMixin {

    @Redirect(
            method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState unbound$maskLockedOreBreakingOverlay(ClientLevel level, BlockPos pos) {
        BlockState original = level.getBlockState(pos);

        Block block = original.getBlock();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);

        if (StageManager.isOreLocked(blockId)) {
            Identifier disguiseId = StageManager.getDisguiseBlock(blockId).orElse(null);
            if (disguiseId != null) {
                Block disguiseBlock = BuiltInRegistries.BLOCK.get(disguiseId).map(Holder::value).orElse(null);
                if (disguiseBlock != null) {
                    return disguiseBlock.defaultBlockState();
                }
            }
        }

        return original;
    }
}