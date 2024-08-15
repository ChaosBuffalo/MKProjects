package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.core.Holder;


public class PassiveTalent extends AbilityGrantTalent {

    public PassiveTalent(Holder<MKAbility> ability) {
        super(ability, TalentType.PASSIVE);
    }
}
