package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MKTowerWorkspacePlanner implements MKWorkspaceTopologyPlanner {
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();
    private final MKTowerStackPlanner towerStackPlanner = new MKTowerStackPlanner();

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    private enum HallwayPathKind {
        MAIN("main"),
        BRANCH("branch");

        private final String serializedName;

        HallwayPathKind(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    @Override
    public String profileType() {
        return MKWorkspaceTopologyProfile.TOWER_PROFILE_TYPE;
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                profileType(),
                List.of(
                        new MKWorkspaceRegionSchema("tower.entry", "tower_stack", true),
                        new MKWorkspaceRegionSchema("tower.main", "tower_stack", true),
                        new MKWorkspaceRegionSchema("tower.basement", "tower_stack", true),
                        new MKWorkspaceRegionSchema("tower.top_cap", "tower_cap", true),
                        new MKWorkspaceRegionSchema("tower.basement_cap", "tower_cap", true),
                        new MKWorkspaceRegionSchema("tower.linear_runs", "linear_run", true)
                ),
                List.of(
                        new MKWorkspaceSlotSchema("tower.basement_cap", "tower.basement_cap", "cap", "tower.basement_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.basement_cap_approach", "tower.basement_cap", "approach", "tower.basement_cap_approach", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("tower.basement_floor", "tower.basement", "floor", "tower.basement_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("tower.basement_entry", "tower.basement", "entry", "tower.basement_entry", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.entry", "tower.entry", "entry", "tower.entry", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.main_floor", "tower.main", "floor", "tower.main_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("tower.top_cap_approach", "tower.top_cap", "approach", "tower.top_cap_approach", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("tower.top_cap", "tower.top_cap", "cap", "tower.top_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("tower.linear_run.main", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.main", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("tower.linear_run.branch", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.branch", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("tower.linear_run.branch_cap", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.branch_cap", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("tower.linear_run.main_ending", "tower.linear_runs", "enclosed_corridor", "tower.linear_run.main_ending", MKWorkspaceSlotSchema.Repeat.DERIVED)
                ),
                List.of(
                        new MKWorkspaceLinkSchema("tower.vertical.basement_cap_to_entry", "tower.basement_cap", "tower.entry", "vertical_stack"),
                        new MKWorkspaceLinkSchema("tower.vertical.entry_to_top_cap", "tower.entry", "tower.top_cap", "vertical_stack")
                ),
                List.of(
                        new MKWorkspaceRoleSchema("tower.entry", "floor", "room", false, true, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.main_floor", "floor", "room", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.top_cap_approach", "approach", "top_cap_approach", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.top_cap", "cap", "top_cap", true, false, Set.of("terminal_top")),
                        new MKWorkspaceRoleSchema("tower.basement_entry", "floor", "room", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.basement_floor", "floor", "room", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.basement_cap_approach", "approach", "basement_cap_approach", false, false, Set.of("vertical_access")),
                        new MKWorkspaceRoleSchema("tower.basement_cap", "cap", "terminal", true, false, Set.of("terminal_bottom")),
                        new MKWorkspaceRoleSchema("tower.linear_run.main", "connector", "room", false, false, Set.of("linear_run", "main_path")),
                        new MKWorkspaceRoleSchema("tower.linear_run.branch", "connector", "room", false, false, Set.of("linear_run", "branch_path")),
                        new MKWorkspaceRoleSchema("tower.linear_run.branch_cap", "connector", "room", true, false, Set.of("linear_run", "branch_cap")),
                        new MKWorkspaceRoleSchema("tower.linear_run.main_ending", "connector", "room", true, false, Set.of("linear_run", "main_ending"))
                )
        );
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>(towerStackPlanner.createRoomPieces(
                workspace,
                towerStackDefinition(workspace),
                workspace.familyDefinitions()));
        pieces.addAll(createLinearRunPieces(workspace));
        return List.copyOf(pieces);
    }

    private MKTowerStackDefinition towerStackDefinition(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().towerStackSettings("tower.primary")
                .map(MKTowerStackDefinition::towerPrimary)
                .orElseGet(() -> MKTowerStackDefinition.legacyTower(workspace.floorSettings()));
    }

    private List<MKPlannedPiece> createLinearRunPieces(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun).stream())
                .toList();
    }

    private List<MKPlannedPiece> createLinearRunPieces(MKStructureWorkspace workspace,
                                                       MKWorkspaceLinearRunFamilyDefinition linearRun) {
        ResolvedOpeningProfile opening = workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(linearRun.openingProfileId()))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()))
                .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " + linearRun.openingProfileId()));
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        if (!linearRun.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
            return List.of();
        }
        if (linearRun.allowOnMainPath()) {
            pieces.add(createLinearRunPiece(workspace, linearRun, opening, HallwayPathKind.MAIN));
        }
        if (linearRun.allowOnBranchPath()) {
            pieces.add(createLinearRunPiece(workspace, linearRun, opening, HallwayPathKind.BRANCH));
        }
        return List.copyOf(pieces);
    }

    private MKPlannedPiece createLinearRunPiece(MKStructureWorkspace workspace,
                                                MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                ResolvedOpeningProfile opening, HallwayPathKind pathKind) {
        int westOffset = Math.max(0, -linearRun.slopeDelta());
        int eastOffset = Math.max(0, linearRun.slopeDelta());
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", "linear_run_" + linearRun.linearRunId() + "_" + pathKind.serializedName);
        tags.put("workspace_topology_slot_id", topologySlotId(linearRun, pathKind));
        tags.put("workspace_topology_role_id", topologySlotId(linearRun, pathKind));
        tags.put("tower_piece_kind", "linear_run");
        tags.put("workspace_linear_run_family_id", linearRun.linearRunId());
        tags.put("workspace_linear_run_kind", linearRun.kind().getSerializedName());
        tags.put("workspace_linear_run_projection", linearRun.projection().getSerializedName());
        tags.put("workspace_linear_run_shape", MKWorkspaceLinearRunPieceShape.STRAIGHT.getSerializedName());
        tags.put("workspace_linear_run_path_kind", pathKind.serializedName);
        tags.put("workspace_linear_run_slope_delta", Integer.toString(linearRun.slopeDelta()));
        tags.put("workspace_opening_profile_id", linearRun.openingProfileId());
        applyFoundationTags(linearRun.foundationPolicy(), tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, linearRun));
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                pathKind == HallwayPathKind.MAIN, pathKind == HallwayPathKind.BRANCH, false, false).applyToTags(tags);
        String linearRunPool = hallwayPoolName(linearRun.openingProfileId(), pathKind);
        MKConnectorRole westRole = pathKind == HallwayPathKind.MAIN ? MKConnectorRole.MAIN_FORWARD : MKConnectorRole.BRANCH;
        MKConnectorRole eastRole = pathKind == HallwayPathKind.MAIN ? MKConnectorRole.MAIN_BACK : MKConnectorRole.BRANCH;
        return new MKPlannedPiece(
                MKWorkspacePieceRole.HALLWAY,
                "linear_run_" + linearRun.linearRunId() + "_" + pathKind.serializedName,
                linearRun.length(),
                linearRun.interiorWidth(),
                linearRun.interiorHeight() + Math.abs(linearRun.slopeDelta()),
                List.of(
                        new MKPlannedConnector(westRole, Direction.WEST,
                                opening.openingWidth(), opening.openingHeight(), 0, westOffset,
                                EMPTY_POOL, linearRunPool),
                        new MKPlannedConnector(eastRole, Direction.EAST,
                                opening.openingWidth(), opening.openingHeight(), 0, eastOffset,
                                EMPTY_POOL, linearRunPool)
                ),
                tags
        );
    }

    private String hallwayPoolName(String openingProfileId, HallwayPathKind pathKind) {
        return LINEAR_RUN_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    private void applyFoundationTags(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy policy,
                                     Map<String, String> tags) {
        if (policy.enabled()) {
            tags.put(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy.MODE_TAG,
                    policy.mode().getSerializedName());
        }
    }

    private String topologySlotId(MKWorkspaceLinearRunFamilyDefinition linearRun, HallwayPathKind pathKind) {
        if (pathKind == HallwayPathKind.MAIN) {
            return linearRun.allowOnMainPath() && !linearRun.allowOnBranchPath() ?
                    linearRun.topologySlotId() : "tower.linear_run.main";
        }
        return linearRun.allowOnBranchPath() && !linearRun.allowOnMainPath() ?
                linearRun.topologySlotId() : "tower.linear_run.branch";
    }
}

