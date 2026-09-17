package net.veroxuniverse.unbound_world.util;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.veroxuniverse.unbound_world.stage.StageDefinition;

public class StageNotifier {

    public static void broadcastStageUnlocked(MinecraftServer server, StageDefinition stage) {
        Component title = Component.translatable("title.unbound_world.stage_unlocked");
        Component subtitle = Component.translatable(stage.translationKey());

        ClientboundSetTitlesAnimationPacket animPacket = new ClientboundSetTitlesAnimationPacket(10, 60, 20);
        ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(title);
        ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(subtitle);

        Holder<SoundEvent> soundHolder = Holder.direct(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(animPacket);
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);

            player.connection.send(new ClientboundSoundPacket(
                    soundHolder,
                    SoundSource.PLAYERS,
                    player.getX(), player.getY(), player.getZ(),
                    1.0f, 1.0f, player.level().getRandom().nextLong()
            ));
        }
    }
}