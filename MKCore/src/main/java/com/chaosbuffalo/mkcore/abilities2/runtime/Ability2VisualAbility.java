package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPresentation;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.Objects;

public class Ability2VisualAbility extends MKAbility {
    private final ResourceLocation abilityId;
    private final TargetingContext targetContext;
    @Nullable
    private final SoundEvent castingSound;
    @Nullable
    private final SoundEvent completeSound;

    public Ability2VisualAbility(ResourceLocation abilityId,
                                 AbilityPresentation presentation,
                                 TargetingContext targetContext) {
        this.abilityId = Objects.requireNonNull(abilityId, "abilityId");
        Objects.requireNonNull(presentation, "presentation");
        this.targetContext = Objects.requireNonNull(targetContext, "targetContext");
        if (presentation.castingParticles() != null) {
            castingParticles.setValue(presentation.castingParticles());
        }
        this.castingSound = resolveSoundEvent(presentation.castingSound());
        this.completeSound = resolveSoundEvent(presentation.completeSound());
    }

    @Override
    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    @Override
    public TargetingContext getTargetContext() {
        return targetContext;
    }

    @Override
    public @Nullable SoundEvent getCastingSoundEvent() {
        return castingSound != null ? castingSound : super.getCastingSoundEvent();
    }

    @Override
    public @Nullable SoundEvent getSpellCompleteSoundEvent() {
        return completeSound != null ? completeSound : super.getSpellCompleteSoundEvent();
    }

    private static @Nullable SoundEvent resolveSoundEvent(@Nullable ResourceLocation soundId) {
        if (soundId == null || !BuiltInRegistries.SOUND_EVENT.containsKey(soundId)) {
            return null;
        }
        return BuiltInRegistries.SOUND_EVENT.get(soundId);
    }
}
