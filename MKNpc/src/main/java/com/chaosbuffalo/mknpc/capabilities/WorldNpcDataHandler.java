package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureComponentData;
import com.chaosbuffalo.mknpc.capabilities.structure_tracking.StructureData;
import com.chaosbuffalo.mknpc.event.WorldStructureHandler;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.world.gen.IStructureStartMixin;
import com.chaosbuffalo.mknpc.npc.*;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKSingleJigsawPiece;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class WorldNpcDataHandler implements IWorldNpcData{

    private final HashMap<UUID, WorldPermanentSpawnConfiguration> worldPermanentSpawnConfigurations;
    private final HashMap<UUID, MKStructureEntry> structureIndex;
    private final HashMap<ResourceLocation, List<UUID>> structureToInstanceIndex;
    private final HashMap<UUID, QuestChainInstance> quests;
    private final HashMap<UUID, NotableChestEntry> notableChests;
    private final HashMap<UUID, NotableNpcEntry> notableNpcs;
    private final HashMap<UUID, PointOfInterestEntry> pointOfInterests;
    private final WorldStructureManager structureManager;
    private final List<GlobalPos> chestsToProcess;
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
        chestsToProcess = new ArrayList<>();
    }



    @Override
    public QuestChainInstance getQuest(UUID questId){
        return quests.get(questId);
    }

    @Override
    public boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, Entity entity) {
        UUID spawnId = getSpawnIdForEntity(entity);
        return hasEntityOptionEntry(definition, attribute, spawnId);
    }

    public void putNotableChest(NotableChestEntry notableChestEntry){
        notableChests.put(notableChestEntry.getChestId(), notableChestEntry);
    }

    @Override
    public NotableChestEntry getNotableChest(UUID id){
        return notableChests.get(id);
    }

    @Override
    public NotableNpcEntry getNotableNpc(UUID id){
        return notableNpcs.get(id);
    }

    @Override
    public void setupStructureDataIfAbsent(StructureStart start, Level world) {
        StructureFeature<?> struct = start.getFeature().feature;
        StructureData structureData = new StructureData(world.dimension(),
                start.getChunkPos().x, start.getChunkPos().z, start.getBoundingBox(), start.getPieces().stream().map(
                this::getComponentDataFromPiece).collect(Collectors.toList()));

        MKStructureEntry structureEntry = new MKStructureEntry(this, ForgeRegistries.STRUCTURE_FEATURES.getKey(struct),
                IStructureStartMixin.getInstanceIdFromStart(start), structureData);
        indexStructureEntry(structureEntry);
    }


    @Override
    public PointOfInterestEntry getPointOfInterest(UUID id) {
        return pointOfInterests.get(id);
    }

    public void putNotableNpc(NotableNpcEntry notableNpcEntry){
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

    public static UUID getSpawnIdForEntity(Entity entity){
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
        if (!worldPermanentSpawnConfigurations.containsKey(spawnId)){
            worldPermanentSpawnConfigurations.put(spawnId, new WorldPermanentSpawnConfiguration());
        }
        worldPermanentSpawnConfigurations.get(spawnId).addAttributeEntry(definition, attribute, entry);
    }

    @Override
    public Optional<QuestChainInstance.QuestChainBuildResult> buildQuest(QuestDefinition definition, BlockPos pos){
        Map<ResourceLocation, Integer> structuresNeeded = definition.getStructuresNeeded();
        if (hasStructureInstances(structuresNeeded.keySet())){
            Map<ResourceLocation, List<MKStructureEntry>> possibilities = structuresNeeded.keySet().stream()
                    .map(x -> new Pair<>(x, structureToInstanceIndex.get(x)))
                    .map(x -> x.mapSecond(ids -> ids.stream().map(structureIndex::get)
                            .filter(definition::doesStructureMeetRequirements)))
                    .collect(Collectors.toMap(Pair::getFirst, pair -> pair.getSecond().collect(Collectors.toList())));
            if (possibilities.entrySet().stream().allMatch(x -> x.getValue().size() >= structuresNeeded.get(x.getKey()))) {
                Map<ResourceLocation, List<MKStructureEntry>> questStructures = new HashMap<>();
                for (Map.Entry<ResourceLocation, Integer> needed : structuresNeeded.entrySet()){
                    int toFind = needed.getValue();
                    List<MKStructureEntry> byDistance = possibilities.get(needed.getKey()).stream().sorted(Comparator.comparingInt(
                            x -> new ChunkPos(pos).getChessboardDistance(x.getChunkPos())))
                            .collect(Collectors.toList());
                    List<MKStructureEntry> finals = new ArrayList<>();
                    for (int i = 0; i < toFind; i++){
                        finals.add(byDistance.get(i));
                    }
                    questStructures.put(needed.getKey(), finals);
                }

                QuestChainInstance instance = definition.generate(questStructures);
                instance.generateDialogue(questStructures);
                MKNpc.LOGGER.debug("Built quest {} for {}", instance.getQuestId(), definition.getName());
                quests.put(instance.getQuestId(), instance);
                return Optional.of(new QuestChainInstance.QuestChainBuildResult(instance, questStructures));
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public boolean hasStructureInstances(Set<ResourceLocation> structureNames){
        return structureNames.stream().allMatch(this::isStructureIndexed);
    }

    private MKStructureEntry computeStructureEntry(IStructurePlaced structurePlaced){
        StructureData structureData = null;
        Level structureWorld = structurePlaced.getStructureWorld();
        if (structureWorld instanceof ServerLevel){
            ServerLevel world = (ServerLevel) structureWorld;
            ConfiguredStructureFeature<?, MKJigsawStructure> struct = WorldStructureHandler.MK_STRUCTURE_INDEX.get(structurePlaced.getStructureName());
            if (struct != null){
                StructureStart start = world.structureFeatureManager().getStructureAt(structurePlaced.getGlobalBlockPos().pos(), struct);
                structureData = new StructureData(structurePlaced.getStructureWorld().dimension(),
                        start.getChunkPos().x, start.getChunkPos().z, start.getBoundingBox(), start.getPieces().stream().map(
                        this::getComponentDataFromPiece).collect(Collectors.toList()));
            }

        }
        MKStructureEntry structureEntry = new MKStructureEntry(this, structurePlaced.getStructureName(), structurePlaced.getStructureId(), structureData);
        indexStructureEntry(structureEntry);
        return structureEntry;
    }

    private void indexStructureEntry(MKStructureEntry structureEntry){
        structureToInstanceIndex.computeIfAbsent(structureEntry.getStructureName(), key -> new ArrayList<>())
                .add(structureEntry.getStructureId());
    }

    private StructureComponentData getComponentDataFromPiece(StructurePiece piece){
        ResourceLocation pieceName = MKNpcWorldGen.UNKNOWN_PIECE;
        if (piece instanceof PoolElementStructurePiece){
            if (((PoolElementStructurePiece) piece).getElement() instanceof MKSingleJigsawPiece){
                MKSingleJigsawPiece mkPiece = ((MKSingleJigsawPiece) ((PoolElementStructurePiece) piece).getElement());
                pieceName = mkPiece.getPieceEither().left().orElse(MKNpcWorldGen.UNKNOWN_PIECE);
            }
        }
        return new StructureComponentData(pieceName, piece.getBoundingBox());
    }

    @Override
    public void addSpawner(MKSpawnerTileEntity spawner) {
        MKStructureEntry structure = structureIndex.computeIfAbsent(spawner.getStructureId(),
                key -> computeStructureEntry(spawner));
        structure.addSpawner(spawner);
    }

    @Override
    public void addChest(IChestNpcData chestData){
        MKStructureEntry structure = structureIndex.computeIfAbsent(chestData.getStructureId(),
                key -> computeStructureEntry(chestData));
        structure.addChest(chestData);

    }

    @Override
    public void addPointOfInterest(MKPoiTileEntity entity) {
        MKStructureEntry structure = structureIndex.computeIfAbsent(entity.getStructureId(),
                key -> computeStructureEntry(entity));
        structure.addPOI(entity);
    }

    @Override
    public void update() {
        structureManager.tick();
        chestsToProcess.forEach(this::processChest);
        chestsToProcess.clear();
    }

    @Override
    public WorldStructureManager getStructureManager() {
        return structureManager;
    }

    @Override
    public MKStructureEntry getStructureData(UUID structId) {
        return structureIndex.get(structId);
    }

    protected boolean hasStructureInstance(UUID structureId){
        return structureIndex.containsKey(structureId);
    }

    protected boolean isStructureIndexed(ResourceLocation structureName){
        return structureToInstanceIndex.containsKey(structureName) && structureToInstanceIndex.get(structureName).size() > 0;
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
                BlockEntity entity = chestLevel.getBlockEntity(pos.pos());
                if (entity != null) {
                    entity.getCapability(NpcCapabilities.CHEST_NPC_DATA_CAPABILITY).ifPresent(IChestNpcData::onLoad);
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag spawnConfig = new CompoundTag();
        for (UUID entityId : worldPermanentSpawnConfigurations.keySet()){
            WorldPermanentSpawnConfiguration config = worldPermanentSpawnConfigurations.get(entityId);
            spawnConfig.put(entityId.toString(), config.serializeNBT());
        }
        tag.put("spawnConfigs", spawnConfig);
        ListTag structuresNbt = new ListTag();
        for (MKStructureEntry structure : structureIndex.values()){
            structuresNbt.add(structure.serializeNBT());
        }
        tag.put("structures", structuresNbt);
        ListTag questNbt = new ListTag();
        for (QuestChainInstance inst : quests.values()){
            questNbt.add(inst.serializeNBT());
        }
        tag.put("quests", questNbt);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag spawnConfigNbt = nbt.getCompound("spawnConfigs");
        for (String idKey : spawnConfigNbt.getAllKeys()){
            UUID entityId = UUID.fromString(idKey);
            WorldPermanentSpawnConfiguration config = new WorldPermanentSpawnConfiguration();
            config.deserializeNBT(spawnConfigNbt.getCompound(idKey));
            worldPermanentSpawnConfigurations.put(entityId, config);
        }
        ListTag structuresNbt = nbt.getList("structures", Tag.TAG_COMPOUND);
        for (Tag structureNbt : structuresNbt){
            MKStructureEntry newStructure = new MKStructureEntry(this);
            newStructure.deserializeNBT((CompoundTag) structureNbt);
            structureIndex.put(newStructure.getStructureId(), newStructure);
            indexStructureEntry(newStructure);
        }
        ListTag questsNbt = nbt.getList("quests", Tag.TAG_COMPOUND);
        for (Tag questNbt : questsNbt){
            QuestChainInstance inst = new QuestChainInstance((CompoundTag) questNbt);
            quests.put(inst.getQuestId(), inst);
        }
    }
}
