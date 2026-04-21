package com.chaosbuffalo.mkcore.client.sound;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.AbilityExecutor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class MovingSoundCasting extends AbstractTickableSoundInstance {
    private final LivingEntity caster;
    private final int castTime;
    @Nullable
    private final ResourceLocation abilityId;

    public MovingSoundCasting(LivingEntity caster, SoundEvent event, int castTime) {
        this(caster, event, caster.getSoundSource(), castTime, null);
    }

    public MovingSoundCasting(LivingEntity caster, SoundEvent event, SoundSource category, int castTime) {
        this(caster, event, category, castTime, null);
    }

    public MovingSoundCasting(LivingEntity caster, SoundEvent event, int castTime, ResourceLocation abilityId) {
        this(caster, event, caster.getSoundSource(), castTime, abilityId);
    }

    public MovingSoundCasting(LivingEntity caster,
                              SoundEvent event,
                              SoundSource category,
                              int castTime,
                              @Nullable ResourceLocation abilityId) {
        super(event, category, caster.getRandom());
        this.caster = caster;
        this.looping = true;
        this.delay = 0;
        this.castTime = castTime;
        this.abilityId = abilityId;
    }

    @Override
    public void tick() {
        if (!caster.isAlive()) {
            stop();
            return;
        }


        boolean donePlaying = MKCore.getEntityData((caster)).map(cap -> {
            AbilityExecutor executor = cap.getAbilityExecutor();
            if (executor.isCasting()) {
                int currentCast = executor.getCastTicks();
                int lerpTime = Math.max(1, (int) (castTime * .2f));
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
            }

            if (abilityId == null || !abilityId.equals(MKCore.getAbilityRuntimeService().getClientCastAbilityId(cap))) {
                return true;
            }
            float progress = MKCore.getAbilityRuntimeService().getClientCastProgress(cap, 0.0f);
            int elapsedTicks = Math.max(0, Math.round(progress * castTime));
            int lerpTime = Math.max(1, (int) (castTime * .2f));
            int fadeOutPoint = castTime - lerpTime;
            if (elapsedTicks <= lerpTime) {
                volume = lerp(0.0f, 1.0f, (float) elapsedTicks / (float) lerpTime);
            } else if (elapsedTicks >= fadeOutPoint) {
                volume = lerp(1.0f, 0.0f, (float) (elapsedTicks - fadeOutPoint) / (float) lerpTime);
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
