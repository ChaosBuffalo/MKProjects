package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.core.Holder;


public class UltimateTalent extends AbilityGrantTalent {

    public UltimateTalent(Holder<MKAbility> ability) {
        super(ability, TalentType.ULTIMATE);
    }
}
