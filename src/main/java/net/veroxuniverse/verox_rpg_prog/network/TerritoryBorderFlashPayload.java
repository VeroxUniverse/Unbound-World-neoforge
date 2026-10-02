package net.veroxuniverse.verox_rpg_prog.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;

public record TerritoryBorderFlashPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<TerritoryBorderFlashPayload> TYPE = new Type<>(RPGProgression.id("territory_border_flash"));

    public static final StreamCodec<ByteBuf, TerritoryBorderFlashPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TerritoryBorderFlashPayload::pos,
            TerritoryBorderFlashPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}