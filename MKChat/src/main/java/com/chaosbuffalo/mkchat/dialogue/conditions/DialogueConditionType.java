package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.mojang.serialization.Codec;

public interface DialogueConditionType<T extends DialogueCondition> {
    Codec<T> codec();
}
