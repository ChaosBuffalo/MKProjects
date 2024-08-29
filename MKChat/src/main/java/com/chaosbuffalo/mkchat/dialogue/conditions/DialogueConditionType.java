package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.mojang.serialization.MapCodec;

public interface DialogueConditionType<T extends DialogueCondition> {
    MapCodec<T> codec();
}
