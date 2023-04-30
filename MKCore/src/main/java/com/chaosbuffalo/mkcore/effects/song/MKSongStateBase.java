package com.chaosbuffalo.mkcore.effects.song;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;

public abstract class MKSongStateBase extends MKEffectState {

    protected MKAbilityInfo getSongAbility(IMKEntityData targetData, MKActiveEffect activeEffect) {
        if (targetData.getEntity().getUUID().equals(activeEffect.getSourceId())) {
            return targetData.getAbilities().getKnownAbility(activeEffect.getAbilityId());
        }
        return null;
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
