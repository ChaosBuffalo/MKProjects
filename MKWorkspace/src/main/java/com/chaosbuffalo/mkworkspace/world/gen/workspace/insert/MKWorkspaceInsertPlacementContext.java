package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public record MKWorkspaceInsertPlacementContext(
        MKStructureWorkspace workspace,
        MKWorkspacePieceDefinition piece,
        BlockPos socketWorldPos,
        BlockPos socketLocalPos,
        Direction socketFacing,
        String finalState,
        List<String> compatibleInsertFamilyIds,
        List<MKWorkspaceInsertFootprintScanner.OccupiedInsertFootprint> occupiedFootprints
) {
}
