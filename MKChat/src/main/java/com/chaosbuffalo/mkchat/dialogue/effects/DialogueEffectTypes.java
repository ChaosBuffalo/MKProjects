package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.MKChat;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DialogueEffectTypes {
    public static final DeferredRegister<DialogueEffectType<?>> REGISTRY = DeferredRegister.create(ChatRegistries.EFFECT_TYPES_REGISTRY_NAME, MKChat.MODID);


    public static final Supplier<DialogueEffectType<AddFlagEffect>> ADD_FLAG = REGISTRY.register("add_flag", () -> () -> AddFlagEffect.CODEC);

    public static final Supplier<DialogueEffectType<AddLevelEffect>> ADD_LEVEL = REGISTRY.register("add_level", () -> () -> AddLevelEffect.CODEC);
}
