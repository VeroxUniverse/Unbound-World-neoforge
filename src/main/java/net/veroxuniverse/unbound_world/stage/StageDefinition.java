package net.veroxuniverse.unbound_world.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record StageDefinition(
        int order,
        String translationKey,
        Optional<BossInfo> mainBoss,
        List<BossInfo> optionalBosses,
        List<ResourceLocation> lockedItems,
        List<ResourceLocation> lockedBlocks
) {
    public record DropEntry(
            ResourceLocation item,
            int countMin,
            int countMax,
            float chance
    ) {
        public static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(DropEntry::item),
                Codec.INT.optionalFieldOf("count_min", 1).forGetter(DropEntry::countMin),
                Codec.INT.optionalFieldOf("count_max", 1).forGetter(DropEntry::countMax),
                Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(DropEntry::chance)
        ).apply(i, DropEntry::new));
    }

    public record DropTable(
            String mode,
            List<DropEntry> drops
    ) {
        public static final Codec<DropTable> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.optionalFieldOf("mode", "none").forGetter(DropTable::mode),
                DropEntry.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(DropTable::drops)
        ).apply(i, DropTable::new));
    }

    public record BossInfo(
            ResourceLocation entityId,
            ResourceLocation displayItem,
            String locationTranslationKey,
            DropTable drops
    ) {
        public static final Codec<BossInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("entity_id").forGetter(BossInfo::entityId),
                ResourceLocation.CODEC.optionalFieldOf("display_item", ResourceLocation.withDefaultNamespace("barrier")).forGetter(BossInfo::displayItem),
                Codec.STRING.optionalFieldOf("location_translation_key", "").forGetter(BossInfo::locationTranslationKey),
                DropTable.CODEC.optionalFieldOf("drops", new DropTable("additional", List.of())).forGetter(BossInfo::drops)
        ).apply(i, BossInfo::new));
    }

    public static final Codec<StageDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("order").forGetter(StageDefinition::order),
                    Codec.STRING.fieldOf("stage_translation_key").forGetter(StageDefinition::translationKey),
                    BossInfo.CODEC.optionalFieldOf("main_boss").forGetter(StageDefinition::mainBoss),
                    BossInfo.CODEC.listOf().optionalFieldOf("optional_bosses", List.of()).forGetter(StageDefinition::optionalBosses),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_items", List.of()).forGetter(StageDefinition::lockedItems),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_blocks_to_mine", List.of()).forGetter(StageDefinition::lockedBlocks)
            ).apply(instance, StageDefinition::new)
    );
}