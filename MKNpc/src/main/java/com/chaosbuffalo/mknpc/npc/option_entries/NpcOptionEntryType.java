package com.chaosbuffalo.mknpc.npc.option_entries;

import com.mojang.serialization.Codec;

public interface NpcOptionEntryType<T extends INpcOptionEntry> {
    Codec<T> codec();
}
