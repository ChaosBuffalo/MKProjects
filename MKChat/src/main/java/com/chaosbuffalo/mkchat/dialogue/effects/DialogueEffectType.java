package com.chaosbuffalo.mkchat.dialogue.effects;

import com.mojang.serialization.MapCodec;

public interface DialogueEffectType<T extends DialogueEffect> {

    MapCodec<T> codec();
}
