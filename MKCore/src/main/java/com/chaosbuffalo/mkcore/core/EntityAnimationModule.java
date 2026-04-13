package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;

import java.util.Collection;

public class EntityAnimationModule implements ISyncGroupProvider {
    public enum VisualCastState {
        NONE,
        CASTING,
        RELEASE
    }

    private static final int RELEASE_ANIM_TICKS = 15;

    private final SyncGroup syncGroup = new SyncGroup();
    private final IMKEntityData entityData;
    private final ParticleEffectInstanceTracker effectInstanceTracker;
    private int castAnimTimer;
    private VisualCastState visualCastState;
    private MKAbility castingAbility;
    private int castTicks;
    private int currentCastTicks;

    public EntityAnimationModule(IMKEntityData entityData) {
        this.entityData = entityData;
        this.effectInstanceTracker = ParticleEffectInstanceTracker.getTracker(entityData.getEntity());
        this.visualCastState = VisualCastState.NONE;
        syncGroup.addPublic("effects", effectInstanceTracker);
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    public IMKEntityData getEntityData() {
        return entityData;
    }

    public ParticleEffectInstanceTracker getEffectInstanceTracker() {
        return effectInstanceTracker;
    }

    public Collection<ParticleEffectInstance> getParticleInstances() {
        return effectInstanceTracker.getParticleInstances();
    }

    public MKAbility getCastingAbility() {
        return castingAbility;
    }

    public VisualCastState getVisualCastState() {
        return visualCastState;
    }

    public int getCastAnimTimer() {
        return castAnimTimer;
    }

    public int getCastTicks() {
        return castTicks;
    }

    public int getCurrentCastTicks() {
        return currentCastTicks;
    }

    public float getCastRatio() {
        if (castTicks == 0) {
            return 0.0f;
        }
        return Math.min((float) currentCastTicks / castTicks, 1.0f);
    }

    public float getReleaseRatio() {
        if (castAnimTimer <= 0) {
            return 0.0f;
        }
        return 1.0f - Math.min((float) castAnimTimer / RELEASE_ANIM_TICKS, 1.0f);
    }

    public void tick() {
        if (visualCastState == VisualCastState.CASTING) {
            currentCastTicks++;
        }
        if (castAnimTimer > 0) {
            castAnimTimer--;
            if (castAnimTimer == 0) {
                castingAbility = null;
                visualCastState = VisualCastState.NONE;
            }
        }
    }

    public void startCast(MKAbility ability, int totalTicks) {
        visualCastState = VisualCastState.CASTING;
        castingAbility = ability;
        castTicks = totalTicks;
        currentCastTicks = 0;
        castAnimTimer = 0;
    }

    public void endCast(MKAbility ability) {
        castingAbility = ability;
        visualCastState = VisualCastState.RELEASE;
        castAnimTimer = RELEASE_ANIM_TICKS;
        currentCastTicks = 0;
        castTicks = 0;
    }

    public void interruptCast(MKAbility ability, CastInterruptReason reason) {
        castingAbility = null;
        castAnimTimer = 0;
        visualCastState = VisualCastState.NONE;
        currentCastTicks = 0;
        castTicks = 0;
    }
}
