package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableChestEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.WorldPermanentOption;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface IWorldNpcData extends INBTSerializable<CompoundTag> {

    boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, Entity entity);

    boolean hasEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute, UUID spawnId);

    INpcOptionEntry getEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                                         Entity entity);

    INpcOptionEntry getEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                                         UUID entityId);

    void addEntityOptionEntry(NpcDefinition definition, WorldPermanentOption attribute,
                              UUID entityId, INpcOptionEntry entry);

    void addSpawner(MKSpawnerTileEntity spawner);

    void addChest(IChestNpcData chestData);

    void addPointOfInterest(MKPoiTileEntity entry);

    void update();

    WorldStructureManager getStructureManager();

    @Nullable
    MKStructureEntry getStructureData(UUID structId);

    @Nullable
    QuestChainInstance getQuest(UUID questId);

    Optional<QuestChainInstance.QuestChainBuildResult> buildQuest(QuestDefinition definition, BlockPos pos);

    @Nullable
    NotableChestEntry getNotableChest(UUID id);

    @Nullable
    NotableNpcEntry getNotableNpc(UUID id);

    void setupStructureDataIfAbsent(StructureStart start, Level world);

    @Nullable
    PointOfInterestEntry getPointOfInterest(UUID id);

    Level getWorld();

    void queueChestForProcessing(GlobalPos pos);
}
