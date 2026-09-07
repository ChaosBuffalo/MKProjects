package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertAttachmentFace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertSocketPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MKWorkspaceInsertOverlayService {
    private static final int DEFAULT_RADIUS_BLOCKS = 96;
    private static final int DEFAULT_MAX_ENTRIES = 128;

    private MKWorkspaceInsertOverlayService() {
    }

    public static MKWorkspaceInsertOverlaySnapshot capture(ServerLevel level, BlockPos viewerPos) {
        return capture(level, viewerPos, DEFAULT_RADIUS_BLOCKS, DEFAULT_MAX_ENTRIES);
    }

    public static MKWorkspaceInsertOverlaySnapshot capture(ServerLevel level, BlockPos viewerPos,
                                                          int radiusBlocks, int maxEntries) {
        ArrayList<MKWorkspaceInsertOverlaySnapshot.Entry> entries = new ArrayList<>();
        int radiusSqr = radiusBlocks * radiusBlocks;
        for (MKStructureWorkspace workspace : IMKStructureWorkspaceData.get(level).getAllWorkspaces()) {
            Map<ResourceLocation, MKWorkspaceInsertFamilyDefinition> familiesByPool = insertSocketFamiliesByPool(
                    workspace);
            if (familiesByPool.isEmpty()) {
                continue;
            }
            for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
                if (MKWorkspaceTemplateReuseTags.isDerived(piece.tags())) {
                    continue;
                }
                if (piece.structureBlockPos().distSqr(viewerPos) > radiusSqr) {
                    continue;
                }
                if (!shouldShowOverlayForPiece(level, piece)) {
                    continue;
                }
                capturePiece(level, piece, familiesByPool, entries, maxEntries);
                if (entries.size() >= maxEntries) {
                    return new MKWorkspaceInsertOverlaySnapshot(entries);
                }
            }
        }
        return new MKWorkspaceInsertOverlaySnapshot(entries);
    }

    private static Map<ResourceLocation, MKWorkspaceInsertFamilyDefinition> insertSocketFamiliesByPool(
            MKStructureWorkspace workspace) {
        LinkedHashMap<ResourceLocation, MKWorkspaceInsertFamilyDefinition> familiesByPool = new LinkedHashMap<>();
        for (MKWorkspaceInsertFamilyDefinition family : workspace.insertFamilies()) {
            if (family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET) {
                familiesByPool.put(MKInsertFamilyPools.poolId(workspace.namespace(), workspace.structureName(),
                        family.familyId()), family);
            }
        }
        return familiesByPool;
    }

    private static boolean shouldShowOverlayForPiece(ServerLevel level, MKWorkspacePieceDefinition piece) {
        BlockEntity structureEntity = level.getBlockEntity(piece.structureBlockPos());
        return structureEntity instanceof StructureBlockEntity structureBlock && structureBlock.getShowBoundingBox();
    }

    private static void capturePiece(ServerLevel level, MKWorkspacePieceDefinition piece,
                                     Map<ResourceLocation, MKWorkspaceInsertFamilyDefinition> familiesByPool,
                                     ArrayList<MKWorkspaceInsertOverlaySnapshot.Entry> entries, int maxEntries) {
        for (BlockPos pos : BlockPos.betweenClosed(piece.exportBounds().minX(), piece.exportBounds().minY(),
                piece.exportBounds().minZ(), piece.exportBounds().maxX(), piece.exportBounds().maxY(),
                piece.exportBounds().maxZ())) {
            if (entries.size() >= maxEntries) {
                return;
            }
            BlockEntity entity = level.getBlockEntity(pos);
            if (!(entity instanceof JigsawBlockEntity jigsaw)) {
                continue;
            }
            MKWorkspaceInsertFamilyDefinition family = familiesByPool.get(jigsaw.getPool().location());
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
            BlockPos socketLocalPos = pos.subtract(piece.worldOrigin());
            BoundingBox localBounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(socketLocalPos,
                    family.width(), family.height(), family.depth(), authoredAttachmentFace, faceUOffset,
                    faceVOffset, placementAttachmentFace, orientation.top());
            BoundingBox worldBounds = localBounds.moved(piece.worldOrigin().getX(), piece.worldOrigin().getY(),
                    piece.worldOrigin().getZ());
            entries.add(new MKWorkspaceInsertOverlaySnapshot.Entry(family.familyId(), pos.immutable(), worldBounds,
                    orientation.front(), orientation.top()));
        }
    }

    private static int centeredUOffset(int width, int depth, MKWorkspaceInsertAttachmentFace attachmentFace) {
        if (attachmentFace == MKWorkspaceInsertAttachmentFace.WEST ||
                attachmentFace == MKWorkspaceInsertAttachmentFace.EAST) {
            return depth / 2;
        }
        return width / 2;
    }

    private static int centeredVOffset(int height, int depth, MKWorkspaceInsertAttachmentFace attachmentFace) {
        if (attachmentFace.isHorizontal()) {
            return 0;
        }
        return depth / 2;
    }
}
