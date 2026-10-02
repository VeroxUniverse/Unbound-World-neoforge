package net.veroxuniverse.verox_rpg_prog.territory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BorderBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int color) {

    public static final int LOCKED_COLOR = 0xFF3030;
    public static final int PROTECTED_COLOR = 0xFFC040;

    public static final StreamCodec<FriendlyByteBuf, BorderBox> STREAM_CODEC = StreamCodec.of(
            (buffer, box) -> {
                buffer.writeVarInt(box.minX());
                buffer.writeVarInt(box.minY());
                buffer.writeVarInt(box.minZ());
                buffer.writeVarInt(box.maxX());
                buffer.writeVarInt(box.maxY());
                buffer.writeVarInt(box.maxZ());
                buffer.writeInt(box.color());
            },
            buffer -> new BorderBox(
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readInt()
            )
    );

    public boolean contains(BlockPos pos) {
        return pos.getX() >= this.minX && pos.getX() <= this.maxX
                && pos.getY() >= this.minY && pos.getY() <= this.maxY
                && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ;
    }
}