package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;

public abstract class DialogueEffect {
    public static final Codec<DialogueEffect> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            ChatRegistries.DIALOGUE_EFFECTS.getCodec().dispatch(DialogueEffect::getType, DialogueEffectType::codec));


    public abstract DialogueEffectType<?> getType();

    public abstract DialogueEffect copy();

    public abstract void applyEffect(ServerPlayer player, LivingEntity source, DialogueNode node);
}
