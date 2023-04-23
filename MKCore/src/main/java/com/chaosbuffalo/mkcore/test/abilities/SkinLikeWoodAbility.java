package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public class SkinLikeWoodAbility extends MKToggleAbility {
    public SkinLikeWoodAbility() {
        super();
        setCooldownSeconds(6);
        setManaCost(4);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    @Override
    public MKEffect getToggleEffect() {
        return MKTestEffects.SKIN_LIKE_WOOD.get();
    }

    @Override
    public void applyEffect(LivingEntity castingEntity, IMKEntityData casterData, Function<Attribute, Float> skillSupplier) {
        super.applyEffect(castingEntity, casterData, skillSupplier);

        MKEffectBuilder<?> instance = getToggleEffect().builder(castingEntity)
                .amplify(2)
                .infinite();
        casterData.getEffects().addEffect(instance);
    }
}
