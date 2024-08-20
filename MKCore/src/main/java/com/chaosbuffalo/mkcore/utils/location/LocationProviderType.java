package com.chaosbuffalo.mkcore.utils.location;

import com.mojang.serialization.MapCodec;

public interface LocationProviderType<T extends LocationProvider> {
    MapCodec<T> codec();
}
