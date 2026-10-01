package net.veroxuniverse.verox_rpg_prog.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RPGProgression.MOD_ID, value = Dist.CLIENT)
public class ModKeyMappings {

    public static final KeyMapping OPEN_STAGE_GUIDE = new KeyMapping(
            "key.verox_rpg_prog.open_guide",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.verox_rpg_prog"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STAGE_GUIDE);
    }
}