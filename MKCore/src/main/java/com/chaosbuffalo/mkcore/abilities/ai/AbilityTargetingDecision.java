package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class AbilityTargetingDecision {
    public static final AbilityTargetingDecision UNDECIDED = new AbilityTargetingDecision(null, null);

    public enum MovementSuggestion {
        STATIONARY,
        FOLLOW,
        KITE,
        MELEE
    }

    private final LivingEntity target;
    private final MovementSuggestion movementSuggestion;
    private final MKAbilityInfo abilityInfo;

    public AbilityTargetingDecision(LivingEntity target, MKAbilityInfo abilityInfo, MovementSuggestion movementSuggestion) {
        this.target = target;
        this.movementSuggestion = movementSuggestion;
        this.abilityInfo = abilityInfo;
    }

    public AbilityTargetingDecision(LivingEntity target, MKAbilityInfo abilityInfo) {
        this(target, abilityInfo, MovementSuggestion.STATIONARY);
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    @Nullable
    public LivingEntity getTargetEntity() {
        return target;
    }

    public MovementSuggestion getMovementSuggestion() {
        return movementSuggestion;
    }
}
