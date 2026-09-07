package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceSimpleChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceSimpleChangePayload;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceContentSelectionChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceContentSelectionChangePayload;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class MKWorkspaceChangeRequests {
    private MKWorkspaceChangeRequests() {
    }

    public static MKWorkspaceChangeRequest simple(MKWorkspaceSimpleChangeOperation.Kind kind, BlockPos anchor) {
        return simple(kind, anchor, MKWorkspaceSimpleChangePayload.empty());
    }

    public static MKWorkspaceChangeRequest target(MKWorkspaceSimpleChangeOperation.Kind kind, BlockPos anchor,
                                                  String target) {
        return simple(kind, anchor, new MKWorkspaceSimpleChangePayload(target, "", "", 0));
    }

    public static MKWorkspaceChangeRequest swapBlocks(BlockPos anchor, ResourceLocation source,
                                                       ResourceLocation target) {
        return simple(MKWorkspaceSimpleChangeOperation.Kind.SWAP_BLOCKS, anchor,
                new MKWorkspaceSimpleChangePayload(source.toString(), target.toString(), "", 0));
    }

    public static MKWorkspaceChangeRequest stairs(BlockPos anchor, String pieceName, MKWorkspaceStairMode mode,
                                                   MKWorkspaceStairRiseType riseType, int width) {
        return simple(MKWorkspaceSimpleChangeOperation.Kind.GENERATE_STAIRS, anchor,
                new MKWorkspaceSimpleChangePayload(pieceName, mode.getSerializedName(),
                        riseType.getSerializedName(), width));
    }

    public static MKWorkspaceChangeRequest simple(MKWorkspaceSimpleChangeOperation.Kind kind, BlockPos anchor,
                                                  MKWorkspaceSimpleChangePayload payload) {
        return new MKWorkspaceChangeRequest(UUID.randomUUID(), kind.id(), anchor,
                MKWorkspaceChangePayloads.encode(MKWorkspaceSimpleChangePayload.CODEC, payload,
                        kind.name().toLowerCase(java.util.Locale.ROOT) + " workspace change"));
    }

    public static MKWorkspaceChangeRequest contentSelection(BlockPos anchor,
                                                             MKWorkspaceContentSelectionChangePayload payload) {
        return new MKWorkspaceChangeRequest(UUID.randomUUID(), MKWorkspaceContentSelectionChangeOperation.ID, anchor,
                MKWorkspaceChangePayloads.encode(MKWorkspaceContentSelectionChangePayload.CODEC, payload,
                        "workspace content selection change"));
    }
}
