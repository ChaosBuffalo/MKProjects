package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

public final class MKAbilityPowerResolver implements AbilityPowerResolver {
    @Override
    public AbilityStatSnapshot captureInvocationStats(IMKEntityData caster) {
        return snapshotFor(caster);
    }

    @Override
    public AbilityStatSnapshot resolveStats(AbilityActionContext context, StatCapturePolicy policy) {
        return switch (policy) {
            case ON_INVOCATION -> context.invocation().invocationStats();
            case RESAMPLE_TICK, RESAMPLE_IMPACT -> snapshotFor(context.casterData());
        };
    }

    @Override
    public double resolve(AbilityScalar scalar, AbilityActionContext context) {
        return switch (scalar) {
            case AbilityScalar.ConstantScalar constant -> constant.value();
            case AbilityScalar.ParameterScalar parameter -> numericValue(context.getParam(parameter.parameter()), parameter.parameter());
            case AbilityScalar.AttributeScaledScalar attributeScaled -> {
                double base = resolve(attributeScaled.base(), context);
                double scale = resolve(attributeScaled.scale(), context);
                double attributeValue = context.stats(attributeScaled.capturePolicy()).attributes()
                        .getOrDefault(attributeScaled.attribute(), 0.0);
                yield base + scale * attributeValue;
            }
        };
    }

    private AbilityStatSnapshot snapshotFor(IMKEntityData caster) {
        Object2DoubleOpenHashMap<ResourceLocation> attributes = new Object2DoubleOpenHashMap<>();
        MKAttributes.iterateEntityAttributes(attribute -> captureAttribute(attributes, caster, attribute));
        if (caster.getEntity() instanceof Player) {
            MKAttributes.iteratePlayerAttributes(attribute -> captureAttribute(attributes, caster, attribute));
        }

        return new AbilityStatSnapshot(
                attributes,
                MKAttributes.getValueSafe(MKAttributes.CASTING_SPEED, caster.getEntity()),
                MKAttributes.getValueSafe(MKAttributes.COOLDOWN, caster.getEntity()),
                1.0,
                MKAttributes.getValueSafe(MKAttributes.SPELL_CRIT, caster.getEntity()),
                MKAttributes.getValueSafe(MKAttributes.SPELL_CRIT_MULTIPLIER, caster.getEntity())
        );
    }

    private void captureAttribute(Object2DoubleOpenHashMap<ResourceLocation> attributes,
                                  IMKEntityData caster,
                                  Holder<Attribute> attributeHolder) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attributeHolder.value());
        if (id != null) {
            attributes.put(id, MKAttributes.getValueSafe(attributeHolder, caster.getEntity()));
        }
    }

    private double numericValue(AbilityValue value, String label) {
        return switch (value.kind()) {
            case FLOAT -> value.asFloat(label);
            case INT -> value.asInt(label);
            case BOOL, STRING, ENTITY_REF, RESOURCE_LOCATION ->
                    throw new IllegalStateException("Ability scalar '" + label + "' resolved to non-numeric " + value.kind());
        };
    }
}
