package com.chaosbuffalo.mkcore.abilities.client_state;

import com.mojang.serialization.MapCodec;

public interface AbilityClientStateType<T extends AbilityClientState> {
    MapCodec<T> codec();
}
