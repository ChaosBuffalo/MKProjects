package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class NotableNpcEntry implements INBTSerializable<CompoundTag> {

    private GlobalPos location;
    private MutableComponent name;
    private ResourceLocation definition;
    private UUID structureId;
    private UUID spawnerId;
    private UUID notableId;

    public NotableNpcEntry(NpcDefinition definition, MKSpawnerBlockEntity spawner) {
        this.location = spawner.getGlobalPos();
        this.name = definition.getNameForEntity(spawner.getLevel(), spawner.getSpawnUUID());
        this.definition = definition.getDefinitionName();
        this.structureId = spawner.getStructureId();
        this.spawnerId = spawner.getSpawnUUID();
        this.notableId = UUID.randomUUID();
    }

    public NotableNpcEntry() {

    }

    public GlobalPos getLocation() {
        return location;
    }

    public UUID getSpawnerId() {
        return spawnerId;
    }

    public UUID getStructureId() {
        return structureId;
    }

    public UUID getNotableId() {
        return notableId;
    }

    public Component getName() {
        return name;
    }

    public ResourceKey<NpcDefinition> getDefinitionKey() {
        return ResourceKey.create(NpcRegistries.NPC_DEFINITIONS, definition);
    }

    public boolean isDefinitionValid(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(NpcRegistries.NPC_DEFINITIONS).containsKey(definition);
    }

    @Nullable
    public NpcDefinition getDefinition(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(NpcRegistries.NPC_DEFINITIONS).get(definition);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("location", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, getLocation()).getOrThrow());
        tag.putUUID("spawnerId", spawnerId);
        tag.putUUID("structureId", structureId);
        tag.putUUID("notableId", notableId);
        tag.putString("definition", definition.toString());
        tag.putString("name", name.getString());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        location = GlobalPos.CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("location")).getOrThrow();
        spawnerId = nbt.getUUID("spawnerId");
        structureId = nbt.getUUID("structureId");
        definition = ResourceLocation.parse(nbt.getString("definition"));
        name = Component.literal(nbt.getString("name"));
        notableId = nbt.getUUID("notableId");
    }
}
