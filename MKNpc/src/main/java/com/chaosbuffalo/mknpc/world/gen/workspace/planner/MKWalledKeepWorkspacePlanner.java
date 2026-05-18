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
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MKWalledKeepWorkspacePlanner implements MKWorkspaceTopologyPlanner {
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String SLOT_POOL_PREFIX = "keep_slots/";
    private static final List<String> CONCRETE_CORNER_SLOTS = List.of(
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west"
    );
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
        Set<String> availableSlots = collectAvailableSlots(workspace);
        workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().startsWith("keep."))
                .map(family -> createRoomPiece(workspace, family, availableSlots))
                .forEach(pieces::add);
        workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().startsWith("keep."))
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun, availableSlots).stream())
                .forEach(pieces::add);
        return List.copyOf(pieces);
    }

    private Set<String> collectAvailableSlots(MKStructureWorkspace workspace) {
        LinkedHashSet<String> slots = new LinkedHashSet<>();
        workspace.familyDefinitions().stream()
                .map(MKTowerWorkspaceFamilyDefinition::topologySlotId)
                .filter(slot -> slot.startsWith("keep."))
                .forEach(slots::add);
        workspace.linearRunFamilies().stream()
                .map(MKWorkspaceLinearRunFamilyDefinition::topologySlotId)
                .filter(slot -> slot.startsWith("keep."))
                .forEach(slots::add);
        if (slots.contains("keep.corner.shared")) {
            slots.addAll(CONCRETE_CORNER_SLOTS);
        }
        return Set.copyOf(slots);
    }

    private MKPlannedPiece createRoomPiece(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family,
                                           Set<String> availableSlots) {
        int shaftSize = workspace.verticalAccessSpec().shaftSize();
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
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
        connectors.addAll(roomLayoutConnectors(family.topologySlotId(), availableSlots, opening));
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
                                                       MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                       Set<String> availableSlots) {
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
                linearRunLayoutConnectors(linearRun, directions, availableSlots, opening),
                buildLinearRunTags(workspace, linearRun)
        ));
    }

    private List<MKPlannedConnector> roomLayoutConnectors(String topologySlotId, Set<String> availableSlots,
                                                          ResolvedOpeningProfile opening) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        switch (topologySlotId) {
            case "keep.center.entry" -> {
                if (availableSlots.contains("keep.walkway.south")) {
                    connectors.add(new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                            opening.openingWidth(), opening.openingHeight(), slotPool("keep.walkway.south")));
                }
            }
            case "keep.gate.main" -> {
                connectors.add(new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH,
                        opening.openingWidth(), opening.openingHeight(), EMPTY_POOL, slotPool("keep.gate.main")));
                addBranchTarget(connectors, Direction.WEST, "keep.wall.south", availableSlots, opening);
                addBranchTarget(connectors, Direction.EAST, "keep.wall.south", availableSlots, opening);
            }
            case "keep.corner.shared" -> addSharedCornerConnectors(connectors, availableSlots, opening);
            case "keep.corner.north_west", "keep.corner.north_east", "keep.corner.south_east",
                 "keep.corner.south_west" -> addConcreteCornerConnectors(connectors, topologySlotId, availableSlots,
                    opening);
            default -> {
            }
        }
        return List.copyOf(connectors);
    }

    private List<MKPlannedConnector> linearRunLayoutConnectors(MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                               DirectionPair directions,
                                                               Set<String> availableSlots,
                                                               ResolvedOpeningProfile opening) {
        String slotId = linearRun.topologySlotId();
        int negativeOffset = Math.max(0, -linearRun.slopeDelta());
        int positiveOffset = Math.max(0, linearRun.slopeDelta());
        return switch (slotId) {
            case "keep.walkway.south" -> List.of(
                    new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH,
                            opening.openingWidth(), opening.openingHeight(), 0, negativeOffset,
                            EMPTY_POOL, slotPool(slotId)),
                    new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                            opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                            poolOrEmpty("keep.gate.main", availableSlots), EMPTY_POOL)
            );
            case "keep.wall.north" -> wallConnectors(slotId, directions, "keep.corner.north_west",
                    "keep.corner.north_east", availableSlots, opening, negativeOffset, positiveOffset);
            case "keep.wall.east" -> wallConnectors(slotId, directions, "keep.corner.north_east",
                    "keep.corner.south_east", availableSlots, opening, negativeOffset, positiveOffset);
            case "keep.wall.south" -> wallConnectors(slotId, directions, "keep.corner.south_west",
                    "keep.corner.south_east", availableSlots, opening, negativeOffset, positiveOffset);
            case "keep.wall.west" -> wallConnectors(slotId, directions, "keep.corner.north_west",
                    "keep.corner.south_west", availableSlots, opening, negativeOffset, positiveOffset);
            default -> List.of(
                    new MKPlannedConnector(MKConnectorRole.BRANCH, directions.negative(),
                            opening.openingWidth(), opening.openingHeight(), 0, negativeOffset,
                            slotPool(slotId), slotPool(slotId)),
                    new MKPlannedConnector(MKConnectorRole.BRANCH, directions.positive(),
                            opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                            slotPool(slotId), slotPool(slotId))
            );
        };
    }

    private List<MKPlannedConnector> wallConnectors(String wallSlotId, DirectionPair directions,
                                                   String negativeCornerSlotId, String positiveCornerSlotId,
                                                   Set<String> availableSlots, ResolvedOpeningProfile opening,
                                                   int negativeOffset, int positiveOffset) {
        return List.of(
                new MKPlannedConnector(MKConnectorRole.BRANCH, directions.negative(),
                        opening.openingWidth(), opening.openingHeight(), 0, negativeOffset,
                        poolOrEmpty(negativeCornerSlotId, availableSlots), slotPool(wallSlotId)),
                new MKPlannedConnector(MKConnectorRole.BRANCH, directions.positive(),
                        opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                        poolOrEmpty(positiveCornerSlotId, availableSlots), slotPool(wallSlotId))
        );
    }

    private void addSharedCornerConnectors(List<MKPlannedConnector> connectors, Set<String> availableSlots,
                                           ResolvedOpeningProfile opening) {
        addCornerEntryConnector(connectors, "keep.corner.north_west", Direction.EAST, "keep.wall.north",
                availableSlots, opening);
        addCornerEntryConnector(connectors, "keep.corner.north_east", Direction.WEST, "keep.wall.north",
                availableSlots, opening);
        addCornerEntryConnector(connectors, "keep.corner.south_east", Direction.WEST, "keep.wall.south",
                availableSlots, opening);
        addCornerEntryConnector(connectors, "keep.corner.south_west", Direction.EAST, "keep.wall.south",
                availableSlots, opening);
    }

    private void addConcreteCornerConnectors(List<MKPlannedConnector> connectors, String cornerSlotId,
                                             Set<String> availableSlots, ResolvedOpeningProfile opening) {
        switch (cornerSlotId) {
            case "keep.corner.north_west" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.EAST, "keep.wall.north",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.SOUTH, "keep.wall.west", availableSlots, opening);
            }
            case "keep.corner.north_east" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.WEST, "keep.wall.north",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.SOUTH, "keep.wall.east", availableSlots, opening);
            }
            case "keep.corner.south_east" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.WEST, "keep.wall.south",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.NORTH, "keep.wall.east", availableSlots, opening);
            }
            case "keep.corner.south_west" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.EAST, "keep.wall.south",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.NORTH, "keep.wall.west", availableSlots, opening);
            }
            default -> {
            }
        }
    }

    private void addCornerEntryConnector(List<MKPlannedConnector> connectors, String cornerSlotId, Direction facing,
                                         String targetWallSlotId, Set<String> availableSlots,
                                         ResolvedOpeningProfile opening) {
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                opening.openingWidth(), opening.openingHeight(), 0, 0,
                poolOrEmpty(targetWallSlotId, availableSlots), slotPool(cornerSlotId)));
    }

    private void addBranchTarget(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                 Set<String> availableSlots, ResolvedOpeningProfile opening) {
        if (availableSlots.contains(targetSlotId)) {
            connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                    opening.openingWidth(), opening.openingHeight(), slotPool(targetSlotId)));
        }
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

    private ResolvedOpeningProfile defaultOpeningProfile(MKStructureWorkspace workspace) {
        return workspace.openingProfiles().stream()
                .findFirst()
                .map(profile -> new ResolvedOpeningProfile(profile.profileId(), profile.openingWidth(), profile.openingHeight()))
                .orElse(new ResolvedOpeningProfile("default", 3, 3));
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

    private String slotPool(String topologySlotId) {
        return SLOT_POOL_PREFIX + topologySlotId.replace('.', '/');
    }

    private String poolOrEmpty(String topologySlotId, Set<String> availableSlots) {
        return availableSlots.contains(topologySlotId) ? slotPool(topologySlotId) : EMPTY_POOL;
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
