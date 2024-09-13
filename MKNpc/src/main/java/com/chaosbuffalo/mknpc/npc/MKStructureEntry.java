package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.core.AbilityTracker;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.capabilities.PointOfInterestEntry;
import com.chaosbuffalo.mknpc.capabilities.WorldNpcDataHandler;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureData;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.spawn.SpawnOption;
import com.chaosbuffalo.mknpc.block_entities.MKPoiBlockEntity;
import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import com.chaosbuffalo.mknpc.utils.NBTSerializableMappedData;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKStructure;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MKStructureEntry implements INBTSerializable<CompoundTag> {
    private ResourceLocation structureName;
    private UUID structureId;
    private final List<NotableChestEntry> notableChests;
    private final List<NotableNpcEntry> notables;
    private final Map<String, List<PointOfInterestEntry>> pois;
    private final Set<ResourceLocation> mobs;
    private final Set<ResourceLocation> factions;
    @Nullable
    private StructureData structureData;
    private final WorldNpcDataHandler worldData;
    private final NBTSerializableMappedData customStructureData;
    private final AbilityTracker.ExternalEventsTracker cooldownTracker;
    private final Set<String> activeEvents = new HashSet<>();

    public MKStructureEntry(WorldNpcDataHandler worldData, ResourceLocation structureName, UUID structureId, @Nullable StructureData structureData) {
        this(worldData);
        this.structureName = structureName;
        this.structureId = structureId;
        this.structureData = structureData;
    }

    public AbilityTracker.ExternalEventsTracker getCooldownTracker() {
        return cooldownTracker;
    }

    public void addActiveEvent(String name) {
        activeEvents.add(name);
    }

    public Set<String> getActiveEvents() {
        return activeEvents;
    }

    public void clearActiveEvents() {
        activeEvents.clear();
    }

    public ChunkPos getChunkPos() {
        if (structureData != null) {
            return structureData.getChunkPos();
        } else {
            return ChunkPos.ZERO;
        }
    }

    public MKStructureEntry(WorldNpcDataHandler worldData) {
        this.worldData = worldData;
        notables = new ArrayList<>();
        mobs = new HashSet<>();
        factions = new HashSet<>();
        notableChests = new ArrayList<>();
        pois = new HashMap<>();
        structureData = null;
        customStructureData = new NBTSerializableMappedData();
        cooldownTracker = new AbilityTracker.ExternalEventsTracker();
    }

    public Map<String, List<PointOfInterestEntry>> getPointsOfInterest() {
        return pois;
    }

    public List<PointOfInterestEntry> getPoisWithTag(String tag) {
        return pois.get(tag);
    }

    public Optional<PointOfInterestEntry> getFirstPoiWithTag(String tag) {
        return pois.containsKey(tag) ? pois.get(tag).stream().findFirst() : Optional.empty();
    }

    public boolean hasChestWithTag(String tag) {
        return notableChests.stream().anyMatch(x -> x.getLabel() != null && x.getLabel().equals(tag));
    }

    public boolean hasNotableOfType(ResourceLocation npcDef, RegistryAccess registryAccess) {
        return notables.stream().anyMatch(x -> {
            var definition = x.getDefinition(registryAccess);
            return definition != null && definition.getDefinitionName().equals(npcDef);
        });
    }

    public Optional<NotableNpcEntry> getFirstNotableOfType(ResourceLocation npcDef, RegistryAccess registryAccess) {
        return notables.stream().filter(x -> {
            var definition = x.getDefinition(registryAccess);
            return definition != null && definition.getDefinitionName().equals(npcDef);
        }).findFirst();
    }

    public boolean hasAnyNotableOfTypes(Set<ResourceLocation> defs, RegistryAccess registryAccess) {
        return notables.stream().anyMatch(x -> {
            var definition = x.getDefinition(registryAccess);
            return definition != null && defs.contains(definition.getDefinitionName());
        });
    }

    public boolean hasNpc(ResourceLocation npcDef, RegistryAccess registryAccess) {
        return mobs.contains(npcDef);
    }

    public List<NotableNpcEntry> getNotablesOfTypes(Set<ResourceLocation> defs, RegistryAccess registryAccess) {
        return notables.stream().filter(x -> {
            var definition = x.getDefinition(registryAccess);
            return definition != null && defs.contains(definition.getDefinitionName());
        }).collect(Collectors.toList());
    }

    public Optional<NotableNpcEntry> getRandomNotableFromTypes(Set<ResourceLocation> defs, RegistryAccess registryAccess) {
        var matches = getNotablesOfTypes(defs, registryAccess);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(getWorldData().getWorld().getRandom().nextInt(matches.size())));
    }

    public List<NotableNpcEntry> getAllNotablesOfType(ResourceLocation npcDef, RegistryAccess registryAccess) {
        return notables.stream().filter(x -> {
            var definition = x.getDefinition(registryAccess);
            return definition != null && definition.getDefinitionName().equals(npcDef);
        }).collect(Collectors.toList());
    }

    public Optional<NotableChestEntry> getFirstChestWithTag(String tag) {
        return notableChests.stream().filter(x -> x.getLabel() != null && x.getLabel().equals(tag)).findFirst();
    }

    public List<NotableChestEntry> getChestsWithTag(String tag) {
        return notableChests.stream().filter(x -> x.getLabel() != null && x.getLabel().equals(tag)).collect(Collectors.toList());
    }

    public boolean hasStructureData() {
        return structureData != null;
    }

    public UUID getStructureId() {
        return structureId;
    }

    public ResourceLocation getStructureName() {
        return structureName;
    }

    public void addSpawner(MKSpawnerBlockEntity spawner) {
        for (SpawnOption spawnOption : spawner.getSpawnList().getOptions()) {
            NpcDefinition def = spawnOption.getDefinition(getWorldData().getWorld().registryAccess());
            if (def.isNotable()) {
                NotableNpcEntry entry = new NotableNpcEntry(def, spawner);
                worldData.putNotableNpc(entry);
                notables.add(entry);
                spawner.putNotableId(def.getDefinitionName(), entry.getNotableId());
            } else {
                mobs.add(def.getDefinitionName());
            }
            factions.add(def.getFactionName());
        }
    }

    public boolean hasPoi(String name) {
        return pois.containsKey(name) && !pois.get(name).isEmpty();
    }

    public WorldNpcDataHandler getWorldData() {
        return worldData;
    }

    private void putPoi(PointOfInterestEntry entry) {
        List<PointOfInterestEntry> entries = pois.computeIfAbsent(entry.getLabel(), (key) -> new ArrayList<>());
        entries.add(entry);
        worldData.putNotablePOI(entry);
    }

    public NBTSerializableMappedData getCustomData() {
        return customStructureData;
    }

    public void addPOI(MKPoiBlockEntity poi) {
        PointOfInterestEntry entry = new PointOfInterestEntry(poi);
        putPoi(entry);
    }

    public void addChest(IChestNpcData chestData) {
        NotableChestEntry entry = new NotableChestEntry(chestData);
        worldData.putNotableChest(entry);
        notableChests.add(entry);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("structureName", structureName.toString());
        tag.putUUID("structureId", structureId);
        ListTag notablesNbt = new ListTag();
        for (NotableNpcEntry notableEntry : notables) {
            notablesNbt.add(notableEntry.serializeNBT(provider));
        }
        tag.put("notables", notablesNbt);
        ListTag mobNbt = new ListTag();
        for (ResourceLocation mob : mobs) {
            mobNbt.add(StringTag.valueOf(mob.toString()));
        }
        tag.put("mobs", mobNbt);
        ListTag factionNbt = new ListTag();
        for (ResourceLocation faction : factions) {
            factionNbt.add(StringTag.valueOf(faction.toString()));
        }
        tag.put("factions", factionNbt);
        if (structureData != null) {
            tag.put("structureData", structureData.serializeNBT(provider));
        }
        ListTag chestNbt = new ListTag();
        for (NotableChestEntry chest : notableChests) {
            chestNbt.add(chest.serializeNBT(provider));
        }
        tag.put("chests", chestNbt);
        CompoundTag poiTag = new CompoundTag();
        for (String key : pois.keySet()) {
            ListTag poiList = new ListTag();
            for (PointOfInterestEntry entry : pois.getOrDefault(key, new ArrayList<>())) {
                poiList.add(entry.serializeNBT(provider));
            }
            poiTag.put(key, poiList);
        }
        tag.put("pois", poiTag);
        if (!customStructureData.isEmpty()) {
            tag.put("customData", customStructureData.serializeNBT(provider));
        }
        tag.put("cooldowns", cooldownTracker.serialize());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        structureName = ResourceLocation.parse(nbt.getString("structureName"));
        structureId = nbt.getUUID("structureId");
        ListTag notablesNbt = nbt.getList("notables", Tag.TAG_COMPOUND);
        for (Tag notTag : notablesNbt) {
            NotableNpcEntry newEntry = new NotableNpcEntry();
            newEntry.deserializeNBT(provider, (CompoundTag) notTag);
            worldData.putNotableNpc(newEntry);
            notables.add(newEntry);
        }
        ListTag mobNbt = nbt.getList("mobs", Tag.TAG_STRING);
        for (Tag mobName : mobNbt) {
            ResourceLocation mobLoc = ResourceLocation.parse(mobName.getAsString());
            mobs.add(mobLoc);
        }
        ListTag factionNbt = nbt.getList("factions", Tag.TAG_STRING);
        for (Tag factionName : factionNbt) {
            ResourceLocation factionLoc = ResourceLocation.parse(factionName.getAsString());
            factions.add(factionLoc);
        }
        if (nbt.contains("structureData")) {
            structureData = new StructureData();
            structureData.deserializeNBT(provider, nbt.getCompound("structureData"));
        }
        ListTag chestNbt = nbt.getList("chests", Tag.TAG_COMPOUND);
        for (Tag chest : chestNbt) {
            NotableChestEntry chestEntry = new NotableChestEntry();
            chestEntry.deserializeNBT(provider, (CompoundTag) chest);
            worldData.putNotableChest(chestEntry);
            notableChests.add(chestEntry);
        }
        pois.clear();
        CompoundTag poiNbt = nbt.getCompound("pois");
        for (String key : poiNbt.getAllKeys()) {
            ListTag poiLNbt = poiNbt.getList(key, Tag.TAG_COMPOUND);
            for (Tag poi : poiLNbt) {
                PointOfInterestEntry entry = new PointOfInterestEntry();
                entry.deserializeNBT(provider, (CompoundTag) poi);
                putPoi(entry);
            }
        }
        if (nbt.contains("customData")) {
            customStructureData.deserializeNBT(provider, nbt.getCompound("customData"));
        }
        if (nbt.contains("cooldowns")) {
            cooldownTracker.deserialize(nbt.getCompound("cooldowns"));
        }
    }

    public Optional<MKStructure> getStructure() {
        return Optional.ofNullable(WorldStructureHandler.MK_STRUCTURE_INDEX.get(structureName));
    }

    public void reset() {
        cooldownTracker.removeAll();
    }
}
