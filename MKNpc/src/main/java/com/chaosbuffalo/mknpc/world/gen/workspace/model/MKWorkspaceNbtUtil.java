package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class MKWorkspaceNbtUtil {
    private MKWorkspaceNbtUtil() {
    }

    public static CompoundTag blockPosToTag(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public static BlockPos blockPosFromTag(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public static CompoundTag boundingBoxToTag(BoundingBox box) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("minX", box.minX());
        tag.putInt("minY", box.minY());
        tag.putInt("minZ", box.minZ());
        tag.putInt("maxX", box.maxX());
        tag.putInt("maxY", box.maxY());
        tag.putInt("maxZ", box.maxZ());
        return tag;
    }

    public static BoundingBox boundingBoxFromTag(CompoundTag tag) {
        return new BoundingBox(
                tag.getInt("minX"),
                tag.getInt("minY"),
                tag.getInt("minZ"),
                tag.getInt("maxX"),
                tag.getInt("maxY"),
                tag.getInt("maxZ")
        );
    }
}
