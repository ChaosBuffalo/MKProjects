package com.chaosbuffalo.mknpc.block_entities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkcore.utils.WorldUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.blocks.MKSpawnerBlock;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.components.NpcComponents;
import com.chaosbuffalo.mknpc.components.SpawnerDataComponent;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.init.MKNpcBlockEntityTypes;
import com.chaosbuffalo.mknpc.npc.INotifyOnEntityDeath;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.spawn.SpawnList;
import com.chaosbuffalo.mknpc.spawn.SpawnOption;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MKSpawnerBlockEntity extends BlockEntity implements IStructurePlaced, INotifyOnEntityDeath {
    private final SpawnList spawnList;
    private UUID spawnUUID;
    private Entity entity;
    private int respawnTime;
    private int ticksSinceDeath;
    private int ticksSincePlayer;
    private final static double SPAWN_RANGE = 100.0;
    private final static double DESPAWN_RANGE = 150.0;
    private static final int IDLE_TIME = GameConstants.TICKS_PER_SECOND * 10;
    private final RandomCollection<NpcDefinition> randomSpawns;
    private MKEntity.NonCombatMoveType moveType;
    private ResourceLocation structureName;
    private UUID structureId;
    private boolean needsUploadToWorld;
    private boolean placedByStructure;
    private boolean needsPopulate = false;
    private final Map<ResourceLocation, UUID> notableIds = new HashMap<>();


    public MKSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MKNpcBlockEntityTypes.MK_SPAWNER_BLOCK_ENTITY_TYPE.get(), blockPos, blockState);
        this.spawnList = new SpawnList();
        this.spawnUUID = UUID.randomUUID();
        this.structureName = null;
        this.structureId = null;
        this.needsUploadToWorld = false;
        this.respawnTime = GameConstants.TICKS_PER_SECOND * 300;
        this.ticksSinceDeath = 0;
        this.ticksSincePlayer = 0;
        this.entity = null;
        this.moveType = MKEntity.NonCombatMoveType.STATIONARY;
        this.randomSpawns = new RandomCollection<>();
        this.placedByStructure = false;
    }

    @Override
    public boolean isInsideStructure() {
        return structureName != null && structureId != null;
    }

    @Override
    public void setStructureName(ResourceLocation structureName) {
        this.structureName = structureName;
    }

    @Override
    @Nullable
    public ResourceLocation getStructureName() {
        return structureName;
    }

    @Override
    @Nullable
    public UUID getStructureId() {
        return structureId;
    }

    public void putNotableId(ResourceLocation loc, UUID notableId) {
        this.notableIds.put(loc, notableId);
    }

    @Override
    public void setStructureId(UUID structureId) {
        this.structureId = structureId;
    }

    @Override
    public GlobalPos getGlobalPos() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }

    @Override
    @Nullable
    public Level getStructureLevel() {
        return getLevel();
    }

    public void setMoveType(MKEntity.NonCombatMoveType moveType) {
        this.moveType = moveType;
    }

    public MKEntity.NonCombatMoveType getMoveType() {
        return moveType;
    }

    public SpawnList getSpawnList() {
        return spawnList;
    }

    protected void flagNeedsPopulate()
    {
        needsPopulate = true;
    }

    public void setSpawnList(SpawnList list) {
        spawnList.copyList(list);
        flagNeedsPopulate();
        ticksSinceDeath = 0;
    }

    public void populateRandomSpawns() {
        randomSpawns.clear();
        if (getLevel() instanceof ServerLevel serverLevel) {
            for (SpawnOption option : spawnList.getOptions()) {
                randomSpawns.add(option.getWeight(), option.getDefinition(serverLevel.registryAccess()));
            }
            needsPopulate = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.put("spawnList", spawnList.serializeNBT(registries));
        compound.putUUID("spawnId", spawnUUID);
        compound.putInt("ticksSinceDeath", ticksSinceDeath);
        compound.putInt("moveType", moveType.ordinal());
        compound.putBoolean("hasUploadedToWorld", needsUploadToWorld);
        compound.putBoolean("placedByStructure", placedByStructure);
        compound.putInt("respawnTime", respawnTime);
        if (isInsideStructure()) {
            compound.putString("structureName", structureName.toString());
            compound.putUUID("structureId", structureId);
        }
        CompoundTag notableTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, UUID> entry : notableIds.entrySet()) {
            notableTag.putUUID(entry.getKey().toString(), entry.getValue());
        }
        compound.put("notableIds", notableTag);
    }


    public UUID getSpawnUUID() {
        return spawnUUID;
    }

    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public boolean isSpawnAlive() {
        return entity != null && entity.isAlive();
    }

    private boolean isSpawnDead() {
        return entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() <= 0 && !entity.isAlive();
    }

    public static double getDespawnRange() {
        return DESPAWN_RANGE;
    }

    public static double getSpawnRange() {
        return SPAWN_RANGE;
    }

    public void regenerateSpawnID() {
        if (!placedByStructure) {
            this.spawnUUID = UUID.randomUUID();
            this.needsUploadToWorld = true;
            this.placedByStructure = true;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        if (compound.contains("spawnList")) {
            spawnList.deserializeNBT(registries, compound.getCompound("spawnList"));
            flagNeedsPopulate();
        }
        if (compound.contains("moveType")) {
            setMoveType(MKEntity.NonCombatMoveType.values()[compound.getInt("moveType")]);
        }
        if (compound.contains("structureName")) {
            setStructureName(ResourceLocation.parse(compound.getString("structureName")));
        }
        if (compound.contains("structureId")) {
            setStructureId(compound.getUUID("structureId"));
        }
        if (compound.contains("hasUploadedToWorld")) {
            needsUploadToWorld = compound.getBoolean("hasUploadedToWorld");
        }
        if (compound.contains("placedByStructure")) {
            placedByStructure = compound.getBoolean("placedByStructure");
        }
        ticksSinceDeath = compound.getInt("ticksSinceDeath");
        if (compound.contains("spawnId")) {
            spawnUUID = compound.getUUID("spawnId");
        } else {
            spawnUUID = UUID.randomUUID();
        }
        if (compound.contains("respawnTime")) {
            setRespawnTime(compound.getInt("respawnTime"));
        }
        if (compound.contains("notableIds")) {
            CompoundTag notableTag = compound.getCompound("notableIds");
            for (String key : notableTag.getAllKeys()) {
                UUID notId = notableTag.getUUID(key);
                notableIds.put(ResourceLocation.parse(key), notId);
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        SpawnerDataComponent comp = componentInput.get(NpcComponents.SPAWNER_DATA);
        if (comp != null) {
            setSpawnList(comp.spawns());
            setRespawnTime(comp.spawnTime());
            setMoveType(comp.moveType());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(NpcComponents.SPAWNER_DATA, new SpawnerDataComponent(getSpawnList(), getRespawnTime(), getMoveType()));
    }

    public void spawnEntity() {
        if (getLevel() != null) {
            NpcDefinition definition = randomSpawns.next(getLevel().getRandom());
            Vec3 spawnPos = Vec3.atLowerCornerOf(getBlockPos()).add(0.5, 0.125, 0.5);
            double difficultyValue = WorldUtils.getDifficultyForGlobalPos(getLevel(),
                    GlobalPos.of(getLevel().dimension(), getBlockPos()));
            switch (getLevel().getDifficulty()) {
                case EASY:
                    difficultyValue *= .5;
                    break;
                case NORMAL:
                    difficultyValue *= .75;
                    break;
                case PEACEFUL:
                    difficultyValue = 0.0;
                    break;
                default:
                    break;
            }
            Entity entity = definition.createEntity(getLevel(), spawnPos, spawnUUID, difficultyValue);
            this.entity = entity;
            if (entity != null) {
                float rot = getBlockState().getValue(MKSpawnerBlock.ORIENTATION).getAngleInDegrees();
                entity.absMoveTo(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        rot,
                        0.0f);
                entity.setYHeadRot(rot);
                final double finDiff = difficultyValue;
                MKNpc.getNpcData(entity).ifPresent((cap) -> {
                    cap.setSpawnPos(BlockPos.containing(spawnPos).above());
                    if (notableIds.containsKey(definition.getDefinitionName())) {
                        cap.setNotableUUID(notableIds.get(definition.getDefinitionName()));
                    }
                    cap.setStructureId(getStructureId());
                    cap.setDifficultyValue(finDiff);
                    cap.setDeathReceiver(this);
                });
                if (entity instanceof MKEntity mkEntity) {
                    mkEntity.setNonCombatMoveType(getMoveType());
                }
                if (entity instanceof Mob mobEnt && getLevel() instanceof ServerLevel serverLevel) {
                    EventHooks.finalizeMobSpawn(mobEnt, serverLevel,
                            serverLevel.getCurrentDifficultyAt(getBlockPos()),
                            MobSpawnType.SPAWNER, null);
                }
                getLevel().addFreshEntity(entity);
                MKNpc.getNpcData(entity).ifPresent(cap -> cap.setMKSpawned(true));
            }
        }
    }

    private boolean isPlayerInRange() {
        if (getLevel() == null) {
            return false;
        }
        Vec3 loc = Vec3.atLowerCornerOf(getBlockPos());
        for (Player player : getLevel().players()) {
            if (player.distanceToSqr(loc) < getSpawnRange() * getSpawnRange()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerInDespawnRange() {
        if (getLevel() == null) {
            return false;
        }
        Vec3 loc = Vec3.atLowerCornerOf(getBlockPos());
        for (Player player : getLevel().players()) {
            if (player.distanceToSqr(loc) < getDespawnRange() * getDespawnRange()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnRespawnTimer() {
        return ticksSinceDeath > 0;
    }

    public void clearSpawn() {
        if (entity != null) {
            EntityUtils.mkDiscard(entity);
            entity = null;
        }
        ticksSinceDeath = 0;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        clearSpawn();
    }

    private boolean isAir(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isAir();
    }


    public static void spawnerTick(Level world, BlockPos blockPos, BlockState blockState, MKSpawnerBlockEntity blockEntity) {
        blockEntity.tick(world);
    }


    public void tick(Level level) {
        if (needsPopulate) {
            populateRandomSpawns();
        }
        if (level != null && randomSpawns.size() > 0) {
            if (needsUploadToWorld) {
                MinecraftServer server = level.getServer();
                if (server != null) {
                    ContentDB.getPrimaryData().addSpawner(this);
                    if (!isAir(level, getBlockPos().above())) {
                        level.setBlock(getBlockPos().above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    needsUploadToWorld = false;
                }
            }
            if (!isAir(level, getBlockPos().above())) {
                if (placedByStructure) {
                    level.setBlock(getBlockPos().above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    return;
                }
            }
            boolean isAlive = isSpawnAlive();
            if (ticksSinceDeath > 0) {
                ticksSinceDeath--;
            }

            if (isPlayerInDespawnRange()) {
                if (!isAlive) {
                    if (ticksSinceDeath <= 0 && isPlayerInRange()) {
                        spawnEntity();
                    }
                }
                ticksSincePlayer = 0;
            } else {
                if (isAlive) {
                    ticksSincePlayer++;
                    if (ticksSincePlayer > IDLE_TIME) {
                        EntityUtils.mkDiscard(entity);
                        this.entity = null;
                        ticksSinceDeath = 0;
                        ticksSincePlayer = 0;
                    }
                }
            }

        }
    }

    @Override
    public void onEntityDeath(IEntityNpcData npcData, LivingDeathEvent event) {
        ticksSinceDeath = getRespawnTime();
    }
}
