package net.veroxuniverse.unbound_world.compat.jade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
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
        registration.registerBlockComponent(LockedBlockProvider.INSTANCE, Block.class);
    }

    public enum LockedBlockProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(accessor.getBlock());
            if (StageManager.isBlockLocked(blockId)) {
                tooltip.clear();
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
    }
}