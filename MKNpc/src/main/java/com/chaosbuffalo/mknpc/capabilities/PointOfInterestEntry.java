package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PointOfInterestEntry {
    public static final Codec<PointOfInterestEntry> CODEC = RecordCodecBuilder.<PointOfInterestEntry>mapCodec(builder -> {
        return builder.group(
                GlobalPos.CODEC.fieldOf("location").forGetter(PointOfInterestEntry::getLocation),
                Codec.STRING.optionalFieldOf("label").forGetter(i -> Optional.ofNullable(i.getLabel())),
                UUIDUtil.CODEC.optionalFieldOf("structureId").forGetter(i -> Optional.ofNullable(i.getStructureId())),
                UUIDUtil.CODEC.fieldOf("pointId").forGetter(PointOfInterestEntry::getPointId)
        ).apply(builder, PointOfInterestEntry::new);
    }).codec();

    private final GlobalPos location;
    private final String label;
    @Nullable
    private final UUID structureId;
    private final UUID pointId;

    private PointOfInterestEntry(GlobalPos location, Optional<String> label, Optional<UUID> structureId, UUID pointId) {
        this.location = location;
        this.label = label.orElse(null);
        this.structureId = structureId.orElse(null);
        this.pointId = pointId;
    }

    public PointOfInterestEntry(MKPoiTileEntity entity) {
        this.location = entity.getGlobalPos();
        this.label = entity.getPoiTag();
        this.structureId = entity.getStructureId();
        this.pointId = entity.getPoiID();
    }

    public GlobalPos getLocation() {
        return location;
    }

    public UUID getPointId() {
        return pointId;
    }

    @Nullable
    public UUID getStructureId() {
        return structureId;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static PointOfInterestEntry deserialize(Tag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
