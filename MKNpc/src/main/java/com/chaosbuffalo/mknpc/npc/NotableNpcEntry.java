package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class NotableNpcEntry {
    public static final Codec<NotableNpcEntry> CODEC = RecordCodecBuilder.<NotableNpcEntry>mapCodec(builder -> {
        return builder.group(
                GlobalPos.CODEC.fieldOf("location").forGetter(NotableNpcEntry::getLocation),
                ExtraCodecs.COMPONENT.fieldOf("name").forGetter(NotableNpcEntry::getName),
                ResourceLocation.CODEC.fieldOf("definition").forGetter(NotableNpcEntry::getDefinitionName),
                UUIDUtil.CODEC.optionalFieldOf("structureId").forGetter(i -> Optional.ofNullable(i.getStructureId())),
                UUIDUtil.CODEC.fieldOf("spawnerId").forGetter(NotableNpcEntry::getSpawnerId),
                UUIDUtil.CODEC.fieldOf("notableId").forGetter(NotableNpcEntry::getNotableId)
        ).apply(builder, NotableNpcEntry::new);
    }).codec();

    private final GlobalPos location;
    private final Component name;
    private final ResourceLocation definition;
    @Nullable
    private final UUID structureId;
    private final UUID spawnerId;
    private final UUID notableId;

    private NotableNpcEntry(GlobalPos location, Component name, ResourceLocation definition,
                            Optional<UUID> structureId, UUID spawnerId, UUID notableId) {
        this.location = location;
        this.name = name;
        this.definition = definition;
        this.structureId = structureId.orElse(null);
        this.spawnerId = spawnerId;
        this.notableId = notableId;
    }

    public NotableNpcEntry(NpcDefinition definition, MKSpawnerTileEntity spawner) {
        this.location = spawner.getGlobalPos();
        this.name = definition.getNameForEntity(spawner.getLevel(), spawner.getSpawnUUID());
        this.definition = definition.getDefinitionName();
        this.structureId = spawner.getStructureId();
        this.spawnerId = spawner.getSpawnUUID();
        this.notableId = UUID.randomUUID();
    }

    public GlobalPos getLocation() {
        return location;
    }

    public UUID getSpawnerId() {
        return spawnerId;
    }

    @Nullable
    public UUID getStructureId() {
        return structureId;
    }

    public UUID getNotableId() {
        return notableId;
    }

    public Component getName() {
        return name;
    }

    @Nullable
    public NpcDefinition getDefinition() {
        return NpcDefinitionManager.getDefinition(definition);
    }

    public ResourceLocation getDefinitionName() {
        return definition;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public static NotableNpcEntry deserialize(Tag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, MKNpc.LOGGER::error);
    }
}
