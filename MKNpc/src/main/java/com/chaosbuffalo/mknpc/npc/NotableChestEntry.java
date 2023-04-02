package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class NotableChestEntry implements INBTSerializable<CompoundTag> {

    private GlobalPos location;
    @Nullable
    private String label;
    private UUID structureId;
    private UUID chestId;

    public NotableChestEntry(IChestNpcData data) {
        this.location = data.getGlobalBlockPos();
        this.label = data.getChestLabel();
        this.structureId = data.getStructureId();
        this.chestId = data.getChestId();
    }

    public NotableChestEntry() {

    }

    public GlobalPos getLocation() {
        return location;
    }

    public UUID getChestId() {
        return chestId;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("location", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, getLocation())
                .getOrThrow(false, MKNpc.LOGGER::error));
        tag.putUUID("chestId", chestId);
        tag.putUUID("structureId", structureId);
        if (label != null) {
            tag.putString("label", label);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        location = GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("location"))
                .result().orElse(GlobalPos.of(Level.OVERWORLD, NbtUtils.readBlockPos(nbt.getCompound("location"))));
        chestId = nbt.getUUID("chestId");
        structureId = nbt.getUUID("structureId");
        if (nbt.contains("label")){
            label = nbt.getString("label");
        }
    }
}
