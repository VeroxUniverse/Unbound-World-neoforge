package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageManager;

import java.util.List;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class OreDisguiseHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        Level level = player.level();

        event.getPosition().ifPresent(pos -> {
            BlockState realState = level.getBlockState(pos);
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(realState.getBlock());

            if (StageManager.isOreLocked(blockId)) {
                StageManager.getDisguiseBlock(blockId).ifPresent(disguiseId -> {
                    Block disguiseBlock = BuiltInRegistries.BLOCK.get(disguiseId);
                    if (disguiseBlock != null) {
                        BlockState disguiseState = disguiseBlock.defaultBlockState();
                        ItemStack tool = player.getMainHandItem();

                        float toolSpeed = tool.getDestroySpeed(disguiseState);
                        float disguiseHardness = disguiseBlock.defaultDestroyTime();
                        float realHardness = realState.getBlock().defaultDestroyTime();

                        if (disguiseHardness > 0.0F && realHardness > 0.0F) {
                            float hardnessFactor = realHardness / disguiseHardness;
                            event.setNewSpeed(toolSpeed * hardnessFactor);
                        } else {
                            event.setNewSpeed(toolSpeed);
                        }
                    }
                });
            }
        });
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        BlockState realState = event.getTargetBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(realState.getBlock());

        if (StageManager.isOreLocked(blockId)) {
            StageManager.getDisguiseBlock(blockId).ifPresent(disguiseId -> {
                Block disguiseBlock = BuiltInRegistries.BLOCK.get(disguiseId);
                if (disguiseBlock != null) {
                    BlockState disguiseState = disguiseBlock.defaultBlockState();
                    boolean canHarvestDisguise = event.getEntity().hasCorrectToolForDrops(disguiseState);
                    event.setCanHarvest(canHarvestDisguise);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockState realState = event.getState();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(realState.getBlock());

        if (StageManager.isOreLocked(blockId)) {
            StageManager.getDisguiseBlock(blockId).ifPresent(disguiseId -> {
                Block disguiseBlock = BuiltInRegistries.BLOCK.get(disguiseId);
                if (disguiseBlock != null) {
                    BlockPos pos = event.getPos();
                    BlockState disguiseState = disguiseBlock.defaultBlockState();

                    event.getDrops().clear();

                    LootParams.Builder paramsBuilder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.TOOL, event.getTool())
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, event.getBlockEntity())
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, event.getBreaker());

                    List<ItemStack> fakeDrops = disguiseState.getDrops(paramsBuilder);

                    for (ItemStack drop : fakeDrops) {
                        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                                serverLevel,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                drop
                        ));
                    }
                }
            });
        }
    }
}