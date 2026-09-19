package net.veroxuniverse.unbound_world.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.veroxuniverse.unbound_world.UnboundWorld;

public record SyncStagePayload(int unlockedOrder) implements CustomPacketPayload {

    public static final Type<SyncStagePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(UnboundWorld.MOD_ID, "sync_stage"));

    public static final StreamCodec<ByteBuf, SyncStagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncStagePayload::unlockedOrder,
            SyncStagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}