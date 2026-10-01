package net.veroxuniverse.verox_rpg_prog.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

@EventBusSubscriber(modid = RPGProgression.MOD_ID)
public class BossDropHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        StageManager.getBossInfo(entityId).ifPresent(bossInfo -> {
            StageDefinition.DropTable dropTable = bossInfo.drops();

            if ("none".equalsIgnoreCase(dropTable.mode())) {
                return;
            }

            if ("replace".equalsIgnoreCase(dropTable.mode())) {
                event.getDrops().clear();
            }

            RandomSource random = entity.getRandom();
            for (StageDefinition.DropEntry entry : dropTable.drops()) {
                if (random.nextFloat() <= entry.chance()) {
                    Item item = BuiltInRegistries.ITEM.get(entry.item());
                    if (item != null) {
                        int min = entry.countMin();
                        int max = Math.max(min, entry.countMax());
                        int count = min + random.nextInt((max - min) + 1);

                        ItemStack stack = new ItemStack(item, count);
                        ItemEntity itemEntity = new ItemEntity(
                                entity.level(),
                                entity.getX(), entity.getY(), entity.getZ(),
                                stack
                        );
                        event.getDrops().add(itemEntity);
                    }
                }
            }
        });
    }
}