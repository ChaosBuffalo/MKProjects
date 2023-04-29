package com.chaosbuffalo.mkcore.core.talents.talent_types;

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
    private final TalentType<?> talentType;
    private final Supplier<MKAbilityInfo> abilityInfo;

    @Deprecated
    public AbilityGrantTalent(Supplier<? extends MKAbility> ability, TalentType<?> talentType) {
        this.talentType = talentType;
        abilityInfo = () -> ability.get().getDefaultInstance();
    }

    public AbilityGrantTalent(TalentType<?> talentType, Supplier<MKAbilityInfo> abilityInfo) {
        this.talentType = talentType;
        this.abilityInfo = abilityInfo;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo.get();
    }

    @Override
    public TalentType<?> getTalentType() {
        return talentType;
    }

    @Override
    public void describeTalent(IMKEntityData entityData, TalentRecord record, Consumer<Component> consumer) {
        super.describeTalent(entityData, record, consumer);
        MKAbilityInfo abilityInfo = getAbilityInfo();
        consumer.accept(abilityInfo.getAbilityName());
        abilityInfo.getAbility().buildDescription(entityData, consumer);
    }

    @Override
    public String toString() {
        return "AbilityGrantTalent{" +
                "ability=" + getAbilityInfo() +
                ", talentType=" + talentType +
                '}';
    }
}
