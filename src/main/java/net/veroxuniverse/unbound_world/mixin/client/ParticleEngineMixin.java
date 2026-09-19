package net.veroxuniverse.unbound_world.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ParticleEngine.class, remap = false)
public class ParticleEngineMixin {

    @ModifyVariable(
            method = "destroy",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private BlockState unbound$maskDestroyParticles(BlockState state) {
        return unbound$getDisguiseState(state);
    }

    @Redirect(
            method = "crack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState unbound$maskCrackParticles(ClientLevel level, BlockPos pos) {
        BlockState original = level.getBlockState(pos);
        return unbound$getDisguiseState(original);
    }

    private static BlockState unbound$getDisguiseState(BlockState original) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(original.getBlock());
        if (StageManager.isOreLocked(blockId)) {
            var disguiseId = StageManager.getDisguiseBlock(blockId).orElse(null);
            if (disguiseId != null) {
                Block disguiseBlock = BuiltInRegistries.BLOCK.get(disguiseId);
                if (disguiseBlock != null) {
                    return disguiseBlock.defaultBlockState();
                }
            }
        }
        return original;
    }
}