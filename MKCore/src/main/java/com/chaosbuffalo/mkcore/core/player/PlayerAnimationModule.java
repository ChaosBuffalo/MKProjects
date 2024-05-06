package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;

import java.util.Collection;

public class PlayerAnimationModule implements IPlayerSyncComponentProvider {
    private final PlayerSyncComponent sync = new PlayerSyncComponent("anim");
    private final MKPlayerData playerData;
    private final ParticleEffectInstanceTracker effectInstanceTracker;
    private int castAnimTimer;
    private PlayerVisualCastState playerVisualCastState;
    private MKAbilityInfo castingAbility;

    @Override
    public PlayerSyncComponent getSyncComponent() {
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

    public Collection<ParticleEffectInstance> getParticleInstances() {
        return effectInstanceTracker.getParticleInstances();
    }

    protected MKPlayerData getPlayerData() {
        return playerData;
    }

    public MKAbilityInfo getCastingAbility() {
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

    public void startCast(MKAbilityInfo abilityInfo) {
        playerVisualCastState = PlayerVisualCastState.CASTING;
        castingAbility = abilityInfo;
    }

    public void endCast(MKAbilityInfo abilityInfo) {
        castingAbility = abilityInfo;
        playerVisualCastState = PlayerVisualCastState.RELEASE;
        castAnimTimer = 15;
    }

    public void interruptCast(MKAbilityInfo abilityInfo, CastInterruptReason reason) {
        castingAbility = null;
        castAnimTimer = 0;
        playerVisualCastState = PlayerVisualCastState.NONE;
    }
}
