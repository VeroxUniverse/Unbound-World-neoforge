package net.veroxuniverse.verox_rpg_prog.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.verox_rpg_prog.RPGProgression;
import net.veroxuniverse.verox_rpg_prog.stage.StageDefinition;

import java.util.HashMap;
import java.util.Map;

public record SyncStageDefinitionsPayload(Map<ResourceLocation, StageDefinition> stages) implements CustomPacketPayload {

    public static final Type<SyncStageDefinitionsPayload> TYPE = new Type<>(RPGProgression.id("sync_stage_definitions"));

    public static final StreamCodec<ByteBuf, SyncStageDefinitionsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodec(StageDefinition.CODEC)),
            SyncStageDefinitionsPayload::stages,
            SyncStageDefinitionsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}