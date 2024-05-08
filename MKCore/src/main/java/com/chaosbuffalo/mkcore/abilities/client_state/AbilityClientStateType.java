package com.chaosbuffalo.mkcore.abilities.client_state;

import com.mojang.serialization.Codec;

public interface AbilityClientStateType<T extends AbilityClientState> {
    Codec<T> codec();
}
