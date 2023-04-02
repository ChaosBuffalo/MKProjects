package com.chaosbuffalo.mkcore.test.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKSongAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.song.MKSongPulseEffect;
import com.chaosbuffalo.mkcore.effects.song.MKSongSustainEffect;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class NewHealSong extends MKSongAbility {
    public NewHealSong() {
        super();
    }

    @Override
    public MKSongSustainEffect getSustainEffect() {
        return MKTestEffects.NEW_HEAL_SONG_SUSTAIN.get();
    }

    @Override
    public int getSustainEffectTicks() {
        return 18 * GameConstants.TICKS_PER_SECOND;
    }

    @Override
    public MKSongPulseEffect getPulseEffect() {
        return MKTestEffects.NEW_HEAL_SONG_PULSE.get();
    }

    @Override
    public int getPulseEffectTicks() {
        return 6 * GameConstants.TICKS_PER_SECOND;
    }

    @Override
    public void addPulseAreaEffects(IMKEntityData casterData, AreaEffectBuilder areaEffect) {
        MKEffectBuilder<?> effect = MKTestEffects.NEW_HEAL.get().builder(casterData.getEntity())
                .state(s -> s.setScalingParameters(3, 1))
                .ability(this);

        areaEffect.effect(effect, TargetingContexts.FRIENDLY);
    }
}
