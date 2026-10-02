package net.veroxuniverse.verox_rpg_prog.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;

public record TerritoryStatePayload(int tintColor) implements CustomPacketPayload {

    public static final Type<TerritoryStatePayload> TYPE = new Type<>(RPGProgression.id("territory_state"));

    public static final StreamCodec<ByteBuf, TerritoryStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            TerritoryStatePayload::tintColor,
            TerritoryStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}