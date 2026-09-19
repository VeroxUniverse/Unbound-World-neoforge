package net.veroxuniverse.unbound_world.mixin.client;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SectionCompiler.class, remap = false)
public class OreRenderDisguiseMixin {

    @Redirect(
            method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState unbound$maskLockedOreMesh(RenderChunkRegion region, BlockPos pos) {
        BlockState original = region.getBlockState(pos);

        Block block = original.getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

        if (StageManager.isOreLocked(blockId)) {
            ResourceLocation disguiseId = StageManager.getDisguiseBlock(blockId).orElse(null);
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