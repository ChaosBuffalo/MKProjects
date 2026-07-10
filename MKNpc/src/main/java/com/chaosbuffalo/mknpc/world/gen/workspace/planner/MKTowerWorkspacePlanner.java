package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStableSlotIdentity;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPathSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackFloorCounts;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MKTowerWorkspacePlanner implements MKWorkspacePlanner {
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
    public static final String PRIMARY_STACK_ID = "tower.primary";
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();
    private final MKWorkspaceVerticalStackPlanner verticalStackPlanner = new MKWorkspaceVerticalStackPlanner(PRIMARY_STACK_ID);
    private final MKFloorTopologyPlanner floorTopologyPlanner = new MKFloorTopologyPlanner();

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    private enum LinearRunPathKind {
        MAIN("main"),
        BRANCH("branch");

        private final String serializedName;

        LinearRunPathKind(String serializedName) {
            this.serializedName = serializedName;
        }
    }

    @Override
    public net.minecraft.resources.ResourceLocation plannerId() {
        return PLANNER_ID;
    }

    public static MKWorkspaceTopologyProfile defaultTopologyProfile() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        return new MKWorkspaceTopologyProfile(PLANNER_ID,
                List.of(new MKWorkspaceVerticalStackSettings("tower.primary",
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_MAIN_FLOORS,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_FLOORS,
                        dimensions.roomHeight(), dimensions.roomWidth(), dimensions.roomLength(),
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                        MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED)),
                List.of(),
                MKWorkspaceTopologyPathSettings.defaults(),
                List.of(),
                TerrainAdjustment.BEARD_THIN);
    }

    @Override
    public MKWorkspaceTopologyProfile createDefaultTopologyProfile() {
        return defaultTopologyProfile();
    }

    public static List<MKWorkspaceRoomFamilyDefinition> defaultRoomFamilyDefinitions() {
        return defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions());
    }

    public static List<MKWorkspaceRoomFamilyDefinition> defaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return List.of(
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("entry", MKWorkspaceVerticalStackSlot.ENTRY,
                        PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(new MKFamilyHorizontalExitDefinition(Direction.SOUTH,
                                MKHorizontalExitPathKind.INGRESS, "main_opening",
                                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION)),
                        0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("floor_main", MKWorkspaceVerticalStackSlot.MAIN_FLOOR,
                        PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("top_cap_approach",
                        MKWorkspaceVerticalStackSlot.TOP_CAP_APPROACH, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("top_cap", MKWorkspaceVerticalStackSlot.TOP_CAP,
                        PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_entry",
                        MKWorkspaceVerticalStackSlot.BASEMENT_ENTRY, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_main",
                        MKWorkspaceVerticalStackSlot.BASEMENT_FLOOR, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_cap_approach",
                        MKWorkspaceVerticalStackSlot.BASEMENT_CAP_APPROACH, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null),
                MKWorkspaceRoomFamilyDefinition.forVerticalStackSlot("basement_cap",
                        MKWorkspaceVerticalStackSlot.BASEMENT_CAP, PRIMARY_STACK_ID, true, 0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0, null, null)
        );
    }

    @Override
    public List<MKWorkspaceRoomFamilyDefinition> createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return defaultRoomFamilyDefinitions(dimensions);
    }

    public static List<MKWorkspaceLinearRunFamilyDefinition> defaultLinearRunFamilyDefinitions(
            MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
        return List.of(
                new MKWorkspaceLinearRunFamilyDefinition(
                        "main",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "main_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        true,
                        false,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        MKWorkspaceFoundationPolicy.none(),
                        null
                ),
                new MKWorkspaceLinearRunFamilyDefinition(
                        "branch",
                        MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR,
                        "branch_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        false,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        MKWorkspaceFoundationPolicy.none(),
                        null
                )
        );
    }

    @Override
    public List<MKWorkspaceLinearRunFamilyDefinition> createDefaultLinearRunFamilyDefinitions(
            MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
        return defaultLinearRunFamilyDefinitions(dimensions, palette);
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                plannerId(),
                List.of(
                        new MKWorkspaceRegionSchema("tower.primary.entry", "vertical_stack", true),
                        new MKWorkspaceRegionSchema("tower.primary.main", "vertical_stack", true),
                        new MKWorkspaceRegionSchema("tower.primary.basement", "vertical_stack", true),
                        new MKWorkspaceRegionSchema("tower.primary.top_cap", "tower_cap", true),
                        new MKWorkspaceRegionSchema("tower.primary.basement_cap", "tower_cap", true),
                        new MKWorkspaceRegionSchema("tower.linear_runs", "linear_run", true)
                ),
                schemaSlots(),
                List.of(
                        new MKWorkspaceLinkSchema("tower.vertical.basement_cap_to_entry",
                                slotId(MKWorkspaceVerticalStackSlot.BASEMENT_CAP),
                                slotId(MKWorkspaceVerticalStackSlot.ENTRY), "vertical_stack"),
                        new MKWorkspaceLinkSchema("tower.vertical.entry_to_top_cap",
                                slotId(MKWorkspaceVerticalStackSlot.ENTRY),
                                slotId(MKWorkspaceVerticalStackSlot.TOP_CAP), "vertical_stack")
                ),
                schemaRoles()
        );
    }

    private static List<MKWorkspaceSlotSchema> schemaSlots() {
        ArrayList<MKWorkspaceSlotSchema> slots = new ArrayList<>();
        for (MKWorkspaceVerticalStackSlot stackSlot : MKWorkspaceVerticalStackSlot.schemaOrder()) {
            slots.add(new MKWorkspaceSlotSchema(
                    slotId(stackSlot),
                    regionId(stackSlot),
                    stackSlot.roleKind(),
                    slotId(stackSlot),
                    repeatFor(stackSlot)
            ));
        }
        slots.add(new MKWorkspaceSlotSchema("tower.linear_run.main", "tower.linear_runs",
                "enclosed_corridor", "tower.linear_run.main", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("tower.linear_run.branch", "tower.linear_runs",
                "enclosed_corridor", "tower.linear_run.branch", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("tower.linear_run.branch_cap", "tower.linear_runs",
                "enclosed_corridor", "tower.linear_run.branch_cap", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("tower.linear_run.main_ending", "tower.linear_runs",
                "enclosed_corridor", "tower.linear_run.main_ending", MKWorkspaceSlotSchema.Repeat.DERIVED));
        return List.copyOf(slots);
    }

    private static List<MKWorkspaceRoleSchema> schemaRoles() {
        ArrayList<MKWorkspaceRoleSchema> roles = new ArrayList<>();
        for (MKWorkspaceVerticalStackSlot stackSlot : MKWorkspaceVerticalStackSlot.schemaOrder()) {
            roles.add(new MKWorkspaceRoleSchema(
                    slotId(stackSlot),
                    stackSlot.roleKind(),
                    stackSlot.pieceKind(),
                    stackSlot.terminal(),
                    stackSlot == MKWorkspaceVerticalStackSlot.ENTRY,
                    traitsFor(stackSlot)
            ));
        }
        roles.add(new MKWorkspaceRoleSchema("tower.linear_run.main", "connector", "room",
                false, false, Set.of("linear_run", "main_path")));
        roles.add(new MKWorkspaceRoleSchema("tower.linear_run.branch", "connector", "room",
                false, false, Set.of("linear_run", "branch_path")));
        roles.add(new MKWorkspaceRoleSchema("tower.linear_run.branch_cap", "connector", "room",
                true, false, Set.of("linear_run", "branch_cap")));
        roles.add(new MKWorkspaceRoleSchema("tower.linear_run.main_ending", "connector", "room",
                true, false, Set.of("linear_run", "main_ending")));
        return List.copyOf(roles);
    }

    private static String slotId(MKWorkspaceVerticalStackSlot stackSlot) {
        return stackSlot.slotId(PRIMARY_STACK_ID);
    }

    private static String regionId(MKWorkspaceVerticalStackSlot stackSlot) {
        return switch (stackSlot) {
            case BASEMENT_CAP, BASEMENT_CAP_APPROACH -> "tower.primary.basement_cap";
            case BASEMENT_ENTRY, BASEMENT_FLOOR -> "tower.primary.basement";
            case ENTRY -> "tower.primary.entry";
            case MAIN_FLOOR -> "tower.primary.main";
            case TOP_CAP_APPROACH, TOP_CAP -> "tower.primary.top_cap";
        };
    }

    private static MKWorkspaceSlotSchema.Repeat repeatFor(MKWorkspaceVerticalStackSlot stackSlot) {
        return switch (stackSlot) {
            case BASEMENT_CAP_APPROACH, TOP_CAP_APPROACH -> MKWorkspaceSlotSchema.Repeat.OPTIONAL;
            case BASEMENT_FLOOR, MAIN_FLOOR -> MKWorkspaceSlotSchema.Repeat.RANGE;
            default -> MKWorkspaceSlotSchema.Repeat.FIXED;
        };
    }

    private static Set<String> traitsFor(MKWorkspaceVerticalStackSlot stackSlot) {
        return switch (stackSlot) {
            case TOP_CAP -> Set.of("terminal_top");
            case BASEMENT_CAP -> Set.of("terminal_bottom");
            default -> Set.of("vertical_access");
        };
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>(verticalStackPlanner.createRoomPieces(
                workspace,
                verticalStackDefinition(workspace),
                workspace.familyDefinitions()));
        pieces.addAll(createLinearRunPieces(workspace));
        pieces.addAll(floorTopologyPlanner.createFloorTopologyPieces(workspace, workspace.familyDefinitions()));
        return List.copyOf(pieces);
    }

    private MKWorkspaceVerticalStackDefinition verticalStackDefinition(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().verticalStackSettings(PRIMARY_STACK_ID)
                .map(MKWorkspaceVerticalStackDefinition::towerPrimary)
                .orElseThrow(() -> new IllegalStateException("tower topology is missing " + PRIMARY_STACK_ID + " stack settings"));
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
            pieces.add(createLinearRunPiece(workspace, linearRun, opening, LinearRunPathKind.MAIN));
        }
        if (linearRun.allowOnBranchPath()) {
            pieces.add(createLinearRunPiece(workspace, linearRun, opening, LinearRunPathKind.BRANCH));
        }
        return List.copyOf(pieces);
    }

    private MKPlannedPiece createLinearRunPiece(MKStructureWorkspace workspace,
                                                MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                ResolvedOpeningProfile opening, LinearRunPathKind pathKind) {
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
        MKWorkspaceStableSlotIdentity.apply(tags, "tower_linear_run",
                "tower.linear_run." + linearRun.linearRunId() + "." + pathKind.serializedName);
        applyFoundationTags(linearRun.foundationPolicy(), tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, linearRun));
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                pathKind == LinearRunPathKind.MAIN, pathKind == LinearRunPathKind.BRANCH, false, false).applyToTags(tags);
        String linearRunPool = linearRunPoolName(linearRun.openingProfileId(), pathKind);
        MKConnectorRole westRole = pathKind == LinearRunPathKind.MAIN ? MKConnectorRole.MAIN_FORWARD : MKConnectorRole.BRANCH;
        MKConnectorRole eastRole = pathKind == LinearRunPathKind.MAIN ? MKConnectorRole.MAIN_BACK : MKConnectorRole.BRANCH;
        return new MKPlannedPiece(
                topologySlotId(linearRun, pathKind),
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

    private String linearRunPoolName(String openingProfileId, LinearRunPathKind pathKind) {
        return LINEAR_RUN_POOL_PREFIX + "/" + pathKind.serializedName + "/" + openingProfileId;
    }

    private void applyFoundationTags(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy policy,
                                     Map<String, String> tags) {
        if (policy.enabled()) {
            tags.put(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy.MODE_TAG,
                    policy.mode().getSerializedName());
        }
    }

    private String topologySlotId(MKWorkspaceLinearRunFamilyDefinition linearRun, LinearRunPathKind pathKind) {
        if (pathKind == LinearRunPathKind.MAIN) {
            return linearRun.allowOnMainPath() && !linearRun.allowOnBranchPath() ?
                    linearRun.topologySlotId() : "tower.linear_run.main";
        }
        return linearRun.allowOnBranchPath() && !linearRun.allowOnMainPath() ?
                linearRun.topologySlotId() : "tower.linear_run.branch";
    }
}

