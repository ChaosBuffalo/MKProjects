package com.chaosbuffalo.mknpc.world.gen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.UUID;

public interface StructureStartExtension {

    UUID getInstanceId();

    void mknpc_loadAdditional(CompoundTag tag);

    static StructureStartExtension of(StructureStart start) {
        return (StructureStartExtension) (Object) start;
    }

    static UUID getInstanceId(StructureStart start) {
        return of(start).getInstanceId();
    }
}
