package net.veroxuniverse.unbound_world.handler;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.veroxuniverse.unbound_world.UnboundWorld;
import net.veroxuniverse.unbound_world.stage.StageDefinition;
import net.veroxuniverse.unbound_world.stage.StageManager;

@EventBusSubscriber(modid = UnboundWorld.MOD_ID)
public class BossDropHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

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
                    Item item = BuiltInRegistries.ITEM.get(entry.item()).map(Holder::value).orElse(null);
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