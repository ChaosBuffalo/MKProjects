package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceImportService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
                List.of(runtimeStartPiece(draft), insertAuthoringTemplatePiece(draft, insertFamily),
                        insertVariantPiece(draft, insertFamily))
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
        assertTrue(manifest.runtimeHints().templateGroups().stream()
                .anyMatch(group -> group.baseName().equals(insertFamily.familyId()) &&
                        group.pieceMetadata().terminal() &&
                        group.pieceMetadata().branchCap()));
        assertEquals(1, manifest.settings().insertFamilies().size());
        assertTrue(manifest.templateGroups().stream()
                .anyMatch(group -> group.baseName().equals("crypt_link_supports")));

        MKStructureWorkspace imported = new MKStructureWorkspaceImportService()
                .workspaceFromManifest(UUID.randomUUID(), BlockPos.ZERO, 123L, manifest);
        assertEquals(List.of(insertFamily), imported.insertFamilies());
    }

    @Test
    void insertFamilyAuthoringTemplatesAreNotRuntimePoolChildren() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceInsertFamilyDefinition insertFamily =
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("crypt_link_supports", 5, 4, 3);
        MKStructureWorkspace workspace = workspaceWithInsertFamily(
                draft,
                insertFamily,
                List.of(runtimeStartPiece(draft), insertAuthoringTemplatePiece(draft, insertFamily))
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 5, "test");

        ResourceLocation expectedPool = MKWorkspaceInsertFamilyDefinition.poolId(
                workspace.namespace(), workspace.structureName(), insertFamily.familyId());
        assertFalse(manifest.runtimeHints().pools().stream()
                .anyMatch(candidate -> candidate.poolId().equals(expectedPool)));
    }

    @Test
    void insertFamilyRuntimePoolHonorsTaggedVariantIndexForImportedWorkspaces() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceInsertFamilyDefinition insertFamily =
                MKWorkspaceInsertFamilyDefinition.floorLinkHallway("crypt_link_supports", 5, 4, 3);
        MKStructureWorkspace workspace = workspaceWithInsertFamily(
                draft,
                insertFamily,
                List.of(runtimeStartPiece(draft), taggedInsertVariantPieceWithLegacyIdentity(draft, insertFamily))
        );

        MKWorkspaceExportManifest manifest = MKWorkspaceExportManifest.fromWorkspace(workspace, 5, "test");

        ResourceLocation expectedPool = MKWorkspaceInsertFamilyDefinition.poolId(
                workspace.namespace(), workspace.structureName(), insertFamily.familyId());
        MKWorkspaceExportManifest.ExportRuntimePool pool = manifest.runtimeHints().pools().stream()
                .filter(candidate -> candidate.poolId().equals(expectedPool))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(insertFamily.familyId()), pool.childBaseNames());
        assertTrue(manifest.runtimeHints().templateGroups().stream()
                .anyMatch(group -> group.baseName().equals(insertFamily.familyId()) &&
                        group.pieceMetadata().terminal() &&
                        group.pieceMetadata().branchCap()));
    }

    @Test
    void connectorJigsawFinalStateUsesAuthoredTemplateValueWhenPresent() {
        MKWorkspaceConnectorDefinition connector = connectorWithFinalState("minecraft:air");
        CompoundTag authoredJigsaw = new CompoundTag();
        authoredJigsaw.putString("final_state", "minecraft:lava[level=0]");

        assertEquals("minecraft:lava[level=0]", MKWorkspaceExportArchiveWriter
                .readAuthoredFinalState(authoredJigsaw)
                .orElseThrow());
        assertEquals("minecraft:lava[level=0]", MKWorkspaceExportArchiveWriter
                .resolveConnectorFinalState(connector, authoredJigsaw.getString("final_state")));
        assertEquals("minecraft:air", MKWorkspaceExportArchiveWriter.resolveConnectorFinalState(connector, null));
        assertEquals("minecraft:air", MKWorkspaceExportArchiveWriter.resolveConnectorFinalState(connector, ""));
    }

    @Test
    void missingAuthoredJigsawFinalStateIsIgnored() {
        assertTrue(MKWorkspaceExportArchiveWriter.readAuthoredFinalState(null).isEmpty());
        assertTrue(MKWorkspaceExportArchiveWriter.readAuthoredFinalState(new CompoundTag()).isEmpty());

        CompoundTag blankJigsaw = new CompoundTag();
        blankJigsaw.putString("final_state", "");
        assertTrue(MKWorkspaceExportArchiveWriter.readAuthoredFinalState(blankJigsaw).isEmpty());
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
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "1");
        return piece(workspace, insertFamily.familyId() + "_variant_1", insertFamily.familyId(), 1, tags);
    }

    private static MKWorkspacePieceDefinition insertAuthoringTemplatePiece(MKStructureWorkspace workspace,
                                                                          MKWorkspaceInsertFamilyDefinition insertFamily) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("workspace_base_name", insertFamily.familyId());
        tags.put("workspace_piece_kind", "template");
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, insertFamily.familyId());
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND, insertFamily.kind().getSerializedName());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0");
        return piece(workspace, insertFamily.familyId() + "_template", insertFamily.familyId(), 0, tags);
    }

    private static MKWorkspacePieceDefinition taggedInsertVariantPieceWithLegacyIdentity(MKStructureWorkspace workspace,
                                                                                        MKWorkspaceInsertFamilyDefinition insertFamily) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("workspace_base_name", insertFamily.familyId());
        tags.put("workspace_piece_kind", "instance");
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, insertFamily.familyId());
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND, insertFamily.kind().getSerializedName());
        tags.put(MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "1");
        return piece(workspace, insertFamily.familyId() + "_legacy_variant_1", insertFamily.familyId(), 0, tags);
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

    private static MKWorkspaceConnectorDefinition connectorWithFinalState(String finalState) {
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.BRANCH,
                Direction.NORTH,
                BlockPos.ZERO,
                1,
                1,
                0,
                0,
                ResourceLocation.parse("mk:test_name"),
                ResourceLocation.parse("mk:test_target"),
                ResourceLocation.parse("minecraft:empty"),
                ResourceLocation.parse("minecraft:empty"),
                "north_up",
                finalState,
                "aligned"
        );
    }
}
