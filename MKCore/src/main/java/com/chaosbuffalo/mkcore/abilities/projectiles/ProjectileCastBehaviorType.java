package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.mojang.serialization.Codec;

public interface ProjectileCastBehaviorType<T extends ProjectileCastBehavior> {
    Codec<T> codec();
}
