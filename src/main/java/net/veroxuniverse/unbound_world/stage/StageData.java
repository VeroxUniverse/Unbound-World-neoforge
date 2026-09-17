package net.veroxuniverse.unbound_world.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record StageData(
        ResourceLocation id,
        int order,
        String translationKey,
        List<ResourceLocation> lockedItems,
        List<ResourceLocation> lockedBlocks
) {
    public static final Codec<StageData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("stage_id").forGetter(StageData::id),
                    Codec.INT.fieldOf("order").forGetter(StageData::order),
                    Codec.STRING.fieldOf("stage_translation_key").forGetter(StageData::translationKey),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_items", List.of()).forGetter(StageData::lockedItems),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_blocks", List.of()).forGetter(StageData::lockedBlocks)
            ).apply(instance, StageData::new)
    );
}