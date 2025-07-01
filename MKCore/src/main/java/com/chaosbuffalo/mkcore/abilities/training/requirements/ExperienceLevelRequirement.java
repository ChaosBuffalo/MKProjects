package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ExperienceLevelRequirement extends AbilityTrainingRequirement {
    public static final ResourceLocation TYPE_NAME = MKCore.id("training_req.exp_level");
    public static final MapCodec<ExperienceLevelRequirement> CODEC = Codec.INT.xmap(ExperienceLevelRequirement::new, i -> i.requiredLevel).fieldOf("level");

    private final int requiredLevel;

    public ExperienceLevelRequirement(int reqLevel) {
        super(TYPE_NAME);
        requiredLevel = reqLevel;
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        Player playerEntity = playerData.getEntity();
        return playerEntity.experienceLevel >= requiredLevel;
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {
        Player playerEntity = playerData.getEntity();
        playerEntity.giveExperienceLevels(-requiredLevel);
    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        return Component.literal(String.format("You must be at least level %d", requiredLevel));
    }
}
