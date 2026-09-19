package net.veroxuniverse.unbound_world.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class AttributeScalingUtil {

    private AttributeScalingUtil() {}

    public static void applyFractionModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation modifierId, double bonusFraction) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(modifierId);

        if (bonusFraction <= 0.0) return;

        instance.addPermanentModifier(new AttributeModifier(
                modifierId,
                bonusFraction,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }
}