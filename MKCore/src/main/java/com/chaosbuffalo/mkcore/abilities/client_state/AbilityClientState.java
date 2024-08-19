package com.chaosbuffalo.mkcore.abilities.client_state;

import com.chaosbuffalo.mkcore.MKCoreRegistry;

import com.mojang.serialization.Codec;

public abstract class AbilityClientState {

    public static final Codec<AbilityClientState> CODEC = Codec.lazyInitialized(MKCoreRegistry.CLIENT_STATE_TYPES::byNameCodec)
            .dispatch(AbilityClientState::getType, AbilityClientStateType::codec);


    public abstract AbilityClientStateType<? extends AbilityClientState> getType();
}
