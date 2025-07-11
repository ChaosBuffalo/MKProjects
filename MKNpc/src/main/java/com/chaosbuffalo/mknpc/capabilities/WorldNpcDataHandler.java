package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureComponentData;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureData;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.npc.*;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.generation.QuestChainBuildResult;
import com.chaosbuffalo.mknpc.block_entities.MKPoiBlockEntity;
import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import com.chaosbuffalo.mknpc.world.gen.StructureStartExtension;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKStructure;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class WorldNpcDataHandler implements IWorldNpcData {

    private final HashMap<UUID, WorldPermanentSpawnConfiguration> worldPermanentSpawnConfigurations;
    private final HashMap<UUID, MKStructureEntry> structureIndex;
    private final HashMap<ResourceLocation, List<UUID>> structureToInstanceIndex;
    private final HashMap<UUID, QuestChainInstance> quests;
    private final HashMap<UUID, NotableChestEntry> notableChests;
    private final HashMap<UUID, NotableNpcEntry> notableNpcs;
    private final HashMap<UUID, PointOfInterestEntry> pointOfInterests;
    private final WorldStructureManager structureManager;
    private final ConcurrentLinkedQueue<GlobalPos> chestsToProcess;
    private final Level world;

    public WorldNpcDataHandler(Level world) {
        this.world = world;
        worldPermanentSpawnConfigurations = new HashMap<>();
        structureIndex = new HashMap<>();
        structureToInstanceIndex = new HashMap<>();
        notableChests = new HashMap<>();
        notableNpcs = new HashMap<>();
        quests = new HashMap<>();
        pointOfInterests = new HashMap<>();
        structureManager = new WorldStructureManager(this);
        chestsToProcess = new ConcurrentLinkedQueue<>();
    }


    @Override
    public QuestChainInstance getQuest(UUID questId) {
        return quests.get(questId);
    }

    @Override
    public boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, Entity entity) {
        UUID spawnId = getSpawnIdForEntity(entity);
        return hasEntityOptionEntry(definition, attribute, spawnId);
    }

    public void putNotableChest(NotableChestEntry notableChestEntry) {
        notableChests.put(notableChestEntry.getChestId(), notableChestEntry);
    }

    @Override
    public NotableChestEntry getNotableChest(UUID id) {
        return notableChests.get(id);
    }

    @Override
    public NotableNpcEntry getNotableNpc(UUID id) {
        return notableNpcs.get(id);
    }

    @Override
    public void setupStructureDataIfAbsent(StructureStart start, Level level) {
        UUID instanceId = StructureStartExtension.getInstanceId(start);
        Structure struct = start.getStructure();
        ResourceLocation structId = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(struct);
        if (hasStructureInstance(structId, instanceId))
            return;
        StructureData structureData = new StructureData(level.dimension(),
                start, this::getComponentDataFromPiece);
        MKStructureEntry structureEntry = new MKStructureEntry(this,
                structId, instanceId, structureData);
        indexStructureEntry(structureEntry);
    }

    @Override
    public PointOfInterestEntry getPointOfInterest(UUID id) {
        return pointOfInterests.get(id);
    }

    public void putNotableNpc(NotableNpcEntry notableNpcEntry) {
        notableNpcs.put(notableNpcEntry.getNotableId(), notableNpcEntry);
    }

    public void putNotablePOI(PointOfInterestEntry entry) {
        pointOfInterests.put(entry.getPointId(), entry);
    }

    @Override
    public boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, UUID spawnId) {
        return worldPermanentSpawnConfigurations.containsKey(spawnId) &&
                worldPermanentSpawnConfigurations.get(spawnId).hasAttributeEntry(
                        definition.getDefinitionName(), attribute.getName());
    }

    public static UUID getSpawnIdForEntity(Entity entity) {
        return MKNpc.getNpcData(entity).map(IEntityNpcData::getSpawnID).orElse(entity.getUUID());
    }

    @Override
    public INpcOptionEntry getEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, Entity entity) {
        UUID spawnId = getSpawnIdForEntity(entity);
        return getEntityOptionEntry(definition, attribute, spawnId);
    }

    @Override
    public INpcOptionEntry getEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, UUID spawnId) {
        return worldPermanentSpawnConfigurations.get(spawnId).getOptionEntry(definition, attribute);
    }

    @Override
    public void addEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                                     UUID spawnId, INpcOptionEntry entry) {
        worldPermanentSpawnConfigurations.computeIfAbsent(
                spawnId, (id) -> new WorldPermanentSpawnConfiguration()).addAttributeEntry(definition, attribute, entry);
    }

    @Override
    public Optional<QuestChainBuildResult> buildQuest(QuestDefinition definition, BlockPos pos) {
        Set<QuestStructureLocation>structuresNeeded = definition.getStructuresNeeded();
        if (hasStructureInstances(structuresNeeded)) {
            Map<QuestStructureLocation, List<MKStructureEntry>> possibilities = structuresNeeded.stream()
                    .map(x -> new Pair<>(x, structureToInstanceIndex.get(x.getStructureId())))
                    .map(x -> x.mapSecond(ids -> ids.stream().map(structureIndex::get)
                            .filter(Objects::nonNull)
                            .filter(y -> definition.doesStructureMeetRequirements(x.getFirst(), y))))
                    .collect(Collectors.toMap(Pair::getFirst, pair -> pair.getSecond().collect(Collectors.toList())));
            if (possibilities.entrySet().stream().anyMatch(x -> x.getValue().isEmpty())) {
                return Optional.empty();
            } else {
                // sort by number of possibilites so we pick the structure with the least possibilites first
                var inOrder = possibilities.entrySet().stream().sorted(Comparator.comparingInt(x -> x.getValue().size())).toList();
                // final choices
                Map<QuestStructureLocation, MKStructureEntry> questStructures = new HashMap<>();
                for (var entry : inOrder) {
                    List<MKStructureEntry> byDistance = entry.getValue().stream().sorted(Comparator.comparingInt(
                                    x -> new ChunkPos(pos).getChessboardDistance(x.getChunkPos())))
                            .toList();
                    for (var found : byDistance) {
                        if (questStructures.values().stream().noneMatch(x -> x.getStructureId().equals(found.getStructureId()))) {
                            questStructures.put(entry.getKey(), found);
                            break;
                        }
                    }
                }
                // check if we found a structure for each possibility
                if (questStructures.size() == inOrder.size()) {
                    QuestChainInstance instance = definition.generate(questStructures, getWorld());
                    instance.generateDialogue(questStructures);
                    MKNpc.LOGGER.debug("Built quest {} for {}", instance.getQuestId(), definition.getName());
                    quests.put(instance.getQuestId(), instance);
                    return Optional.of(new QuestChainBuildResult(instance, questStructures));
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasStructureInstances(Set<QuestStructureLocation> structureNames) {
        return structureNames.stream().allMatch(x -> isStructureIndexed(x.getStructureId()));
    }

    private MKStructureEntry computeStructureEntry(GlobalPos structurePos, ResourceLocation structureName, UUID structureId) {
        StructureData structureData = null;
        Level structureWorld = getWorld().getServer().getLevel(structurePos.dimension());
        if (structureWorld instanceof ServerLevel serverLevel) {
            MKStructure struct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(structureName);
            if (struct != null) {
                StructureStart start = serverLevel.structureManager()
                        .getStructureAt(structurePos.pos(), struct);
                structureData = new StructureData(structureWorld.dimension(),
                        start, this::getComponentDataFromPiece);
            }

        }
        MKStructureEntry structureEntry = new MKStructureEntry(this, structureName, structureId, structureData);
        indexStructureEntry(structureEntry);
        return structureEntry;
    }

    private MKStructureEntry computeStructureEntry(IStructurePlaced structurePlaced) {
        StructureData structureData = null;
        Level structureWorld = structurePlaced.getStructureLevel();
        if (structureWorld instanceof ServerLevel serverLevel) {
            MKStructure struct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(structurePlaced.getStructureName());
            if (struct != null) {
                StructureStart start = serverLevel.structureManager()
                        .getStructureAt(structurePlaced.getGlobalPos().pos(), struct);
                structureData = new StructureData(structureWorld.dimension(),
                        start, this::getComponentDataFromPiece);
            }

        }
        MKStructureEntry structureEntry = new MKStructureEntry(this, structurePlaced.getStructureName(), structurePlaced.getStructureId(), structureData);
        indexStructureEntry(structureEntry);
        return structureEntry;
    }

    private void indexStructureEntry(MKStructureEntry structureEntry) {
        structureToInstanceIndex.computeIfAbsent(structureEntry.getStructureName(), key -> new ArrayList<>())
                .add(structureEntry.getStructureId());
    }

    private StructureComponentData getComponentDataFromPiece(StructurePiece piece) {
        ResourceLocation pieceName = MKNpcWorldGen.UNKNOWN_PIECE;
        if (piece instanceof PoolElementStructurePiece structurePiece) {
            if (structurePiece.getElement() instanceof MKSinglePoolElement mkPiece) {
                pieceName = mkPiece.getPieceEither().left().orElse(MKNpcWorldGen.UNKNOWN_PIECE);
            }
        }
        return new StructureComponentData(pieceName, piece.getBoundingBox());
    }

    @Override
    public void addSpawner(MKSpawnerBlockEntity spawner) {
        MKStructureEntry structure = structureIndex.computeIfAbsent(spawner.getStructureId(),
                key -> computeStructureEntry(spawner));
        structure.addSpawner(spawner);
    }

    @Override
    public void addChest(IChestNpcData chestData) {
        MKStructureEntry structure = structureIndex.computeIfAbsent(chestData.getStructureId(),
                key -> computeStructureEntry(chestData));
        structure.addChest(chestData);

    }

    @Override
    public void addPointOfInterest(MKPoiBlockEntity entity) {
        MKStructureEntry structure = structureIndex.computeIfAbsent(entity.getStructureId(),
                key -> computeStructureEntry(entity));
        structure.addPOI(entity);
    }

    @Override
    public void addPointOfInterest(GlobalPos location, String label, UUID structureId, UUID pointId, ResourceLocation structureName) {
        MKStructureEntry structure = structureIndex.computeIfAbsent(structureId,
                key -> computeStructureEntry(location, structureName, structureId));
        structure.addPOI(location, label, structureId, pointId);
    }

    @Override
    public void update() {
        structureManager.tick();
        int processCount = 0;
        while (!chestsToProcess.isEmpty() && processCount < 5) {
            processChest(chestsToProcess.poll());
            processCount++;
        }
    }

    @Override
    public WorldStructureManager getStructureManager() {
        return structureManager;
    }

    @Override
    public Optional<MKStructureEntry> getStructureData(UUID structId) {
        return Optional.ofNullable(structureIndex.get(structId));
    }

    protected boolean hasStructureInstance(ResourceLocation structId, UUID instanceId) {
        List<UUID> instances = structureToInstanceIndex.get(structId);
        return instances != null && instances.contains(instanceId);
    }

    protected boolean isStructureIndexed(ResourceLocation structureName) {
        List<UUID> instances = structureToInstanceIndex.get(structureName);
        return instances != null && !instances.isEmpty();
    }

    @Override
    public Level getWorld() {
        return world;
    }

    @Override
    public void queueChestForProcessing(GlobalPos pos) {
        chestsToProcess.add(pos);
    }

    protected void processChest(GlobalPos pos) {
        if (getWorld() instanceof ServerLevel && getWorld().getServer() != null) {
            Level chestLevel = getWorld().getServer().getLevel(pos.dimension());
            if (chestLevel != null) {
                ChunkPos chunkPos = new ChunkPos(pos.pos());
                if (!chestLevel.hasChunk(chunkPos.x, chunkPos.z)) {
                    chestsToProcess.add(pos);
                    return;
                }
                BlockEntity entity = chestLevel.getBlockEntity(pos.pos());
                if (entity != null) {
                    IChestNpcData.get(entity).ifPresent(IChestNpcData::onLoad);
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var regOps = provider.createSerializationContext(NbtOps.INSTANCE);
        CompoundTag tag = new CompoundTag();
        CompoundTag spawnConfig = new CompoundTag();
        for (UUID entityId : worldPermanentSpawnConfigurations.keySet()) {
            WorldPermanentSpawnConfiguration config = worldPermanentSpawnConfigurations.get(entityId);
            spawnConfig.put(entityId.toString(), config.serialize(regOps));
        }
        tag.put("spawnConfigs", spawnConfig);
        ListTag structuresNbt = new ListTag();
        for (MKStructureEntry structure : structureIndex.values()) {
            structuresNbt.add(structure.serializeNBT(provider));
        }
        tag.put("structures", structuresNbt);
        ListTag questNbt = new ListTag();
        for (QuestChainInstance inst : quests.values()) {
            questNbt.add(inst.serializeNBT(provider));
        }
        tag.put("quests", questNbt);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag spawnConfigNbt = nbt.getCompound("spawnConfigs");
        for (String idKey : spawnConfigNbt.getAllKeys()) {
            UUID entityId = UUID.fromString(idKey);
            WorldPermanentSpawnConfiguration config = WorldPermanentSpawnConfiguration.deserialize(NbtOps.INSTANCE, spawnConfigNbt.get(idKey));
            worldPermanentSpawnConfigurations.put(entityId, config);
        }
        ListTag structuresNbt = nbt.getList("structures", Tag.TAG_COMPOUND);
        for (Tag structureNbt : structuresNbt) {
            MKStructureEntry newStructure = new MKStructureEntry(this);
            newStructure.deserializeNBT(provider, (CompoundTag) structureNbt);
            structureIndex.put(newStructure.getStructureId(), newStructure);
            indexStructureEntry(newStructure);
        }
        ListTag questsNbt = nbt.getList("quests", Tag.TAG_COMPOUND);
        for (Tag questNbt : questsNbt) {
            QuestChainInstance inst = new QuestChainInstance(provider, (CompoundTag) questNbt, getWorld());
            quests.put(inst.getQuestId(), inst);
        }
    }
}
