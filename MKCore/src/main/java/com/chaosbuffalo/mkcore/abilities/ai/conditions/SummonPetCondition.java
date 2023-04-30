package com.chaosbuffalo.mkcore.abilities.ai.conditions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityDecisionContext;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class SummonPetCondition extends AbilityUseCondition {

    Function<MKAbilityInfo, ResourceLocation> petSlotProvider;

    public SummonPetCondition(Function<MKAbilityInfo, ResourceLocation> petSlotProvider) {
        this.petSlotProvider = petSlotProvider;
    }

    private boolean hasPet(LivingEntity entity, MKAbilityInfo abilityInfo) {
        return MKCore.getEntityData(entity)
                .map(x -> x.getPets().isPetActive(petSlotProvider.apply(abilityInfo)))
                .orElse(false);
    }

    @Nonnull
    @Override
    public AbilityTargetingDecision getDecision(MKAbilityInfo abilityInfo, AbilityDecisionContext context) {
        if (!hasPet(context.getCaster(), abilityInfo)) {
            return new AbilityTargetingDecision(context.getCaster(), abilityInfo);
        }
        return AbilityTargetingDecision.UNDECIDED;
    }

}