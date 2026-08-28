package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceGeometry;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateCloneTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspacePlannerChangePlan;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MKWorkspacePlanner extends MKWorkspacePiecePlanner {
    ResourceLocation plannerId();

    MKWorkspaceTopologySchema schema();

    MKWorkspaceTopologyProfile createDefaultTopologyProfile();

    default List<MKWorkspaceRoomFamilyDefinition> createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return List.of();
    }

    default List<MKWorkspaceLinearRunFamilyDefinition> createDefaultLinearRunFamilyDefinitions(
            MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
        return List.of();
    }

    default List<MKPlannedPiece> createInsertFamilyTemplatePieces(MKStructureWorkspace workspace) {
        return createCommonInsertFamilyTemplatePieces(workspace);
    }

    static List<MKPlannedPiece> createCommonInsertFamilyTemplatePieces(MKStructureWorkspace workspace) {
        return workspace.insertFamilies().stream()
                .map(MKWorkspacePlanner::createCommonInsertFamilyTemplatePiece)
                .toList();
    }

    private static MKPlannedPiece createCommonInsertFamilyTemplatePiece(MKWorkspaceInsertFamilyDefinition insertFamily) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        String kindName = insertFamily.kind().getSerializedName();
        String slotId = "workspace.insert_family." + kindName;
        String identityPrefix = insertFamily.kind() == MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY ?
                "floor_insert_family" :
                "insert_socket_family";
        String identity = insertFamily.kind() == MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY ?
                "floor.insert_family." + insertFamily.familyId() :
                "insert_socket.insert_family." + insertFamily.familyId();
        tags.put("topology_role", slotId);
        tags.put("workspace_topology_slot_id", slotId);
        tags.put("workspace_topology_role_id", slotId);
        tags.put(MKWorkspacePieceGeometry.TAG_TOWER_PIECE_KIND,
                insertFamily.kind() == MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY ?
                        MKWorkspacePieceGeometry.TOWER_PIECE_KIND_FLOOR_LINK_INSERT :
                        "insert_socket_template");
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID, insertFamily.familyId());
        tags.put(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_KIND, kindName);
        MKWorkspaceStableSlotIdentity.apply(tags, identityPrefix, identity);
        tags.put("workspace_insert_family_width", Integer.toString(insertFamily.width()));
        tags.put("workspace_insert_family_height", Integer.toString(insertFamily.height()));
        tags.put("workspace_insert_family_depth", Integer.toString(insertFamily.depth()));
        if (!insertFamily.templateCloneSourcePieceName().isBlank()) {
            tags.put(MKWorkspaceTemplateCloneTags.SOURCE_PIECE_NAME_TAG,
                    insertFamily.templateCloneSourcePieceName());
        }
        return new MKPlannedPiece(
                slotId,
                insertFamily.familyId(),
                insertFamily.width(),
                insertFamily.depth(),
                insertFamily.height(),
                List.of(),
                tags
        );
    }

    default List<String> validateTopology(MKStructureWorkspace workspace) {
        return List.of();
    }

    default Map<String, String> migrateImportedRuntimeTags(MKStructureWorkspace workspace,
                                                           Map<String, String> sourceTags) {
        return sourceTags;
    }

    default boolean allowsRuntimePoolChild(String runtimePoolPath, Map<String, String> childTags) {
        return true;
    }

    default boolean usesRuntimePathFilters(String runtimePoolPath) {
        return true;
    }

    /**
     * Claims an exact persistent definition mutation that core classification does not understand.
     * Returning empty deliberately selects the safe full-regeneration fallback.
     */
    default Optional<MKWorkspacePlannerChangePlan> prepareDefinitionChange(
            ServerPlayer player,
            MKStructureWorkspace existing,
            MKStructureWorkspace requested,
            MKWorkspaceMutationPreflight corePreflight) {
        return Optional.empty();
    }
}
