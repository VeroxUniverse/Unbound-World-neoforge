package net.veroxuniverse.verox_rpg_prog.lootbag;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.book.StageBookItem;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RPGProgression.MOD_ID);

    public static final DeferredHolder<Item, LootBagItem> LOOT_BAG = ITEMS.registerItem(
            "loot_bag",
            LootBagItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredHolder<Item, StageBookItem> STAGE_BOOK = ITEMS.registerItem(
            "stage_book",
            StageBookItem::new,
            new Item.Properties().stacksTo(1)
    );
}