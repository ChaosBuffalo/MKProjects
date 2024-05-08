package com.chaosbuffalo.mkcore.utils.location;

import com.mojang.serialization.Codec;

public interface LocationProviderType<T extends LocationProvider> {
    Codec<T> codec();
}
