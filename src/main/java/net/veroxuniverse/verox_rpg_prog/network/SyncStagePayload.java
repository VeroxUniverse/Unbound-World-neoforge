package net.veroxuniverse.verox_rpg_prog.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.stage.StageManager;

public record SyncStagePayload(int unlockedOrder) implements CustomPacketPayload {

    public static final Type<SyncStagePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RPGProgression.MOD_ID, "sync_stage"));

    public static final StreamCodec<ByteBuf, SyncStagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncStagePayload::unlockedOrder,
            SyncStagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncStagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            StageManager.setUnlockedOrder(payload.unlockedOrder());

            Minecraft mc = Minecraft.getInstance();
            if (mc.levelRenderer != null) {
                mc.levelRenderer.allChanged();
            }
        });
    }
}