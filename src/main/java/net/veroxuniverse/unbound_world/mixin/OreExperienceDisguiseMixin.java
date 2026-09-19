package net.veroxuniverse.unbound_world.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.stage.StageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DropExperienceBlock.class, remap = false)
public class OreExperienceDisguiseMixin {

    @Inject(
            method = "getExpDrop(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void unbound$suppressLockedOreExperience(
            BlockState state,
            LevelAccessor level,
            BlockPos pos,
            BlockEntity blockEntity,
            Entity breaker,
            ItemStack tool,
            CallbackInfoReturnable<Integer> cir
    ) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (StageManager.isOreLocked(blockId)) {
            cir.setReturnValue(0);
        }
    }
}