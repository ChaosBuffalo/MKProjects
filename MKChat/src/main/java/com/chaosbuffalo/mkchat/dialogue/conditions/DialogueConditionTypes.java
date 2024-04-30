package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.MKChat;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DialogueConditionTypes {
    public static final DeferredRegister<DialogueConditionType<?>> REGISTRY = DeferredRegister.create(ChatRegistries.CONDITION_TYPES_REGISTRY_NAME, MKChat.MODID);

    public static final Supplier<DialogueConditionType<HasFlagCondition>> HAS_FLAG = REGISTRY.register("has_flag",
            () -> () -> HasFlagCondition.CODEC);

    public static final Supplier<DialogueConditionType<InvertCondition>> INVERT = REGISTRY.register("invert",
            () -> () -> InvertCondition.CODEC);
}
