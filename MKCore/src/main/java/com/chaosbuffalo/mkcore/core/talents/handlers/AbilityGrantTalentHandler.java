package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
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
        if (record.getNode().getTalent() instanceof AbilityGrantTalent grantTalent) {
            MKTalent talent = record.getNode().getTalent();
            MKAbilityInfo abilityInfo = grantTalent.getAbilityInfo();
            if (!record.isKnown()) {
                playerData.getAbilities().unlearnAbility(abilityInfo.getId(), AbilitySource.forTalent(talent));
            } else {
                tryLearn(talent, abilityInfo);
            }
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.isKnown() && record.getNode().getTalent() instanceof AbilityGrantTalent grantTalent) {
            MKAbilityInfo ability = grantTalent.getAbilityInfo();
            tryLearn(record.getNode().getTalent(), ability);
        }
    }

    protected void tryLearn(MKTalent talent, MKAbilityInfo ability) {
        playerData.getAbilities().learnAbility(ability, AbilitySource.forTalent(talent));
    }
}
