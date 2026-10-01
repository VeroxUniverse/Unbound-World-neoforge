package net.veroxuniverse.verox_rpg_prog.lootbag;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class LootBagItem extends Item {

    public LootBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        LootBagContents contents = stack.get(ModDataComponents.LOOT_BAG_CONTENTS);
        if (contents == null) {
            return InteractionResultHolder.fail(stack);
        }

        for (ItemStack rolled : contents.rolledItems()) {
            ItemStack copy = rolled.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BUNDLE_DROP_CONTENTS,
                SoundSource.PLAYERS,
                1.0F,
                0.9F + level.getRandom().nextFloat() * 0.2F
        );

        stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        LootBagContents contents = stack.get(ModDataComponents.LOOT_BAG_CONTENTS);
        if (contents != null) {
            tooltip.add(Component.translatable("item.verox_rpg_prog.loot_bag.from", contents.getBossDisplayName()));
        }
    }
}