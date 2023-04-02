package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class PointOfInterestEntry implements INBTSerializable<CompoundTag> {

    private GlobalPos location;
    private String label;
    private UUID structureId;
    private UUID pointId;

    public PointOfInterestEntry(MKPoiTileEntity entity) {
        this.location = entity.getGlobalBlockPos();
        this.label = entity.getPoiTag();
        this.structureId = entity.getStructureId();
        this.pointId = entity.getPoiID();
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
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("location", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, getLocation())
                .getOrThrow(false, MKNpc.LOGGER::error));
        tag.putUUID("pointId", pointId);
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
        pointId = nbt.getUUID("pointId");
        structureId = nbt.getUUID("structureId");
        if (nbt.contains("label")){
            label = nbt.getString("label");
        }
    }
}
