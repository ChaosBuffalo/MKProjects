package com.chaosbuffalo.mknpc.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingEntity;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueEffectTypes;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class OpenLearnAbilitiesEffect extends DialogueEffect {
    private static final OpenLearnAbilitiesEffect INSTANCE = new OpenLearnAbilitiesEffect();
    public static final Codec<OpenLearnAbilitiesEffect> CODEC = Codec.unit(INSTANCE);


    @Override
    public DialogueEffectType<?> getType() {
        return NpcDialogueEffectTypes.OPEN_LEARN_ABILITIES.get();
    }

    @Override
    public OpenLearnAbilitiesEffect copy() {
        return this;
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity livingEntity, DialogueNode dialogueNode) {
        if (livingEntity instanceof IAbilityTrainingEntity trainingEntity) {
            trainingEntity.openTrainingGui(player);
        }
    }
}
