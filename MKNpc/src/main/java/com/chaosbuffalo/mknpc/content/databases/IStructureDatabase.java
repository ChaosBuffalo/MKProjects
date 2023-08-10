package com.chaosbuffalo.mknpc.content.databases;

import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Optional;
import java.util.UUID;

public interface IStructureDatabase {

    WorldStructureManager getStructureManager();

    Optional<MKStructureEntry> getStructureInstance(UUID structId);

    Optional<MKStructureEntry> findContainingStructure(IStructurePlaced structurePlaced);

    MKStructureEntry getStructureInstance(StructureStart start, Level level);
}
