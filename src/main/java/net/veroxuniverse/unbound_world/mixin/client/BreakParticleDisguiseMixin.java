package net.veroxuniverse.unbound_world.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientLevel.class, remap = false)
public class BreakParticleDisguiseMixin {

    @Redirect(
            method = "addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/phys/HitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState unbound$maskLockedOreHitEffect(ClientLevel level, BlockPos pos) {
        return disguiseIfLocked(level.getBlockState(pos));
    }

    @ModifyVariable(
            method = "addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BlockState unbound$maskLockedOreDestroyEffect(BlockState blockState) {
        return disguiseIfLocked(blockState);
    }

    private static BlockState disguiseIfLocked(BlockState original) {
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