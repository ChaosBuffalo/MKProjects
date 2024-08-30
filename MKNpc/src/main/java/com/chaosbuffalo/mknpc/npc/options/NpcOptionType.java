package com.chaosbuffalo.mknpc.npc.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface NpcOptionType<T extends NpcDefinitionOption> {
    MapCodec<T> codec();
}
