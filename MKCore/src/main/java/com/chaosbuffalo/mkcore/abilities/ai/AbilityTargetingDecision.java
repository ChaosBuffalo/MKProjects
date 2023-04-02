package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
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
    private final MKAbility ability;

    public AbilityTargetingDecision(LivingEntity target, MovementSuggestion movementSuggestion, MKAbility ability) {
        this.target = target;
        this.movementSuggestion = movementSuggestion;
        this.ability = ability;
    }

    public AbilityTargetingDecision(LivingEntity target, MKAbility ability) {
        this(target, MovementSuggestion.STATIONARY, ability);
    }

    @Nullable
    public MKAbility getAbility() {
        return ability;
    }

    @Nullable
    public LivingEntity getTargetEntity() {
        return target;
    }

    public MovementSuggestion getMovementSuggestion() {
        return movementSuggestion;
    }
}
