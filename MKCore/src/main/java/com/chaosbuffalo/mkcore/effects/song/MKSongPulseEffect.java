package com.chaosbuffalo.mkcore.effects.song;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.MKSongAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.UUID;

public class MKSongPulseEffect extends MKEffect {
    public MKSongPulseEffect() {
        super(MobEffectCategory.NEUTRAL);
    }

    @Override
    public SongPulseState makeState() {
        return new SongPulseState();
    }

    @Override
    public MKEffectBuilder<SongPulseState> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public static class SongPulseState extends MKSongStateBase {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            MKAbilityInfo abilityInfo = getSongAbility(targetData, instance);
            MKCore.LOGGER.info("MKSongPulseEffect.performEffect {} {}", instance, abilityInfo);
            if (abilityInfo == null || !(abilityInfo.getAbility() instanceof MKSongAbility songAbility)) {
                return false;
            }

            AreaEffectBuilder area = AreaEffectBuilder.createOnCaster(targetData.getEntity());
            songAbility.addPulseAreaEffects(targetData, abilityInfo, area);

            area.instant()
                    .particle(songAbility.getSongPulseParticle())
                    .color(16762905)
                    .radius(songAbility.getSongDistance(targetData, instance), true)
                    .spawn();

            return true;
        }
    }
}
