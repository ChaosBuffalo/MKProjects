package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKFloorMaskVariantExporterTest {
    @Test
    void runtimeMaskVariantsUseAuthoredContentVariantsAsSources() {
        MKStructureWorkspace workspace = workspaceWithPieces(List.of(
                floorTemplate("floor_branch_cap_template", "floor_branch_cap"),
                floorVariant("floor_branch_cap_1", "floor_branch_cap", 1)
        ));

        List<MKWorkspacePieceDefinition> exportPieces = MKFloorMaskVariantExporter.exportPieces(workspace, true);

        assertFalse(hasPiece(exportPieces, "floor_branch_cap_template_mask_none"));
        MKWorkspacePieceDefinition runtimeMask = piece(exportPieces, "floor_branch_cap_1_mask_none");
        assertEquals(1, runtimeMask.variantIndex());
        assertEquals("floor_branch_cap", MKWorkspaceTemplateReuseTags.sourceId(runtimeMask.tags()));
        assertEquals(MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT,
                runtimeMask.tags().get(MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG));
    }

    @Test
    void authoringTemplatesAloneDoNotBecomeRuntimeMaskVariants() {
        MKStructureWorkspace workspace = workspaceWithPieces(List.of(
                floorTemplate("floor_branch_cap_template", "floor_branch_cap")
        ));

        List<MKWorkspacePieceDefinition> exportPieces = MKFloorMaskVariantExporter.exportPieces(workspace, true);

        assertTrue(hasPiece(exportPieces, "floor_branch_cap_template"));
        assertFalse(hasPiece(exportPieces, "floor_branch_cap_template_mask_none"));
    }

    private static MKStructureWorkspace workspaceWithPieces(List<MKWorkspacePieceDefinition> pieces) {
        return MKStructureWorkspace.createDraft(BlockPos.ZERO).withPieces(pieces);
    }

    private static MKWorkspacePieceDefinition floorTemplate(String pieceName, String baseName) {
        return piece(pieceName, baseName, 0, "template");
    }

    private static MKWorkspacePieceDefinition floorVariant(String pieceName, String baseName, int variantIndex) {
        return piece(pieceName, baseName, variantIndex, "instance");
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName, int variantIndex,
                                                   String pieceKind) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("tower_piece_kind", "floor_plan_room");
        tags.put("workspace_piece_kind", pieceKind);
        tags.put("workspace_base_name", baseName);
        tags.put("workspace_floor_room_kind", MKFloorRoomKind.BRANCH_CAP.getSerializedName());
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "tower.floor_plan.branch_cap",
                variantIndex,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(connector()),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 4, 4, 4),
                new BoundingBox(0, 0, 0, 4, 4, 4),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private static MKWorkspaceConnectorDefinition connector() {
        ResourceLocation pool = ResourceLocation.parse("mknpc:test/floor/branch");
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.BRANCH,
                Direction.SOUTH,
                new BlockPos(2, 1, 4),
                3,
                3,
                0,
                0,
                ResourceLocation.parse("mknpc:branch"),
                ResourceLocation.parse("mknpc:branch"),
                pool,
                pool
        );
    }

    private static boolean hasPiece(List<MKWorkspacePieceDefinition> pieces, String pieceName) {
        return pieces.stream().anyMatch(piece -> piece.pieceName().equals(pieceName));
    }

    private static MKWorkspacePieceDefinition piece(List<MKWorkspacePieceDefinition> pieces, String pieceName) {
        return pieces.stream()
                .filter(piece -> piece.pieceName().equals(pieceName))
                .findFirst()
                .orElseThrow();
    }
}
