package com.chaosbuffalo.mknpc.quest.data.objective;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.Util;

import java.util.UUID;

public class UUIDInstanceData extends ObjectiveInstanceData{

    private UUID uuid;
    private boolean isValid;

    public UUIDInstanceData(){
        uuid = Util.NIL_UUID;
        isValid = false;
    }

    public UUIDInstanceData(UUID uuid){
        this.uuid = uuid;
        isValid = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", uuid);
        tag.putBoolean("isValid", isValid);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        uuid = nbt.getUUID("id");
        isValid = nbt.getBoolean("isValid");
    }
}
