package com.chaosbuffalo.mknpc.npc.option_entries;

import com.mojang.serialization.MapCodec;

public interface NpcOptionEntryType<T extends INpcOptionEntry> {
    MapCodec<T> codec();
}
