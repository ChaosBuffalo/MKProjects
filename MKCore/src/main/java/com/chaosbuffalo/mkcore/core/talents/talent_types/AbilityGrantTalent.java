package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AbilityGrantTalent extends MKTalent {
    private final Supplier<? extends MKAbility> ability;
    private final TalentType<?> talentType;

    public AbilityGrantTalent(Supplier<? extends MKAbility> ability, TalentType<?> talentType) {
        this.ability = ability;
        this.talentType = talentType;
    }

    public MKAbility getAbility() {
        return ability.get();
    }

    @Override
    public TalentType<?> getTalentType() {
        return talentType;
    }

    @Override
    public void describeTalent(TalentRecord record, Consumer<Component> consumer) {
        super.describeTalent(record, consumer);
        MKCore.getEntityData(Minecraft.getInstance().player).ifPresent(x -> {
            consumer.accept(ability.get().getAbilityName());
            ability.get().buildDescription(x, consumer);
        });
    }

    @Override
    public String toString() {
        return "AbilityGrantTalent{" +
                "ability=" + ability.get() +
                ", talentType=" + talentType +
                '}';
    }
}
