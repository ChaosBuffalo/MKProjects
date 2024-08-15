package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class AbilityGrantTalent extends MKTalent {
    private final Holder<MKAbility> ability;
    private final TalentType talentType;

    public AbilityGrantTalent(Holder<MKAbility> ability, TalentType talentType) {
        this.ability = ability;
        this.talentType = talentType;
    }

    public MKAbility getAbility() {
        return ability.value();
    }

    @Override
    public TalentType getTalentType() {
        return talentType;
    }

    @Override
    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        super.describeTalent(entityData, record, consumer);
        consumer.accept(ability.value().getAbilityName());
        ability.value().buildDescription(entityData, AbilityContext.forCaster(entityData, getAbility()), consumer);

    }

    @Override
    public String toString() {
        return "AbilityGrantTalent{" +
                "ability=" + ability.value() +
                ", talentType=" + talentType +
                '}';
    }
}
