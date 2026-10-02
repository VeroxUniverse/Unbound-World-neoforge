package net.veroxuniverse.verox_rpg_prog.book;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.veroxuniverse.verox_rpg_prog.client.gui.book.BookScreenOpener;

public class StageBookItem extends Item {

    public StageBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            BookScreenOpener.open();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}