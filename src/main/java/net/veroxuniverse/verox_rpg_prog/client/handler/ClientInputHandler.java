package net.veroxuniverse.verox_rpg_prog.client.handler;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.client.gui.book.BookScreenOpener;
import net.veroxuniverse.verox_rpg_prog.client.key.ModKeyMappings;

@EventBusSubscriber(modid = RPGProgression.MOD_ID, value = Dist.CLIENT)
public class ClientInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;

        while (ModKeyMappings.OPEN_STAGE_GUIDE.consumeClick()) {
            BookScreenOpener.open();
        }
    }
}