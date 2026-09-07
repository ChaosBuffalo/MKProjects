package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertAttachmentFace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertSocketPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MKWorkspaceInsertFootprintScanner {
    public record OccupiedInsertFootprint(String familyId, BlockPos socketLocalPos, BoundingBox bounds) {
    }

    public List<OccupiedInsertFootprint> scan(ServerLevel level, MKStructureWorkspace workspace,
                                             MKWorkspacePieceDefinition hostPiece, BlockPos excludedSocketWorldPos) {
        Map<ResourceLocation, MKWorkspaceInsertFamilyDefinition> familiesByPool = new LinkedHashMap<>();
        for (MKWorkspaceInsertFamilyDefinition family : workspace.insertFamilies()) {
            if (family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET) {
                familiesByPool.put(MKInsertFamilyPools.poolId(workspace.namespace(), workspace.structureName(),
                        family.familyId()), family);
            }
        }
        if (familiesByPool.isEmpty()) {
            return List.of();
        }

        ArrayList<OccupiedInsertFootprint> footprints = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(hostPiece.exportBounds().minX(), hostPiece.exportBounds().minY(),
                hostPiece.exportBounds().minZ(), hostPiece.exportBounds().maxX(), hostPiece.exportBounds().maxY(),
                hostPiece.exportBounds().maxZ())) {
            if (excludedSocketWorldPos != null && excludedSocketWorldPos.equals(pos)) {
                continue;
            }
            BlockEntity entity = level.getBlockEntity(pos);
            if (!(entity instanceof JigsawBlockEntity jigsaw)) {
                continue;
            }
            ResourceLocation poolId = jigsaw.getPool().location();
            MKWorkspaceInsertFamilyDefinition family = familiesByPool.get(poolId);
            if (family == null) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            FrontAndTop orientation = state.hasProperty(JigsawBlock.ORIENTATION) ?
                    state.getValue(JigsawBlock.ORIENTATION) : FrontAndTop.fromFrontAndTop(Direction.UP,
                    Direction.NORTH);
            MKWorkspaceInsertAttachmentFace placementAttachmentFace =
                    MKWorkspaceInsertAttachmentFace.fromDirection(orientation.front().getOpposite());
            MKWorkspaceInsertAttachmentFace authoredAttachmentFace = family.attachmentFace()
                    .orElse(placementAttachmentFace);
            int faceUOffset = family.attachmentFace().isPresent() ? family.faceUOffset() :
                    centeredUOffset(family.width(), family.depth(), placementAttachmentFace);
            int faceVOffset = family.attachmentFace().isPresent() ? family.faceVOffset() :
                    centeredVOffset(family.height(), family.depth(), placementAttachmentFace);
            BlockPos socketLocalPos = pos.subtract(hostPiece.worldOrigin());
            BoundingBox bounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(socketLocalPos,
                    family.width(), family.height(), family.depth(), authoredAttachmentFace, faceUOffset,
                    faceVOffset, placementAttachmentFace, orientation.top());
            footprints.add(new OccupiedInsertFootprint(family.familyId(), socketLocalPos, bounds));
        }
        return footprints;
    }

    private int centeredUOffset(int width, int depth, MKWorkspaceInsertAttachmentFace attachmentFace) {
        if (attachmentFace == MKWorkspaceInsertAttachmentFace.WEST ||
                attachmentFace == MKWorkspaceInsertAttachmentFace.EAST) {
            return depth / 2;
        }
        return width / 2;
    }

    private int centeredVOffset(int height, int depth, MKWorkspaceInsertAttachmentFace attachmentFace) {
        if (attachmentFace.isHorizontal()) {
            return 0;
        }
        return depth / 2;
    }
}
