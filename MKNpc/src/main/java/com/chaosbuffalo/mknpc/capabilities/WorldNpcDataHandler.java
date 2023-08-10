package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureComponentData;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureData;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.WorldPermanentSpawnConfiguration;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.generation.QuestChainBuildResult;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSinglePoolElement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.*;
import java.util.stream.Collectors;

public class WorldNpcDataHandler implements IWorldNpcData {

    private final Map<UUID, WorldPermanentSpawnConfiguration> worldPermanentSpawnConfigurations;
    private final Map<UUID, MKStructureEntry> structureIndex;
    private final Map<ResourceLocation, List<UUID>> structureToInstanceIndex;
    private final Map<UUID, QuestChainInstance> quests;
    private final WorldStructureManager structureManager;
    private final Level level;

    public WorldNpcDataHandler(Level level) {
        this.level = level;
        worldPermanentSpawnConfigurations = new HashMap<>();
        structureIndex = new HashMap<>();
        structureToInstanceIndex = new HashMap<>();
        quests = new HashMap<>();
        structureManager = new WorldStructureManager(this);
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

    @Override
    public boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, UUID spawnId) {
        WorldPermanentSpawnConfiguration spawnOptions = worldPermanentSpawnConfigurations.get(spawnId);
        return spawnOptions != null && spawnOptions.hasAttributeEntry(definition.getDefinitionName(), attribute);
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
        worldPermanentSpawnConfigurations.computeIfAbsent(spawnId, id -> new WorldPermanentSpawnConfiguration())
                        .addAttributeEntry(definition, attribute, entry);
    }

    @Override
    public Optional<QuestChainBuildResult> buildQuest(QuestDefinition definition, BlockPos pos) {
        Map<ResourceLocation, Integer> structuresNeeded = definition.getStructuresNeeded();
        if (hasStructureInstances(structuresNeeded.keySet())) {
            Map<ResourceLocation, List<MKStructureEntry>> possibilities = structuresNeeded.keySet().stream()
                    .map(x -> new Pair<>(x, structureToInstanceIndex.get(x)))
                    .map(x -> x.mapSecond(ids -> ids.stream().map(structureIndex::get)
                            .filter(Objects::nonNull)
                            .filter(definition::doesStructureMeetRequirements)))
                    .collect(Collectors.toMap(Pair::getFirst, pair -> pair.getSecond().collect(Collectors.toList())));
            if (possibilities.entrySet().stream().allMatch(x -> x.getValue().size() >= structuresNeeded.get(x.getKey()))) {
                Map<ResourceLocation, List<MKStructureEntry>> questStructures = new HashMap<>();
                for (Map.Entry<ResourceLocation, Integer> needed : structuresNeeded.entrySet()) {
                    int toFind = needed.getValue();
                    List<MKStructureEntry> byDistance = possibilities.get(needed.getKey()).stream().sorted(Comparator.comparingInt(
                                    x -> new ChunkPos(pos).getChessboardDistance(x.getChunkPos())))
                            .collect(Collectors.toList());
                    List<MKStructureEntry> finals = new ArrayList<>();
                    for (int i = 0; i < toFind; i++) {
                        finals.add(byDistance.get(i));
                    }
                    questStructures.put(needed.getKey(), finals);
                }

                QuestChainInstance instance = definition.generate(questStructures);
                instance.generateDialogue(questStructures);
                MKNpc.LOGGER.debug("Built quest {} for {}", instance.getQuestId(), definition.getName());
                quests.put(instance.getQuestId(), instance);
                return Optional.of(new QuestChainBuildResult(instance, questStructures));
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public boolean hasStructureInstances(Set<ResourceLocation> structureNames) {
        return structureNames.stream().allMatch(this::isStructureIndexed);
    }

    private void indexStructureEntry(MKStructureEntry structureEntry) {
        UUID instanceId = structureEntry.getStructureId();
        structureIndex.put(instanceId, structureEntry);
        structureToInstanceIndex.computeIfAbsent(structureEntry.getStructureName(), key -> new ArrayList<>())
                .add(instanceId);
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
    public Optional<MKStructureEntry> findContainingStructure(IStructurePlaced structurePlaced) {
        MKStructureEntry existing = structureIndex.get(structurePlaced.getStructureId());
        if (existing != null) {
            return Optional.of(existing);
        }

        if (structurePlaced.getStructureLevel() instanceof ServerLevel serverLevel) {
            ResourceLocation structureId = structurePlaced.getStructureName();
            var struct = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureId);
            if (struct == null) {
                return Optional.empty();
            }

            StructureStart start = serverLevel.structureManager().getStructureAt(structurePlaced.getBlockPos(), struct);
            if (start.isValid()) {
                MKStructureEntry newEntry = getStructureInstance(start, serverLevel, structurePlaced.getStructureId());
                return Optional.of(newEntry);
            }
        }

        return Optional.empty();
    }

    @Override
    public MKStructureEntry getStructureInstance(StructureStart start, Level level) {
        UUID instanceId = IStructureStartMixin.getInstanceIdFromStart(start);
        return getStructureInstance(start, level, instanceId);
    }

    private MKStructureEntry getStructureInstance(StructureStart start, Level level, UUID instanceId) {
        MKStructureEntry existing = structureIndex.get(instanceId);
        if (existing != null) {
            return existing;
        }

        Structure struct = start.getStructure();
        ResourceLocation structId = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(struct);
        StructureData structureData = new StructureData(level.dimension(), start, this::getComponentDataFromPiece);
        MKStructureEntry structureEntry = new MKStructureEntry(structId, instanceId, start, structureData);
        indexStructureEntry(structureEntry);
        return structureEntry;
    }

    @Override
    public WorldStructureManager getStructureManager() {
        return structureManager;
    }

    @Override
    public Optional<MKStructureEntry> getStructureInstance(UUID structId) {
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
    public Level getLevel() {
        return level;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag spawnConfig = new CompoundTag();
        for (UUID entityId : worldPermanentSpawnConfigurations.keySet()) {
            WorldPermanentSpawnConfiguration config = worldPermanentSpawnConfigurations.get(entityId);
            spawnConfig.put(entityId.toString(), config.serializeNBT());
        }
        tag.put("spawnConfigs", spawnConfig);
        ListTag structuresNbt = new ListTag();
        for (MKStructureEntry structure : structureIndex.values()) {
            structuresNbt.add(structure.serializeNBT());
        }
        tag.put("structures", structuresNbt);
        ListTag questNbt = new ListTag();
        for (QuestChainInstance inst : quests.values()) {
            questNbt.add(inst.serializeNBT());
        }
        tag.put("quests", questNbt);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag spawnConfigNbt = nbt.getCompound("spawnConfigs");
        for (String idKey : spawnConfigNbt.getAllKeys()) {
            UUID entityId = UUID.fromString(idKey);
            WorldPermanentSpawnConfiguration config = new WorldPermanentSpawnConfiguration();
            config.deserializeNBT(spawnConfigNbt.getCompound(idKey));
            worldPermanentSpawnConfigurations.put(entityId, config);
        }
        ListTag structuresNbt = nbt.getList("structures", Tag.TAG_COMPOUND);
        for (Tag structureNbt : structuresNbt) {
            MKStructureEntry newStructure = new MKStructureEntry();
            newStructure.deserializeNBT((CompoundTag) structureNbt);
            indexStructureEntry(newStructure);
        }
        ListTag questsNbt = nbt.getList("quests", Tag.TAG_COMPOUND);
        for (Tag questNbt : questsNbt) {
            QuestChainInstance inst = new QuestChainInstance((CompoundTag) questNbt);
            quests.put(inst.getQuestId(), inst);
        }
    }
}
