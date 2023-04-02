package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class SoundEffect extends MKEffect {

    public SoundEffect() {
        super(MobEffectCategory.NEUTRAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, SoundEvent event, float pitch, float volume,
                                          SoundSource cat) {
        return CoreEffects.SOUND.get().builder(source)
                .state(s -> s.setup(event, pitch, volume, cat));
    }

    public static MKEffectBuilder<?> from(LivingEntity source, SoundEvent event, SoundSource cat) {
        return from(source, event, 1f, 1f, cat);
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends MKEffectState {
        public ResourceLocation soundEvent;
        public float volume;
        public float pitch;
        public SoundSource category;

        public void setup(SoundEvent event, float pitch, float volume, SoundSource cat) {
            soundEvent = ForgeRegistries.SOUND_EVENTS.getKey(event);
            this.volume = volume;
            this.pitch = pitch;
            this.category = cat;
        }

        public void setup(SoundEvent event, SoundSource cat) {
            setup(event, 1f, 1f, cat);
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(soundEvent);
            if (event == null)
                return false;

            if (targetData.isServerSide()) {
                SoundUtils.serverPlaySoundAtEntity(targetData.getEntity(), event, category, volume, pitch);
            }
            return true;
        }
    }
}
