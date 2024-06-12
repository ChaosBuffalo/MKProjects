package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
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

public class NotableChestEntry {
    public static final Codec<NotableChestEntry> CODEC = RecordCodecBuilder.<NotableChestEntry>mapCodec(builder -> {
        return builder.group(
                GlobalPos.CODEC.fieldOf("location").forGetter(NotableChestEntry::getLocation),
                Codec.STRING.optionalFieldOf("label").forGetter(i -> Optional.ofNullable(i.getLabel())),
                UUIDUtil.CODEC.optionalFieldOf("structureId").forGetter(i -> Optional.ofNullable(i.getStructureId())),
                UUIDUtil.CODEC.fieldOf("chestId").forGetter(NotableChestEntry::getChestId)
        ).apply(builder, NotableChestEntry::new);
    }).codec();

    private final GlobalPos location;
    @Nullable
    private final String label;
    @Nullable
    private final UUID structureId;
    private final UUID chestId;

    private NotableChestEntry(GlobalPos location, Optional<String> label, Optional<UUID> structureId, UUID chestId) {
        this.location = location;
        this.label = label.orElse(null);
        this.structureId = structureId.orElse(null);
        this.chestId = chestId;
    }

    public NotableChestEntry(IChestNpcData data) {
        this.location = data.getGlobalPos();
        this.label = data.getChestLabel();
        this.structureId = data.getStructureId();
        this.chestId = data.getChestId();
    }

    public GlobalPos getLocation() {
        return location;
    }

    @Nullable
    public UUID getStructureId() {
        return structureId;
    }

    public UUID getChestId() {
        return chestId;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    public boolean hasTag(String tag) {
        return label != null && label.equals(tag);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static NotableChestEntry deserialize(Tag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
