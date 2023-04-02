package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;

import java.util.List;

public class PlayerAnimationModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("anim");
    private final MKPlayerData playerData;
    private final ParticleEffectInstanceTracker effectInstanceTracker;
    private int castAnimTimer;
    private PlayerVisualCastState playerVisualCastState;
    private MKAbility castingAbility;

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public enum PlayerVisualCastState {
        NONE,
        CASTING,
        RELEASE,
    }

    public PlayerAnimationModule(MKPlayerData playerData) {
        this.playerData = playerData;
        playerVisualCastState = PlayerVisualCastState.NONE;
        castAnimTimer = 0;
        castingAbility = null;
        effectInstanceTracker = ParticleEffectInstanceTracker.getTracker(playerData.getEntity());
        addSyncPublic(effectInstanceTracker);
    }

    public ParticleEffectInstanceTracker getEffectInstanceTracker() {
        return effectInstanceTracker;
    }

    public List<ParticleEffectInstance> getParticleInstances() {
        return effectInstanceTracker.getParticleInstances();
    }

    protected MKPlayerData getPlayerData() {
        return playerData;
    }

    public MKAbility getCastingAbility() {
        return castingAbility;
    }

    public PlayerVisualCastState getPlayerVisualCastState() {
        return playerVisualCastState;
    }

    public int getCastAnimTimer() {
        return castAnimTimer;
    }

    protected void updateEntityCastState() {
        if (castAnimTimer > 0) {
            castAnimTimer--;
            if (castAnimTimer == 0) {
                castingAbility = null;
                playerVisualCastState = PlayerVisualCastState.NONE;
            }
        }
    }

    public void tick() {
        updateEntityCastState();
    }

    public void startCast(MKAbility ability) {
        playerVisualCastState = PlayerVisualCastState.CASTING;
        castingAbility = ability;
    }

    public void endCast(MKAbility ability) {
        castingAbility = ability;
        playerVisualCastState = PlayerVisualCastState.RELEASE;
        castAnimTimer = 15;
    }

    public void interruptCast(MKAbility ability, CastInterruptReason reason) {
        castingAbility = null;
        castAnimTimer = 0;
        playerVisualCastState = PlayerVisualCastState.NONE;
    }
}
