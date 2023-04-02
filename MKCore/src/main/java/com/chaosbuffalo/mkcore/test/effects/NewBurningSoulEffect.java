package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class NewBurningSoulEffect extends MKEffect {

    public final UUID MODIFIER_ID = UUID.fromString("a5924381-d396-4dde-9a62-908b479d9775");

    public NewBurningSoulEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(MKAttributes.SPELL_CRIT_MULTIPLIER, MODIFIER_ID, 1.0, AttributeModifier.Operation.ADDITION);
        addAttribute(MKAttributes.SPELL_CRIT, MODIFIER_ID, 0.1, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
