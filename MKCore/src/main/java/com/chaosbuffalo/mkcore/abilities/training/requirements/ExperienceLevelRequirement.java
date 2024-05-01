package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ExperienceLevelRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.exp_level");
    public static final Codec<ExperienceLevelRequirement> CODEC = Codec.INT.xmap(ExperienceLevelRequirement::new, i -> i.requiredLevel);

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
