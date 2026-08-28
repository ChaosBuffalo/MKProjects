package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
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
import net.minecraft.world.level.block.Rotation;

import java.util.List;

public final class MKWorkspaceInsertTemplateCompatibility {
    private MKWorkspaceInsertTemplateCompatibility() {
    }

    public static List<String> compatibleFamilyIds(ServerLevel level, MKStructureWorkspace workspace,
                                                   Direction socketFacing) {
        return workspace.insertFamilies().stream()
                .filter(family -> family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET)
                .filter(family -> hasAttachableTemplateJigsaw(level, workspace, family, socketFacing))
                .map(MKWorkspaceInsertFamilyDefinition::familyId)
                .toList();
    }

    public static boolean hasAttachableTemplateJigsaw(ServerLevel level, MKStructureWorkspace workspace,
                                                      MKWorkspaceInsertFamilyDefinition family,
                                                      Direction socketFacing) {
        if (family.kind() != MKWorkspaceInsertFamilyKind.INSERT_SOCKET) {
            return false;
        }
        ResourceLocation insertPool = MKInsertFamilyPools.poolId(workspace.namespace(), workspace.structureName(),
                family.familyId());
        ResourceLocation expectedChildName = insertAttach(insertPool);
        Direction expectedChildFront = socketFacing.getOpposite();
        Direction expectedChildTop = jigsawOrientation(socketFacing).top();
        for (MKWorkspacePieceDefinition piece : insertTemplatePieces(workspace, family)) {
            for (BlockPos pos : BlockPos.betweenClosed(piece.exportBounds().minX(), piece.exportBounds().minY(),
                    piece.exportBounds().minZ(), piece.exportBounds().maxX(), piece.exportBounds().maxY(),
                    piece.exportBounds().maxZ())) {
                BlockEntity entity = level.getBlockEntity(pos);
                if (!(entity instanceof JigsawBlockEntity jigsaw)) {
                    continue;
                }
                if (!expectedChildName.equals(jigsaw.getName())) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.hasProperty(JigsawBlock.ORIENTATION)) {
                    continue;
                }
                if (canAttachAfterRotation(state, expectedChildFront, expectedChildTop)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canAttachAfterRotation(BlockState state, Direction expectedChildFront,
                                                  Direction expectedChildTop) {
        for (Rotation rotation : Rotation.values()) {
            FrontAndTop childOrientation = state.rotate(rotation).getValue(JigsawBlock.ORIENTATION);
            if (childOrientation.front() == expectedChildFront &&
                    childOrientation.top() == expectedChildTop) {
                return true;
            }
        }
        return false;
    }

    public static FrontAndTop jigsawOrientation(Direction facing) {
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return FrontAndTop.fromFrontAndTop(facing, Direction.NORTH);
        }
        return FrontAndTop.fromFrontAndTop(facing, Direction.UP);
    }

    private static List<MKWorkspacePieceDefinition> insertTemplatePieces(MKStructureWorkspace workspace,
                                                                         MKWorkspaceInsertFamilyDefinition family) {
        return workspace.pieces().stream()
                .filter(piece -> family.familyId().equals(piece.tags().get(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID)))
                .filter(piece -> insertFamilyKindMatches(family.kind(), piece.tags()
                        .get(MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND)))
                .toList();
    }

    private static boolean insertFamilyKindMatches(MKWorkspaceInsertFamilyKind familyKind, String pieceKindName) {
        if (pieceKindName == null || pieceKindName.isBlank()) {
            return true;
        }
        for (MKWorkspaceInsertFamilyKind pieceKind : MKWorkspaceInsertFamilyKind.values()) {
            if (pieceKind.getSerializedName().equals(pieceKindName)) {
                return pieceKind.canonical() == familyKind.canonical();
            }
        }
        return familyKind.getSerializedName().equals(pieceKindName);
    }

    private static ResourceLocation insertAttach(ResourceLocation insertPool) {
        return ResourceLocation.fromNamespaceAndPath(insertPool.getNamespace(), "attach");
    }
}
