package com.chaosbuffalo.mknpc.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingEntity;
import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class OpenLearnAbilitiesEffect extends DialogueEffect {
    public static ResourceLocation effectTypeName = new ResourceLocation(MKNpc.MODID, "open_learn_abilities");

    public OpenLearnAbilitiesEffect() {
        super(effectTypeName);
    }

    @Override
    public OpenLearnAbilitiesEffect copy() {
        return new OpenLearnAbilitiesEffect();
    }

    @Override
    public void applyEffect(ServerPlayer serverPlayerEntity, LivingEntity livingEntity, DialogueNode dialogueNode) {
        if (livingEntity instanceof IAbilityTrainingEntity) {
            ((IAbilityTrainingEntity) livingEntity).openTrainingGui(serverPlayerEntity);
        }
    }
}
