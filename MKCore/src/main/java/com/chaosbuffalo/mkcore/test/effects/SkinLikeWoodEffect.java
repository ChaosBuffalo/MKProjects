package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class SkinLikeWoodEffect extends MKEffect {

    public SkinLikeWoodEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(Attributes.ARMOR, UUID.fromString("4b488b68-1151-4bae-b99e-b381707a6964"), 2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
