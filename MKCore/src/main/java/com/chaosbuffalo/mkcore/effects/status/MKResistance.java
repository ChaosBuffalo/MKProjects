package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class MKResistance extends MKEffect {
    private final float perLevel;

    public MKResistance(ResourceLocation loc, Attribute attribute, UUID attrId, float perLevel) {
        super(perLevel > 0.0f ? MobEffectCategory.BENEFICIAL : MobEffectCategory.HARMFUL);
        setRegistryName(loc);
        this.perLevel = perLevel;
        addAttribute(attribute, attrId, perLevel, perLevel, AttributeModifier.Operation.ADDITION, MKAttributes.ABJURATION);
    }

    public float getPerLevel() {
        return perLevel;
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
