package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentType;

import java.util.function.Supplier;

public class PassiveTalent extends AbilityGrantTalent {

    public PassiveTalent(Supplier<? extends MKAbility> ability) {
        super(ability, TalentType.PASSIVE);
    }
}
