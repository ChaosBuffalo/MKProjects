package com.chaosbuffalo.mkcore.abilities.training.requirements;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ExperienceLevelRequirement extends AbilityTrainingRequirement {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "training_req.exp_level");
    protected final IntAttribute requiredLevel = new IntAttribute("reqLevel", 0);

    public ExperienceLevelRequirement(int reqLevel) {
        super(TYPE_NAME);
        requiredLevel.setValue(reqLevel);
    }


    public <D> ExperienceLevelRequirement(Dynamic<D> dynamic) {
        super(TYPE_NAME, dynamic);
    }

    @Override
    protected void setupAttributes() {
        super.setupAttributes();
        addAttribute(requiredLevel);
    }

    @Override
    public boolean check(MKPlayerData playerData, MKAbility ability) {
        Player playerEntity = playerData.getEntity();
        return playerEntity.experienceLevel >= requiredLevel.value();
    }

    @Override
    public void onLearned(MKPlayerData playerData, MKAbility ability) {
        Player playerEntity = playerData.getEntity();
        playerEntity.giveExperienceLevels(-requiredLevel.value());
    }

    @Override
    public MutableComponent describe(MKPlayerData playerData) {
        return new TextComponent(String.format("You must be at least level %d", requiredLevel.value()));
    }


}
