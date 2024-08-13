package com.chaosbuffalo.mknpc.npc.options;

import com.mojang.serialization.Codec;

public interface NpcOptionType<T extends NpcDefinitionOption> {
    Codec<T> codec();
}
