package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import net.minecraft.core.BlockPos;

import java.util.List;

public record MKWorkspaceTemplateBlockDiffReport(
        int comparedBlockCount,
        int changedBlockCount,
        List<BlockPos> changedPositions
) {
    public MKWorkspaceTemplateBlockDiffReport {
        changedPositions = List.copyOf(changedPositions);
    }

    public boolean hasAuthoredChanges() {
        return changedBlockCount > 0;
    }
}
