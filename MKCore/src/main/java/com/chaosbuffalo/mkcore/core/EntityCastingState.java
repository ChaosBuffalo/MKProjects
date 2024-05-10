package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.resources.ResourceLocation;

public abstract class EntityCastingState {
    protected final MKAbility ability;
    protected final AbilityExecutor executor;
    protected int castTicks;
    protected int totalTicks;

    public EntityCastingState(AbilityExecutor executor, MKAbility ability, int castTicks) {
        this.executor = executor;
        this.ability = ability;
        this.castTicks = castTicks;
        this.totalTicks = castTicks;
    }

    public int getCastTicks() {
        return castTicks;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public ResourceLocation getAbilityId() {
        return ability.getAbilityId();
    }

    public boolean tick() {
        if (castTicks <= 0)
            return false;

        activeTick();
        castTicks--;
        boolean active = castTicks > 0;
        if (!active) {
            finish();
        }
        return active;
    }

    abstract void begin();

    abstract void activeTick();

    public abstract void finish();

    void interrupt(CastInterruptReason reason) {
    }
}
