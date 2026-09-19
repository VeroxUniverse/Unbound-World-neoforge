package net.veroxuniverse.unbound_world.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record StageDefinition(
        int order,
        String translationKey,
        MobAttributeScaling mobAttributeScaling,
        List<MobEquipmentOverride> mobEquipment,
        List<OreDisguise> lockedOres,
        Optional<BossInfo> mainBoss,
        List<BossInfo> optionalBosses,
        List<ResourceLocation> lockedItems,
        List<ResourceLocation> lockedBlocks,
        List<ResourceLocation> lockedDimensions
) {
    public record MobAttributeScaling(
            float healthMultiplier,
            float damageMultiplier,
            float knockbackResistanceMultiplier,
            float armorMultiplier
    ) {
        public static final MobAttributeScaling NONE = new MobAttributeScaling(1.0f, 1.0f, 1.0f, 1.0f);

        public static final Codec<MobAttributeScaling> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("health_multiplier", 1.0f).forGetter(MobAttributeScaling::healthMultiplier),
                Codec.FLOAT.optionalFieldOf("damage_multiplier", 1.0f).forGetter(MobAttributeScaling::damageMultiplier),
                Codec.FLOAT.optionalFieldOf("knockback_resistance_multiplier", 1.0f).forGetter(MobAttributeScaling::knockbackResistanceMultiplier),
                Codec.FLOAT.optionalFieldOf("armor_multiplier", 1.0f).forGetter(MobAttributeScaling::armorMultiplier)
        ).apply(i, MobAttributeScaling::new));

        public boolean isNoOp() {
            return this.healthMultiplier <= 1.0f
                    && this.damageMultiplier <= 1.0f
                    && this.knockbackResistanceMultiplier <= 1.0f
                    && this.armorMultiplier <= 1.0f;
        }
    }

    public record PlayerScaling(
            double radius,
            float healthPerExtraPlayer,
            float damagePerExtraPlayer,
            float knockbackResistancePerExtraPlayer,
            float armorPerExtraPlayer
    ) {
        public static final PlayerScaling NONE = new PlayerScaling(32.0, 0.0f, 0.0f, 0.0f, 0.0f);

        public static final Codec<PlayerScaling> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.optionalFieldOf("radius", 32.0).forGetter(PlayerScaling::radius),
                Codec.FLOAT.optionalFieldOf("health_per_extra_player", 0.0f).forGetter(PlayerScaling::healthPerExtraPlayer),
                Codec.FLOAT.optionalFieldOf("damage_per_extra_player", 0.0f).forGetter(PlayerScaling::damagePerExtraPlayer),
                Codec.FLOAT.optionalFieldOf("knockback_resistance_per_extra_player", 0.0f).forGetter(PlayerScaling::knockbackResistancePerExtraPlayer),
                Codec.FLOAT.optionalFieldOf("armor_per_extra_player", 0.0f).forGetter(PlayerScaling::armorPerExtraPlayer)
        ).apply(i, PlayerScaling::new));

        public boolean isNoOp() {
            return this.healthPerExtraPlayer <= 0.0f
                    && this.damagePerExtraPlayer <= 0.0f
                    && this.knockbackResistancePerExtraPlayer <= 0.0f
                    && this.armorPerExtraPlayer <= 0.0f;
        }
    }

    public record EnchantmentEntry(
            ResourceLocation enchantmentId,
            int level
    ) {
        public static final Codec<EnchantmentEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(EnchantmentEntry::enchantmentId),
                Codec.INT.optionalFieldOf("level", 1).forGetter(EnchantmentEntry::level)
        ).apply(i, EnchantmentEntry::new));
    }

    public record EquipmentEntry(
            String slot,
            ResourceLocation item,
            List<EnchantmentEntry> enchantments,
            float dropChance
    ) {
        public static final Codec<EquipmentEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("slot").forGetter(EquipmentEntry::slot),
                ResourceLocation.CODEC.fieldOf("item").forGetter(EquipmentEntry::item),
                EnchantmentEntry.CODEC.listOf().optionalFieldOf("enchantments", List.of()).forGetter(EquipmentEntry::enchantments),
                Codec.FLOAT.optionalFieldOf("drop_chance", 0.0f).forGetter(EquipmentEntry::dropChance)
        ).apply(i, EquipmentEntry::new));
    }

    public record MobEquipmentOverride(
            List<ResourceLocation> entityIds,
            List<EquipmentEntry> equipment
    ) {
        public static final Codec<MobEquipmentOverride> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.listOf().fieldOf("entity_ids").forGetter(MobEquipmentOverride::entityIds),
                EquipmentEntry.CODEC.listOf().optionalFieldOf("equipment", List.of()).forGetter(MobEquipmentOverride::equipment)
        ).apply(i, MobEquipmentOverride::new));
    }

    public record OreDisguise(
            ResourceLocation oreBlock,
            ResourceLocation disguiseBlock
    ) {
        public static final Codec<OreDisguise> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("ore_block").forGetter(OreDisguise::oreBlock),
                ResourceLocation.CODEC.fieldOf("disguise_block").forGetter(OreDisguise::disguiseBlock)
        ).apply(i, OreDisguise::new));
    }

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
            List<ResourceLocation> guaranteedDrops,
            PlayerScaling playerScaling,
            DropTable drops
    ) {
        public static final Codec<BossInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("entity_id").forGetter(BossInfo::entityId),
                ResourceLocation.CODEC.optionalFieldOf("display_item", ResourceLocation.withDefaultNamespace("barrier")).forGetter(BossInfo::displayItem),
                Codec.STRING.optionalFieldOf("location_translation_key", "").forGetter(BossInfo::locationTranslationKey),
                ResourceLocation.CODEC.listOf().optionalFieldOf("guaranteed_drops", List.of()).forGetter(BossInfo::guaranteedDrops),
                PlayerScaling.CODEC.optionalFieldOf("player_scaling", PlayerScaling.NONE).forGetter(BossInfo::playerScaling),
                DropTable.CODEC.optionalFieldOf("drops", new DropTable("additional", List.of())).forGetter(BossInfo::drops)
        ).apply(i, BossInfo::new));
    }

    public static final Codec<StageDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("order").forGetter(StageDefinition::order),
                    Codec.STRING.fieldOf("stage_translation_key").forGetter(StageDefinition::translationKey),
                    MobAttributeScaling.CODEC.optionalFieldOf("mob_attribute_scaling", MobAttributeScaling.NONE).forGetter(StageDefinition::mobAttributeScaling),
                    MobEquipmentOverride.CODEC.listOf().optionalFieldOf("mob_equipment", List.of()).forGetter(StageDefinition::mobEquipment),
                    OreDisguise.CODEC.listOf().optionalFieldOf("locked_ores", List.of()).forGetter(StageDefinition::lockedOres),
                    BossInfo.CODEC.optionalFieldOf("main_boss").forGetter(StageDefinition::mainBoss),
                    BossInfo.CODEC.listOf().optionalFieldOf("optional_bosses", List.of()).forGetter(StageDefinition::optionalBosses),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_items", List.of()).forGetter(StageDefinition::lockedItems),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_blocks_to_mine", List.of()).forGetter(StageDefinition::lockedBlocks),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("locked_dimensions", List.of()).forGetter(StageDefinition::lockedDimensions)
            ).apply(instance, StageDefinition::new)
    );
}