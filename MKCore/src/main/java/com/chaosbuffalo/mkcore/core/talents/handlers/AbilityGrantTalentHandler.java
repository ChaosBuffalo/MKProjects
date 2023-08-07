package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
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
            MKAbility ability = grantTalent.getAbility();
            if (!record.isKnown()) {
                playerData.getAbilities().unlearnAbility(ability.getAbilityId(), AbilitySource.forTalent(record.getNode()));
            } else {
                tryLearn(record.getNode(), ability);
            }
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.isKnown() && record.getNode().getTalent() instanceof AbilityGrantTalent grantTalent) {
            MKAbility ability = grantTalent.getAbility();
            tryLearn(record.getNode(), ability);
        }
    }

    protected void tryLearn(TalentNode node, MKAbility ability) {
        playerData.getAbilities().learnAbility(ability, AbilitySource.forTalent(node));
    }
}
