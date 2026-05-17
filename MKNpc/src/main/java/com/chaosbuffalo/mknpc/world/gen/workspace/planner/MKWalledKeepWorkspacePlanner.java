package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MKWalledKeepWorkspacePlanner implements MKWorkspaceTopologyPlanner {
    private static final String EMPTY_POOL = "minecraft:empty";
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    @Override
    public String profileType() {
        return MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE;
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                profileType(),
                List.of(
                        new MKWorkspaceRegionSchema("keep.center_tower", "tower_stack", true),
                        new MKWorkspaceRegionSchema("keep.corner_towers", "tower_stack", true),
                        new MKWorkspaceRegionSchema("keep.wall_runs", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.parapets", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.walkways", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.gates", "entry", false),
                        new MKWorkspaceRegionSchema("keep.courtyard", "open_area", false)
                ),
                List.of(
                        new MKWorkspaceSlotSchema("keep.center.basement_cap", "keep.center_tower", "cap", "keep.center.basement_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("keep.center.basement_floor", "keep.center_tower", "floor", "keep.center.basement_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("keep.center.entry", "keep.center_tower", "entry", "keep.center.entry", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("keep.center.main_floor", "keep.center_tower", "floor", "keep.center.main_floor", MKWorkspaceSlotSchema.Repeat.RANGE),
                        new MKWorkspaceSlotSchema("keep.center.top_cap", "keep.center_tower", "cap", "keep.center.top_cap", MKWorkspaceSlotSchema.Repeat.FIXED),
                        new MKWorkspaceSlotSchema("keep.corner.shared", "keep.corner_towers", "tower_stack", "keep.corner.shared", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.north_west", "keep.corner_towers", "tower_stack", "keep.corner.north_west", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.north_east", "keep.corner_towers", "tower_stack", "keep.corner.north_east", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.south_east", "keep.corner_towers", "tower_stack", "keep.corner.south_east", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.corner.south_west", "keep.corner_towers", "tower_stack", "keep.corner.south_west", MKWorkspaceSlotSchema.Repeat.OPTIONAL),
                        new MKWorkspaceSlotSchema("keep.wall.north", "keep.wall_runs", "solid_wall", "keep.wall.north", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.wall.east", "keep.wall_runs", "solid_wall", "keep.wall.east", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.wall.south", "keep.wall_runs", "solid_wall", "keep.wall.south", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.wall.west", "keep.wall_runs", "solid_wall", "keep.wall.west", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.north", "keep.parapets", "parapet", "keep.parapet.north", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.east", "keep.parapets", "parapet", "keep.parapet.east", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.south", "keep.parapets", "parapet", "keep.parapet.south", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.parapet.west", "keep.parapets", "parapet", "keep.parapet.west", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.north", "keep.walkways", "open_walkway", "keep.walkway.north", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.east", "keep.walkways", "open_walkway", "keep.walkway.east", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.south", "keep.walkways", "open_walkway", "keep.walkway.south", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.walkway.west", "keep.walkways", "open_walkway", "keep.walkway.west", MKWorkspaceSlotSchema.Repeat.DERIVED),
                        new MKWorkspaceSlotSchema("keep.gate.main", "keep.gates", "entry", "keep.gate.main", MKWorkspaceSlotSchema.Repeat.OPTIONAL)
                ),
                List.of(
                        new MKWorkspaceLinkSchema("keep.center.vertical", "keep.center.basement_cap", "keep.center.top_cap", "vertical_access_group:keep.center"),
                        new MKWorkspaceLinkSchema("keep.corner.north_west.vertical", "keep.corner.north_west", "keep.parapet.north", "vertical_access_group:keep.corner.north_west"),
                        new MKWorkspaceLinkSchema("keep.corner.north_east.vertical", "keep.corner.north_east", "keep.parapet.east", "vertical_access_group:keep.corner.north_east"),
                        new MKWorkspaceLinkSchema("keep.corner.south_east.vertical", "keep.corner.south_east", "keep.parapet.south", "vertical_access_group:keep.corner.south_east"),
                        new MKWorkspaceLinkSchema("keep.corner.south_west.vertical", "keep.corner.south_west", "keep.parapet.west", "vertical_access_group:keep.corner.south_west"),
                        new MKWorkspaceLinkSchema("keep.wall.north", "keep.corner.north_west", "keep.corner.north_east", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.wall.east", "keep.corner.north_east", "keep.corner.south_east", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.wall.south", "keep.corner.south_west", "keep.corner.south_east", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.wall.west", "keep.corner.north_west", "keep.corner.south_west", "linear_run")
                ),
                List.of(
                        new MKWorkspaceRoleSchema("keep.center.entry", "floor", "room", false, true, Set.of("vertical_access", "center_tower")),
                        new MKWorkspaceRoleSchema("keep.center.main_floor", "floor", "room", false, false, Set.of("vertical_access", "center_tower")),
                        new MKWorkspaceRoleSchema("keep.center.top_cap", "cap", "top_cap", true, false, Set.of("terminal_top", "center_tower")),
                        new MKWorkspaceRoleSchema("keep.corner.shared", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "shared_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.north_west", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.north_east", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.south_east", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.corner.south_west", "tower", "room", false, false, Set.of("vertical_access", "corner_tower", "unique_corner_template")),
                        new MKWorkspaceRoleSchema("keep.wall.north", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.wall.east", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.wall.south", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.wall.west", "linear_run", "wall", false, false, Set.of("solid_wall")),
                        new MKWorkspaceRoleSchema("keep.walkway.north", "linear_run", "walkway", false, false, Set.of("open_walkway", "terrain_matched_allowed")),
                        new MKWorkspaceRoleSchema("keep.parapet.north", "linear_run", "parapet", false, false, Set.of("parapet"))
                )
        );
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().startsWith("keep."))
                .map(family -> createRoomPiece(workspace, family))
                .forEach(pieces::add);
        workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().startsWith("keep."))
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun).stream())
                .forEach(pieces::add);
        return List.copyOf(pieces);
    }

    private MKPlannedPiece createRoomPiece(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        int shaftSize = workspace.verticalAccessSpec().shaftSize();
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        if (family.supportsVerticalAccess()) {
            if (family.hasVerticalAccess(Direction.UP)) {
                connectors.add(new MKPlannedConnector(MKConnectorRole.CONNECT_UP, Direction.UP, shaftSize, shaftSize,
                        verticalPool(family.verticalAccessGroupId(), Direction.UP),
                        verticalPool(family.verticalAccessGroupId(), Direction.DOWN)));
            }
            if (family.hasVerticalAccess(Direction.DOWN)) {
                connectors.add(new MKPlannedConnector(MKConnectorRole.CONNECT_DOWN, Direction.DOWN, shaftSize, shaftSize,
                        verticalPool(family.verticalAccessGroupId(), Direction.DOWN),
                        verticalPool(family.verticalAccessGroupId(), Direction.UP)));
            }
        }
        return new MKPlannedPiece(
                family.pieceRole(),
                family.baseName(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                connectors,
                buildRoomTags(workspace, family)
        );
    }

    private List<MKPlannedPiece> createLinearRunPieces(MKStructureWorkspace workspace,
                                                       MKWorkspaceLinearRunFamilyDefinition linearRun) {
        if (!linearRun.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
            return List.of();
        }
        ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, linearRun.openingProfileId())
                .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " +
                        linearRun.openingProfileId()));
        DirectionPair directions = directionsForSlot(linearRun.topologySlotId());
        return List.of(new MKPlannedPiece(
                MKWorkspacePieceRole.HALLWAY,
                linearRun.linearRunId(),
                directions.eastWest() ? linearRun.length() : linearRun.interiorWidth(),
                directions.eastWest() ? linearRun.interiorWidth() : linearRun.length(),
                linearRun.interiorHeight() + Math.abs(linearRun.slopeDelta()),
                List.of(
                        new MKPlannedConnector(MKConnectorRole.BRANCH, directions.negative(),
                                opening.openingWidth(), opening.openingHeight(), 0, 0,
                                linearRunPool(linearRun.topologySlotId()), linearRunPool(linearRun.topologySlotId())),
                        new MKPlannedConnector(MKConnectorRole.BRANCH, directions.positive(),
                                opening.openingWidth(), opening.openingHeight(), 0, Math.max(0, linearRun.slopeDelta()),
                                linearRunPool(linearRun.topologySlotId()), linearRunPool(linearRun.topologySlotId()))
                ),
                buildLinearRunTags(workspace, linearRun)
        ));
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", family.topologySlotId());
        tags.put("workspace_topology_slot_id", family.topologySlotId());
        tags.put("workspace_topology_role_id", family.topologySlotId());
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_category", family.category().getSerializedName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", family.horizontalExtrusionMode().getSerializedName());
        applyFoundationTags(family.foundationPolicy(), tags);
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
            tags.put("workspace_vertical_access_group_id", family.verticalAccessGroupId());
            tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG, workspace.verticalAccessSpec().placement().getSerializedName());
            tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, verticalAccessDirectionTag(family));
        }
        runtimeInfoForRoom(family).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, family));
        return tags;
    }

    private Map<String, String> buildLinearRunTags(MKStructureWorkspace workspace,
                                                   MKWorkspaceLinearRunFamilyDefinition linearRun) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", linearRun.topologySlotId());
        tags.put("workspace_topology_slot_id", linearRun.topologySlotId());
        tags.put("workspace_topology_role_id", linearRun.topologySlotId());
        tags.put("tower_piece_kind", "linear_run");
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_linear_run_family_id", linearRun.linearRunId());
        tags.put("workspace_linear_run_kind", linearRun.kind().getSerializedName());
        tags.put("workspace_linear_run_projection", linearRun.projection().getSerializedName());
        tags.put("workspace_linear_run_shape", MKWorkspaceLinearRunPieceShape.STRAIGHT.getSerializedName());
        tags.put("workspace_linear_run_path_kind", "keep");
        tags.put("workspace_linear_run_slope_delta", Integer.toString(linearRun.slopeDelta()));
        tags.put("workspace_opening_profile_id", linearRun.openingProfileId());
        applyFoundationTags(linearRun.foundationPolicy(), tags);
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, false, false).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, linearRun));
        return tags;
    }

    private MKWorkspaceRuntimePieceInfo runtimeInfoForRoom(MKTowerWorkspaceFamilyDefinition family) {
        boolean start = family.topologySlotId().equals("keep.center.entry");
        boolean terminal = family.topologySlotId().contains("top_cap") || family.topologySlotId().contains("basement_cap");
        MKJigsawPieceRole role = switch (family.pieceRole()) {
            case TOP_CAP -> MKJigsawPieceRole.TOP_CAP;
            case TOP_CAP_APPROACH -> MKJigsawPieceRole.TOP_CAP_APPROACH;
            case BASEMENT_CAP_APPROACH -> MKJigsawPieceRole.BASEMENT_CAP_APPROACH;
            case BASEMENT_CAP -> MKJigsawPieceRole.TERMINAL;
            default -> MKJigsawPieceRole.ROOM;
        };
        return new MKWorkspaceRuntimePieceInfo(start, role, 0, 0, true, true, terminal, false,
                family.category().getSerializedName(), false, false);
    }

    private Optional<ResolvedOpeningProfile> resolveOpeningProfile(MKStructureWorkspace workspace, String profileId) {
        return workspace.openingProfiles().stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()));
    }

    private void applyFoundationTags(MKWorkspaceFoundationPolicy policy, Map<String, String> tags) {
        if (policy.enabled()) {
            tags.put(MKWorkspaceFoundationPolicy.MODE_TAG, policy.mode().getSerializedName());
        }
    }

    private String verticalAccessDirectionTag(MKTowerWorkspaceFamilyDefinition family) {
        boolean up = family.hasVerticalAccess(Direction.UP);
        boolean down = family.hasVerticalAccess(Direction.DOWN);
        if (up && down) {
            return "both";
        }
        return up ? "up" : down ? "down" : "none";
    }

    private String verticalPool(String groupId, Direction direction) {
        return "vertical_access/" + groupId + "/" + direction.getSerializedName();
    }

    private String linearRunPool(String topologySlotId) {
        return "keep_linear_runs/" + topologySlotId.replace('.', '/');
    }

    private DirectionPair directionsForSlot(String topologySlotId) {
        if (topologySlotId.endsWith(".north") || topologySlotId.endsWith(".south")) {
            return new DirectionPair(Direction.WEST, Direction.EAST, true);
        }
        return new DirectionPair(Direction.NORTH, Direction.SOUTH, false);
    }

    private record DirectionPair(Direction negative, Direction positive, boolean eastWest) {
    }
}
