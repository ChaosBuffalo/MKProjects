package com.chaosbuffalo.mkchat.dialogue.effects;

import com.mojang.serialization.Codec;

public interface DialogueEffectType<T extends DialogueEffect> {

    Codec<T> codec();
}
