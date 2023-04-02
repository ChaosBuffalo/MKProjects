package com.chaosbuffalo.mknpc.world.gen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IStructurePlaced {

    boolean isInsideStructure();

    @Nullable
    UUID getStructureId();

    @Nullable
    ResourceLocation getStructureName();

    void setStructureName(ResourceLocation structureName);

    void setStructureId(UUID structureId);

    GlobalPos getGlobalBlockPos();

    @Nullable
    Level getStructureWorld();
}
