package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public abstract class AbilityTrainingRequirement {
    public static final Codec<AbilityTrainingRequirement> CODEC = AbilityManager.TRAINING_REQUIREMENT_CODEC;

    private final ResourceLocation typeName;

    public AbilityTrainingRequirement(ResourceLocation typeName) {
        this.typeName = typeName;
    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public abstract boolean check(MKPlayerData playerData, MKAbilityInfo abilityInfo);

    public abstract void onLearned(MKPlayerData playerData, MKAbilityInfo abilityInfo);

    public abstract MutableComponent describe(MKPlayerData playerData);
}
