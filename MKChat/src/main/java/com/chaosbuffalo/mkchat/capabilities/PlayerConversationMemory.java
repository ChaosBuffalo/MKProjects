package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerConversationMemory implements INBTSerializable<CompoundTag> {
    private UUID uuid;
    private final Map<ResourceLocation, Boolean> boolFlags;

    public PlayerConversationMemory(UUID uuid) {
        this.uuid = uuid;
        this.boolFlags = new HashMap<>();
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setBoolFlag(ResourceLocation key, boolean value) {
        boolFlags.put(key, value);
    }

    public boolean getBoolFlag(ResourceLocation key) {
        return boolFlags.getOrDefault(key, false);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("npcId", uuid);
        CompoundTag boolFlagsTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Boolean> entry : boolFlags.entrySet()) {
            boolFlagsTag.putBoolean(entry.getKey().toString(), entry.getValue());
        }
        tag.put("boolFlags", boolFlagsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        uuid = nbt.getUUID("npcId");
        boolFlags.clear();
        CompoundTag boolFlagsTag = nbt.getCompound("boolFlags");
        for (String key : boolFlagsTag.getAllKeys()) {
            boolean value = boolFlagsTag.getBoolean(key);
            boolFlags.put(new ResourceLocation(key), value);
        }
    }
}
