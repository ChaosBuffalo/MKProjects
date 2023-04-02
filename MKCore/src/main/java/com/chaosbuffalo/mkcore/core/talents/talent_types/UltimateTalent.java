package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class UltimateTalent extends AbilityGrantTalent {

    public UltimateTalent(Supplier<? extends MKAbility> ability) {
        super(ability, TalentType.ULTIMATE);
    }
}
