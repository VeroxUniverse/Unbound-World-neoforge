package net.veroxuniverse.unbound_world.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelRenderer.class, remap = false)
public class LevelRendererMixin {

    @Redirect(
            method = "levelEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState unbound$maskLevelEventSoundAndParticles(ClientLevel level, BlockPos pos) {
        BlockState original = level.getBlockState(pos);
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