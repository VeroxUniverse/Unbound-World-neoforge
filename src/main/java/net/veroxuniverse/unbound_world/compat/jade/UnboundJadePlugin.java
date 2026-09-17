package net.veroxuniverse.unbound_world.compat.jade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.handler.BlockRestrictionHandler;
import net.veroxuniverse.unbound_world.stage.StageManager;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class UnboundJadePlugin implements IWailaPlugin {
    public static final ResourceLocation LOCKED_BLOCK_COMPONENT = ResourceLocation.fromNamespaceAndPath(UnboundWorld.MOD_ID, "locked_block");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                Block block = accessor.getBlock();
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

                if (BlockRestrictionHandler.isBlockLocked(blockId)) {
                    tooltip.add(Component.translatable("tooltip.unbound_world.locked_block_title"));

                    StageManager.getRequiredStageForBlock(blockId).ifPresent(stage -> {
                        tooltip.add(Component.translatable(
                                "tooltip.unbound_world.locked_block_stage",
                                Component.translatable(stage.translationKey())
                        ));
                    });

                    tooltip.add(Component.translatable("tooltip.unbound_world.locked_block_status"));
                }
            }

            @Override
            public ResourceLocation getUid() {
                return LOCKED_BLOCK_COMPONENT;
            }
        }, Block.class);
    }
}