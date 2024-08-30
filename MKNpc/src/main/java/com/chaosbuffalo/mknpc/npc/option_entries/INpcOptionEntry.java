package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface INpcOptionEntry extends INBTSerializable<CompoundTag> {
   Codec<INpcOptionEntry> CODEC = Codec.lazyInitialized(() ->
            NpcRegistries.NPC_OPTION_ENTRY_TYPES.byNameCodec().dispatch(INpcOptionEntry::getType, NpcOptionEntryType::codec));

    ResourceLocation getOptionId();

    void applyToEntity(Entity entity);

    default boolean isValid() {
        return true;
    }

    NpcOptionEntryType<? extends INpcOptionEntry> getType();
}
