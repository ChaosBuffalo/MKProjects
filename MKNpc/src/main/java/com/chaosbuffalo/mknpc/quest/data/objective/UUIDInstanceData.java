package com.chaosbuffalo.mknpc.quest.data.objective;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class UUIDInstanceData extends ObjectiveInstanceData {

    private UUID uuid;

    public UUIDInstanceData() {
        uuid = Util.NIL_UUID;
    }

    public UUIDInstanceData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", uuid);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        uuid = nbt.getUUID("id");
    }
}
