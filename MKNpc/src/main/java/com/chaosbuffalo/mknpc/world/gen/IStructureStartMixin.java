package com.chaosbuffalo.mknpc.world.gen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.UUID;

public interface IStructureStartMixin {

    UUID getInstanceId();

    void loadAdditional(CompoundTag tag);

    static UUID getInstanceIdFromStart(StructureStart start) {
        return ((IStructureStartMixin) (Object) start).getInstanceId();
    }
}
