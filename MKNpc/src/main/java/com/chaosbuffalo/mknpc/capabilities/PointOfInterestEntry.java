package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.block_entities.MKPoiBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class PointOfInterestEntry implements INBTSerializable<CompoundTag> {

    private GlobalPos location;
    private String label;
    private UUID structureId;
    private UUID pointId;

    public PointOfInterestEntry(MKPoiBlockEntity entity) {
        this(entity.getGlobalPos(), entity.getPoiTag(), entity.getStructureId(), entity.getPoiID());
        this.location = entity.getGlobalPos();
        this.label = entity.getPoiTag();
        this.structureId = entity.getStructureId();
        this.pointId = entity.getPoiID();
    }

    public PointOfInterestEntry(GlobalPos location, String label, UUID structureId, UUID pointId) {
        this.location = location;
        this.label = label;
        this.structureId = structureId;
        this.pointId = pointId;
    }

    public PointOfInterestEntry() {

    }

    public GlobalPos getLocation() {
        return location;
    }

    public UUID getPointId() {
        return pointId;
    }

    @Nullable
    public String getLabel() {
        return label;
    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("location", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, getLocation()).getOrThrow());
        tag.putUUID("pointId", pointId);
        tag.putUUID("structureId", structureId);
        if (label != null) {
            tag.putString("label", label);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        location = GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("location")).getOrThrow();
        pointId = nbt.getUUID("pointId");
        structureId = nbt.getUUID("structureId");
        if (nbt.contains("label")) {
            label = nbt.getString("label");
        }
    }
}
