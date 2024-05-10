package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.client.sound.MovingSoundCasting;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;

class ClientCastingState extends EntityCastingState {
    protected MovingSoundCasting sound;
    protected boolean playing = false;
    @Nullable
    protected final AbilityClientState clientState;

    public ClientCastingState(AbilityExecutor executor, MKAbility ability, int castTicks, @Nullable AbilityClientState clientState) {
        super(executor, ability, castTicks);
        this.clientState = clientState;
    }

    private void stopSound() {
        if (playing && sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            playing = false;
        }
    }

    private void startSound() {
        SoundEvent event = ability.getCastingSoundEvent();
        if (event != null) {
            sound = new MovingSoundCasting(executor.entityData.getEntity(), event, castTicks);
            Minecraft.getInstance().getSoundManager().play(sound);
            playing = true;
        }
    }

    @Override
    void begin() {
        startSound();
    }

    @Override
    void activeTick() {
        ability.continueCastClient(executor.entityData, castTicks, totalTicks, clientState);
    }

    @Override
    public void finish() {
        stopSound();
        ability.endCastClient(executor.entityData, clientState);
    }

    @Override
    public void interrupt(CastInterruptReason reason) {
        super.interrupt(reason);
        stopSound();
    }
}
