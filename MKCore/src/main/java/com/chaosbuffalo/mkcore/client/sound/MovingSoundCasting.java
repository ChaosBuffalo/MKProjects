package com.chaosbuffalo.mkcore.client.sound;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;


public class MovingSoundCasting extends AbstractTickableSoundInstance {
    private final LivingEntity caster;
    private final int castTime;

    public MovingSoundCasting(LivingEntity caster, SoundEvent event, int castTime) {
        this(caster, event, caster.getSoundSource(), castTime);
    }

    public MovingSoundCasting(LivingEntity caster, SoundEvent event, SoundSource category, int castTime) {
        super(event, category);
        this.caster = caster;
        this.looping = true;
        this.delay = 0;
        this.castTime = castTime;
    }

    @Override
    public void tick() {
        if (!caster.isAlive()) {
            stop();
            return;
        }


        boolean donePlaying = MKCore.getEntityData((caster)).map(cap -> {
            AbilityExecutor executor = cap.getAbilityExecutor();
            if (!executor.isCasting()) {
                return true;
            }

            int currentCast = executor.getCastTicks();
            int lerpTime = (int) (castTime * .2f);
            int timeCasting = castTime - currentCast;
            int fadeOutPoint = castTime - lerpTime;
            if (timeCasting <= lerpTime) {
                volume = lerp(0.0f, 1.0f,
                        (float) timeCasting / (float) lerpTime);
            } else if (timeCasting >= fadeOutPoint) {
                volume = lerp(1.0f, 0.0f,
                        (float) (timeCasting - fadeOutPoint) / (float) lerpTime);
            }
            return false;
        }).orElse(true);

        if (donePlaying) {
            stop();
            return;
        }

        x = (float) caster.getX();
        y = (float) caster.getY();
        z = (float) caster.getZ();
    }

    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }
}
