package com.chaosbuffalo.mkcore.effects.song;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKSongAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;

import java.util.Objects;

public abstract class MKSongStateBase extends MKEffectState {
    protected MKSongAbility songAbility;

    public MKSongAbility getSongAbility() {
        return Objects.requireNonNull(songAbility);
    }

    @Override
    public boolean validateOnLoad(MKActiveEffect activeEffect) {
        return checkSongAbility(activeEffect);
    }

    @Override
    public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
        return checkSongAbility(activeEffect);
    }

    boolean checkSongAbility(MKActiveEffect activeEffect) {
        if (songAbility == null) {
            MKAbility ability = MKCoreRegistry.getAbility(activeEffect.getAbilityId());
            if (ability instanceof MKSongAbility) {
                songAbility = (MKSongAbility) ability;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void combine(MKActiveEffect existing, MKActiveEffect otherInstance) {
        MKCore.LOGGER.info("MKSongStateBase.combine {} + {}", existing, otherInstance);
        if (otherInstance.getDuration() > existing.getDuration()) {
            existing.setDuration(otherInstance.getDuration());
        }
        existing.setStackCount(otherInstance.getStackCount());
        MKCore.LOGGER.info("MKSongStateBase.combine result {}", existing);
    }
}
