package com.chaosbuffalo.mkcore.abilities2.definition;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record AbilityPresentation(
        String name,
        String description,
        @Nullable ResourceLocation icon,
        @Nullable ResourceLocation castingParticles,
        @Nullable ResourceLocation completeParticles,
        @Nullable ResourceLocation castingSound,
        @Nullable ResourceLocation completeSound
) {
    public AbilityPresentation {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ability presentation name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Ability presentation description must not be blank");
        }
    }
}
