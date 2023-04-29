package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKToggleAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;

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
    public void applyEffect(IMKEntityData casterData, MKAbilityInfo abilityInfo) {
        super.applyEffect(casterData, abilityInfo);

        MKEffectBuilder<?> instance = getToggleEffect().builder(casterData.getEntity())
                .amplify(2)
                .infinite();
        casterData.getEffects().addEffect(instance);
    }
}
