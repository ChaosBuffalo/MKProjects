package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.MKCore;
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
            AbilitySource source = nodeSource(record);
            if (!record.isKnown()) {
                if (persona.getAbilities().knowsAbility(abilityNode.getAbilityId())) {
                    persona.getAbilities().unlearnAbility(abilityNode.getAbilityId(), source);
                }
            } else {
                tryLearn(record, abilityNode);
            }
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        if (record.getNode() instanceof AbilityGrantTalentNode abilityNode) {
            tryLearn(record, abilityNode);
        }
    }

    private AbilitySource nodeSource(TalentRecord record) {
        return AbilitySource.forTalent(record);
    }

    protected void tryLearn(TalentRecord record, AbilityGrantTalentNode abilityNode) {
        MKAbility ability = abilityNode.getAbility();
        if (ability != null) {
            persona.getAbilities().learnAbility(ability, nodeSource(record));
            return;
        }

        if (abilityNode.getAbilityDefinition() != null) {
            persona.getAbilities().learnAbilityDefinition(abilityNode.getAbilityId(), nodeSource(record));
            return;
        }

        MKCore.LOGGER.warn("Persona {} tried to apply unknown talent-granted ability {}",
                persona.getEntity(), abilityNode.getAbilityId());
    }
}
