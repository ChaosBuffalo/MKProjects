package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;

public interface INpcOptionEntry {
    Codec<INpcOptionEntry> CODEC = NpcRegistries.NPC_OPTION_ENTRY_TYPES.byNameCodec()
            .dispatch(INpcOptionEntry::getType, NpcOptionEntryType::codec);

    void applyToEntity(Entity entity);

    default boolean isValid() {
        return true;
    }

    NpcOptionEntryType<? extends INpcOptionEntry> getType();
}
