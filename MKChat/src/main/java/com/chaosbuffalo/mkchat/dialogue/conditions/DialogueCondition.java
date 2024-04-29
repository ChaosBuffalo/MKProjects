package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;

public abstract class DialogueCondition {
    public static final Codec<DialogueCondition> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            ChatRegistries.DIALOGUE_CONDITIONS.getCodec().dispatch(DialogueCondition::getType, DialogueConditionType::codec));

    public DialogueCondition() {
    }

    public abstract DialogueConditionType<? extends DialogueCondition> getType();

    public abstract boolean meetsCondition(ServerPlayer player, LivingEntity source);

    public boolean checkCondition(ServerPlayer player, LivingEntity source) {
        return meetsCondition(player, source);
    }

    public abstract DialogueCondition copy();
}
