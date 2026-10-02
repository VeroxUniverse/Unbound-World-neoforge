package net.veroxuniverse.verox_rpg_prog.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.territory.BorderBox;

import java.util.List;

public record TerritoryBordersPayload(List<BorderBox> boxes) implements CustomPacketPayload {

    public static final Type<TerritoryBordersPayload> TYPE = new Type<>(RPGProgression.id("territory_borders"));

    public static final StreamCodec<FriendlyByteBuf, TerritoryBordersPayload> STREAM_CODEC = StreamCodec.composite(
            BorderBox.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TerritoryBordersPayload::boxes,
            TerritoryBordersPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}