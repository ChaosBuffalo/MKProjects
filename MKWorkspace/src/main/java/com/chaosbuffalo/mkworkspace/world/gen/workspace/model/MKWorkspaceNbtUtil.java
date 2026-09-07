package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class MKWorkspaceNbtUtil {
    private MKWorkspaceNbtUtil() {
    }

    public static CompoundTag blockPosToTag(BlockPos pos) {
        return MKWorkspaceCodecs.encodeNbt(MKWorkspaceCodecs.BLOCK_POS_CODEC, pos, "workspace block position");
    }

    public static BlockPos blockPosFromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(MKWorkspaceCodecs.BLOCK_POS_CODEC, tag, "workspace block position");
    }

    public static CompoundTag boundingBoxToTag(BoundingBox box) {
        return MKWorkspaceCodecs.encodeNbt(MKWorkspaceCodecs.BOUNDING_BOX_CODEC, box, "workspace bounding box");
    }

    public static BoundingBox boundingBoxFromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(MKWorkspaceCodecs.BOUNDING_BOX_CODEC, tag, "workspace bounding box");
    }
}
