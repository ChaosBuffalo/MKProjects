package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class AttackSpeedEffect extends MKEffect {

    public AttackSpeedEffect(UUID attributeModifierId, double base, double scaling, Holder<Attribute> skill) {
        super(base >= 0.0 ? MobEffectCategory.BENEFICIAL : MobEffectCategory.HARMFUL);
        addAttribute(Attributes.ATTACK_SPEED, attributeModifierId, base, scaling,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, skill);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
