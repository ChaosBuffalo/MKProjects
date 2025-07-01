package com.chaosbuffalo.mknpc.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueEffectTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class OpenLearnAbilitiesEffect extends DialogueEffect {
    private static final OpenLearnAbilitiesEffect INSTANCE = new OpenLearnAbilitiesEffect();
    public static final MapCodec<OpenLearnAbilitiesEffect> MAP_CODEC = MapCodec.unit(INSTANCE);


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
        livingEntity.getExistingData(CoreAttachments.ABILITY_TRAINER).ifPresent(trainer -> {
            trainer.openTrainingGui(player);
        });
    }
}
