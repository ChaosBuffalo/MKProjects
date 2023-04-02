package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AbilityGrantTalent;

public class AbilityGrantTalentHandler extends TalentTypeHandler {
    public AbilityGrantTalentHandler(MKPlayerData playerData) {
        super(playerData);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode().getTalent() instanceof AbilityGrantTalent) {
            MKTalent talent = record.getNode().getTalent();
            MKAbility ability = ((AbilityGrantTalent) record.getNode().getTalent()).getAbility();
            if (!record.isKnown()) {
                playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forTalent(talent));
            } else {
                tryLearn(talent, ability);
            }
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.isKnown() && record.getNode().getTalent() instanceof AbilityGrantTalent) {
            MKAbility ability = ((AbilityGrantTalent) record.getNode().getTalent()).getAbility();
            tryLearn(record.getNode().getTalent(), ability);
        }
    }

    protected void tryLearn(MKTalent talent, MKAbility ability) {
        playerData.getAbilities().learnAbility(ability, AbilitySource.forTalent(talent));
    }
}
