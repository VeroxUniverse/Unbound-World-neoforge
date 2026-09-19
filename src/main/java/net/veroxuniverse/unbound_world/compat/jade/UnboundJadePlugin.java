package net.veroxuniverse.unbound_world.compat.jade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageManager;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class UnboundJadePlugin implements IWailaPlugin {

    public static final ResourceLocation LOCKED_BLOCK = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "locked_block");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addRayTraceCallback((hitResult, accessor, unmodifiedAccessor) -> {
            if (accessor instanceof BlockAccessor blockAccessor) {
                Level level = blockAccessor.getLevel();
                BlockPos pos = blockAccessor.getPosition();
                BlockState state = level.getBlockState(pos);
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

                if (StageManager.isOreLocked(blockId)) {
                    var disguiseId = StageManager.getDisguiseBlock(blockId).orElse(null);
                    if (disguiseId != null) {
                        Block disguiseBlock = BuiltInRegistries.BLOCK.get(disguiseId);
                        if (disguiseBlock != null && disguiseBlock != Blocks.AIR) {
                            return registration.blockAccessor()
                                    .from(blockAccessor)
                                    .blockState(disguiseBlock.defaultBlockState())
                                    .build();
                        }
                    }
                }
            }
            return accessor;
        });

        registration.registerBlockComponent(LockedBlockProvider.INSTANCE, Block.class);
    }

    public enum LockedBlockProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Level level = accessor.getLevel();
            BlockPos pos = accessor.getPosition();
            BlockState state = level.getBlockState(pos);
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

            if (StageManager.isOreLocked(blockId)) {
                return;
            }

            if (StageManager.isBlockLocked(blockId)) {
                tooltip.add(Component.translatable("tooltip.unbound_world.locked_block_title"));

                StageManager.getRequiredStageForBlock(blockId).ifPresentOrElse(
                        stage -> tooltip.add(Component.translatable(
                                "tooltip.unbound_world.locked_block_stage",
                                Component.translatable(stage.translationKey())
                        )),
                        () -> tooltip.add(Component.translatable(
                                "tooltip.unbound_world.locked_block_stage",
                                Component.literal("Unknown Stage")
                        ))
                );

                tooltip.add(Component.translatable("tooltip.unbound_world.locked_block_status"));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return LOCKED_BLOCK;
        }

        @Override
        public int getDefaultPriority() {
            return 10000;
        }
    }
}