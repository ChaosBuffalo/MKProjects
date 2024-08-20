package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.mojang.serialization.MapCodec;

public interface ProjectileCastBehaviorType<T extends ProjectileCastBehavior> {
    MapCodec<T> codec();
}
