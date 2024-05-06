package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AbilityGrantTalent extends MKTalent {
    private final Supplier<? extends MKAbility> ability;
    private final TalentType talentType;

    public AbilityGrantTalent(Supplier<? extends MKAbility> ability, TalentType talentType) {
        this.ability = ability;
        this.talentType = talentType;
    }

    public MKAbility getAbility() {
        return ability.get();
    }

    @Override
    public TalentType getTalentType() {
        return talentType;
    }

    @Override
    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        super.describeTalent(entityData, record, consumer);
        MKAbilityInfo abilityInfo = ability.get().getPortingInstance();
        consumer.accept(abilityInfo.getAbilityName());
        abilityInfo.getAbility().buildDescription(entityData, AbilityContext.forCaster(entityData, abilityInfo), consumer);

    }

    @Override
    public String toString() {
        return "AbilityGrantTalent{" +
                "ability=" + ability.get() +
                ", talentType=" + talentType +
                '}';
    }
}
