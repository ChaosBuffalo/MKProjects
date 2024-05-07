package com.chaosbuffalo.mkcore.abilities.client_state;

import com.chaosbuffalo.mkcore.MKCoreRegistry;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

public abstract class AbilityClientState {

    public static final Codec<AbilityClientState> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            MKCoreRegistry.CLIENT_STATE_TYPES.getCodec().dispatch(AbilityClientState::getType, AbilityClientStateType::codec));

    public abstract AbilityClientStateType<? extends AbilityClientState> getType();
}
