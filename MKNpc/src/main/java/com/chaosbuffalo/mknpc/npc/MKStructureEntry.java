package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.core.AbilityTracker;
import com.chaosbuffalo.mknpc.capabilities.IChestNpcData;
import com.chaosbuffalo.mknpc.capabilities.PointOfInterestEntry;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureData;
import com.chaosbuffalo.mknpc.content.QuestObjectDB;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.spawn.SpawnOption;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.chaosbuffalo.mknpc.utils.NBTSerializableMappedData;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MKStructureEntry implements INBTSerializable<CompoundTag> {
    private ResourceLocation structureName;
    private UUID structureId;
    private ChunkPos chunkPos;
    private final List<NotableChestEntry> notableChests;
    private final List<NotableNpcEntry> notables;
    private final Map<String, List<PointOfInterestEntry>> pois;
    private final Set<ResourceLocation> mobs;
    private final Set<ResourceLocation> factions;
    @Nullable
    private StructureData structureData;
    private final NBTSerializableMappedData customStructureData;
    private final AbilityTracker cooldownTracker;
    private final Set<String> activeEvents = new HashSet<>();

    public MKStructureEntry(ResourceLocation structureName, UUID structureId, StructureStart start,
                            @Nullable StructureData structureData) {
        this();
        this.structureName = structureName;
        this.structureId = structureId;
        this.chunkPos = start.getChunkPos();
        this.structureData = structureData;
    }

    public MKStructureEntry() {
        chunkPos = ChunkPos.ZERO;
        notables = new ArrayList<>();
        mobs = new HashSet<>();
        factions = new HashSet<>();
        notableChests = new ArrayList<>();
        pois = new HashMap<>();
        structureData = null;
        customStructureData = new NBTSerializableMappedData();
        cooldownTracker = new AbilityTracker();
    }

    public AbilityTracker getCooldownTracker() {
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
        return chunkPos;
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

    public boolean hasNotableOfType(ResourceLocation npcDef) {
        return notables.stream().anyMatch(x -> x.getDefinition() != null && x.getDefinition().getDefinitionName().equals(npcDef));
    }

    public Optional<NotableNpcEntry> getFirstNotableOfType(ResourceLocation npcDef) {
        return notables.stream().filter(x -> x.getDefinition() != null && x.getDefinition().getDefinitionName().equals(npcDef)).findFirst();
    }

    public List<NotableNpcEntry> getAllNotablesOfType(ResourceLocation npcDef) {
        return notables.stream().filter(x -> x.getDefinition() != null && x.getDefinition().getDefinitionName().equals(npcDef)).collect(Collectors.toList());
    }

    public Optional<NotableChestEntry> getFirstChestWithTag(String tag) {
        return notableChests.stream().filter(x -> x.getLabel() != null && x.getLabel().equals(tag)).findFirst();
    }

    public List<NotableChestEntry> getChestsWithTag(String tag) {
        return notableChests.stream().filter(x -> x.getLabel() != null && x.getLabel().equals(tag)).collect(Collectors.toList());
    }

    public UUID getStructureId() {
        return structureId;
    }

    public ResourceLocation getStructureName() {
        return structureName;
    }

    public void addSpawner(MKSpawnerTileEntity spawner) {
        for (SpawnOption spawnOption : spawner.getSpawnList().getOptions()) {
            NpcDefinition def = spawnOption.getDefinition();
            if (def.isNotable()) {
                NotableNpcEntry entry = new NotableNpcEntry(def, spawner);
                QuestObjectDB.putNotableNpc(entry);
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

    private void putPoi(PointOfInterestEntry entry) {
        pois.computeIfAbsent(entry.getLabel(), (key) -> new ArrayList<>()).add(entry);
        QuestObjectDB.putNotablePOI(entry);
    }

    public NBTSerializableMappedData getCustomData() {
        return customStructureData;
    }

    public void addPOI(MKPoiTileEntity poi) {
        PointOfInterestEntry entry = new PointOfInterestEntry(poi);
        putPoi(entry);
    }

    public void addChest(IChestNpcData chestData) {
        NotableChestEntry entry = new NotableChestEntry(chestData);
        QuestObjectDB.putNotableChest(entry);
        notableChests.add(entry);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("structureName", structureName.toString());
        tag.putUUID("structureId", structureId);
        tag.putLong("structurePos", chunkPos.toLong());
        ListTag notablesNbt = new ListTag();
        for (NotableNpcEntry notableEntry : notables) {
            notablesNbt.add(notableEntry.serializeNBT());
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
            tag.put("structureData", structureData.serializeNBT());
        }
        ListTag chestNbt = new ListTag();
        for (NotableChestEntry chest : notableChests) {
            chestNbt.add(chest.serializeNBT());
        }
        tag.put("chests", chestNbt);
        CompoundTag poiTag = new CompoundTag();
        for (String key : pois.keySet()) {
            ListTag poiList = new ListTag();
            for (PointOfInterestEntry entry : pois.get(key)) {
                poiList.add(entry.serializeNBT());
            }
            poiTag.put(key, poiList);
        }
        tag.put("pois", poiTag);
        if (!customStructureData.isEmpty()) {
            tag.put("customData", customStructureData.serializeNBT());
        }
        tag.put("cooldowns", cooldownTracker.serialize());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        structureName = new ResourceLocation(nbt.getString("structureName"));
        structureId = nbt.getUUID("structureId");
        chunkPos = new ChunkPos(nbt.getLong("structurePos"));
        ListTag notablesNbt = nbt.getList("notables", Tag.TAG_COMPOUND);
        for (Tag notTag : notablesNbt) {
            NotableNpcEntry newEntry = new NotableNpcEntry();
            newEntry.deserializeNBT((CompoundTag) notTag);
            QuestObjectDB.putNotableNpc(newEntry);
            notables.add(newEntry);
        }
        ListTag mobNbt = nbt.getList("mobs", Tag.TAG_STRING);
        for (Tag mobName : mobNbt) {
            ResourceLocation mobLoc = new ResourceLocation(mobName.getAsString());
            mobs.add(mobLoc);
        }
        ListTag factionNbt = nbt.getList("factions", Tag.TAG_STRING);
        for (Tag factionName : factionNbt) {
            ResourceLocation factionLoc = new ResourceLocation(factionName.getAsString());
            factions.add(factionLoc);
        }
        if (nbt.contains("structureData")) {
            structureData = new StructureData();
            structureData.deserializeNBT(nbt.getCompound("structureData"));
        }
        ListTag chestNbt = nbt.getList("chests", Tag.TAG_COMPOUND);
        for (Tag chest : chestNbt) {
            NotableChestEntry chestEntry = new NotableChestEntry();
            chestEntry.deserializeNBT((CompoundTag) chest);
            QuestObjectDB.putNotableChest(chestEntry);
            notableChests.add(chestEntry);
        }
        pois.clear();
        CompoundTag poiNbt = nbt.getCompound("pois");
        for (String key : poiNbt.getAllKeys()) {
            ListTag poiLNbt = poiNbt.getList(key, Tag.TAG_COMPOUND);
            for (Tag poi : poiLNbt) {
                PointOfInterestEntry entry = new PointOfInterestEntry();
                entry.deserializeNBT((CompoundTag) poi);
                putPoi(entry);
            }
        }
        if (nbt.contains("customData")) {
            customStructureData.deserializeNBT(nbt.getCompound("customData"));
        }
        if (nbt.contains("cooldowns")) {
            cooldownTracker.deserialize(nbt.getCompound("cooldowns"));
        }
    }

    public Optional<MKJigsawStructure> getStructure() {
        return Optional.ofNullable(WorldStructureHandler.MK_STRUCTURE_INDEX.get(structureName));
    }

    public void reset() {
        cooldownTracker.removeAll();
    }
}
