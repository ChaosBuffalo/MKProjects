package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.AbilityGrantTalentNode;

public class AbilityGrantTalentHandler extends TalentTypeHandler {
    public AbilityGrantTalentHandler(Persona persona) {
        super(persona);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode() instanceof AbilityGrantTalentNode abilityNode) {
            MKAbility ability = abilityNode.getAbility();
            if (!record.isKnown()) {
                persona.getAbilities().unlearnAbility(ability.getAbilityId(), nodeSource(record));
            } else {
                tryLearn(record, ability);
            }
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.getNode() instanceof AbilityGrantTalentNode abilityNode) {
            MKAbility ability = abilityNode.getAbility();
            tryLearn(record, ability);
        }
    }

    private AbilitySource nodeSource(TalentRecord record) {
        return AbilitySource.forTalent(record);
    }

    protected void tryLearn(TalentRecord record, MKAbility ability) {
        persona.getAbilities().learnAbility(ability, nodeSource(record));
    }
}
