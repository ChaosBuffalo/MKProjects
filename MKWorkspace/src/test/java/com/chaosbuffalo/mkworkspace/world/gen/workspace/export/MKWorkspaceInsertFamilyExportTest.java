package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceImportService;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceInsertFamilyExportTest {
    @Test
    void plannerCreatesExactBoundsAuthoringTemplateForInsertFamilies() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceInsertFamilyDefinition insertFamily =
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("crypt_link_supports", 5, 4, 3);
        MKStructureWorkspace workspace = workspaceWithInsertFamily(draft, insertFamily, List.of());

        MKPlannedPiece plannedInsert = new MKTowerWorkspacePlanner().createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.pieceName().equals(insertFamily.familyId()))
                .findFirst()
                .orElseThrow();

        assertEquals(insertFamily.width(), plannedInsert.interiorWidth());
        assertEquals(insertFamily.depth(), plannedInsert.interiorLength());
        assertEquals(insertFamily.height(), plannedInsert.interiorHeight());
        assertEquals("floor_link_insert", plannedInsert.tags().get("tower_piece_kind"));
        assertEquals(insertFamily.familyId(),
                plannedInsert.tags().get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID));
        assertTrue(plannedInsert.connectors().isEmpty());

        MKWorkspaceGridLayout.Placement placement = new MKWorkspaceGridLayout()
                .assignPlacements(workspace.anchor(), List.of(plannedInsert), workspace.shellMargin(),
                        workspace.exteriorAirMargin(), workspace.previewMargin(), 4, 4)
                .getFirst();
        assertEquals(insertFamily.width() + (2 * workspace.previewMargin()), placement.previewBounds().getXSpan());
        assertEquals(insertFamily.depth() + (2 * workspace.previewMargin()), placement.previewBounds().getZSpan());
        assertEquals(insertFamily.height(), placement.previewBounds().getYSpan());
    }

    @Test
    void insertFamiliesExportDedicatedRuntimePools() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceInsertFamilyDefinition insertFamily =
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("crypt_link_supports", 5, 4, 3);
        MKStructureWorkspace workspace = workspaceWithInsertFamily(
                draft,
                insertFamily,
                List.of(runtimeStartPiece(draft), insertVariantPiece(draft, insertFamily))
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 5, "test");

        ResourceLocation expectedPool = MKWorkspaceInsertFamilyDefinition.poolId(
                workspace.namespace(), workspace.structureName(), insertFamily.familyId());
        MKWorkspaceExportManifest.ExportRuntimePool pool = manifest.runtimeHints().pools().stream()
                .filter(candidate -> candidate.poolId().equals(expectedPool))
                .findFirst()
                .orElseThrow();
        assertEquals("insert_families/crypt_link_supports", pool.baseName());
        assertEquals(List.of("crypt_link_supports"), pool.childBaseNames());
        assertEquals(1, manifest.settings().insertFamilies().size());
        assertTrue(manifest.templateGroups().stream()
                .anyMatch(group -> group.baseName().equals("crypt_link_supports")));

        MKStructureWorkspace imported = new MKStructureWorkspaceImportService()
                .workspaceFromManifest(UUID.randomUUID(), BlockPos.ZERO, 123L, manifest);
        assertEquals(List.of(insertFamily), imported.insertFamilies());
    }

    private static MKStructureWorkspace workspaceWithInsertFamily(MKStructureWorkspace draft,
                                                                  MKWorkspaceInsertFamilyDefinition insertFamily,
                                                                  List<MKWorkspacePieceDefinition> pieces) {
        return new MKStructureWorkspace(
                draft.id(),
                draft.anchor(),
                draft.namespace(),
                draft.structureName(),
                draft.topologyProfile(),
                draft.dimensions(),
                draft.palette(),
                draft.stairConfig(),
                draft.verticalAccessPlacement(),
                draft.shellMargin(),
                draft.exteriorAirMargin(),
                draft.previewMargin(),
                draft.verticalAccessSpec(),
                draft.familyDefinitions(),
                draft.openingProfiles(),
                draft.linearRunFamilies(),
                List.of(insertFamily),
                draft.createdAt(),
                draft.updatedAt(),
                pieces,
                draft.layerStates()
        );
    }

    private static MKWorkspacePieceDefinition runtimeStartPiece(MKStructureWorkspace workspace) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("workspace_base_name", "start_room");
        tags.put("workspace_piece_kind", "instance");
        new MKWorkspaceRuntimePieceInfo(true, MKJigsawPieceRole.ROOM, 0, 0, true, false, false,
                false).applyToTags(tags);
        return piece(workspace, "start_room_0", "start_room", 0, tags);
    }

    private static MKWorkspacePieceDefinition insertVariantPiece(MKStructureWorkspace workspace,
                                                                 MKWorkspaceInsertFamilyDefinition insertFamily) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("workspace_base_name", insertFamily.familyId());
        tags.put("workspace_piece_kind", "instance");
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, insertFamily.familyId());
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND, insertFamily.kind().getSerializedName());
        return piece(workspace, insertFamily.familyId() + "_0", insertFamily.familyId(), 0, tags);
    }

    private static MKWorkspacePieceDefinition piece(MKStructureWorkspace workspace, String pieceName, String roleId,
                                                    int variantIndex, Map<String, String> tags) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspace.id(),
                pieceName,
                roleId,
                variantIndex,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 4, 3, 2),
                new BoundingBox(0, 0, 0, 4, 3, 2),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }
}
