package net.veroxuniverse.verox_rpg_prog.lootbag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record LootBagContents(
        ResourceLocation bossEntityId,
        List<ItemStack> rolledItems
) {
    public static final Codec<LootBagContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("boss_entity_id").forGetter(LootBagContents::bossEntityId),
                    ItemStack.CODEC.listOf().fieldOf("rolled_items").forGetter(LootBagContents::rolledItems)
            ).apply(instance, LootBagContents::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LootBagContents> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, LootBagContents::bossEntityId,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), LootBagContents::rolledItems,
            LootBagContents::new
    );

    public Component getBossDisplayName() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(this.bossEntityId)
                .map(EntityType::getDescription)
                .orElseGet(() -> Component.literal(this.bossEntityId.toString()));
    }
}