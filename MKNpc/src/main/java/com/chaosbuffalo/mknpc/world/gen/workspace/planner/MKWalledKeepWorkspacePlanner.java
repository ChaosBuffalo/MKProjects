package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceResolvedFamilySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVoidMarginTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWalledKeepCourtyardSettings;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MKWalledKeepWorkspacePlanner implements MKWorkspacePlanner {
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "walled_keep");
    private static final String EMPTY_POOL = "minecraft:empty";
    private static final String PERIMETER_ROOT_SLOT = "keep.perimeter";
    private static final String ENTRY_APPROACH_SLOT = "keep.entry_approach.main";
    private static final String WALKWAY_WEST_ROOT_SLOT = "keep.walkway.west";
    private static final String WALKWAY_EAST_ROOT_SLOT = "keep.walkway.east";
    private static final String COURTYARD_SLOT_PREFIX = "keep.courtyard.";
    private static final String COURTYARD_PATH_SLOT_PREFIX = "keep.courtyard.path.";
    private static final String COURTYARD_CONTENT_KIND = "courtyard";
    private static final String COURTYARD_PATH_KIND = "courtyard_path";
    public static final String CONTENT_KIND_TAG = "workspace_content_kind";
    public static final String CONTENT_SIZE_TAG = "workspace_content_size";
    public static final String CONTENT_WIDTH_TAG = "workspace_content_width";
    public static final String CONTENT_LENGTH_TAG = "workspace_content_length";
    public static final String CONTENT_HEIGHT_TAG = "workspace_content_height";
    public static final String CONTENT_REQUIRES_PATH_TAG = "workspace_content_requires_path";
    public static final String CONTENT_CONNECTOR_EDGE_TAG = "workspace_content_connector_edge";
    public static final String CONTENT_WALKWAY_CONTINUATION_LENGTH_TAG = "workspace_content_walkway_continuation_length";
    public static final String COURTYARD_SOCKET_ID_TAG = "workspace_courtyard_socket_id";
    public static final String COURTYARD_SOCKET_MAX_SIZE_TAG = "workspace_courtyard_socket_max_square_size";
    public static final String COURTYARD_DISABLED_REASON_TAG = "workspace_courtyard_disabled_reason";
    public static final String COURTYARD_AVAILABLE_HORIZONTAL_SPAN_TAG = "workspace_courtyard_available_horizontal_span";
    public static final String COURTYARD_AVAILABLE_VERTICAL_SPAN_TAG = "workspace_courtyard_available_vertical_span";
    public static final String COURTYARD_REQUESTED_MAX_SOCKET_SIZE_TAG = "workspace_courtyard_requested_max_socket_size";
    public static final String COURTYARD_PATH_SLOT_ID_TAG = "workspace_courtyard_path_slot_id";
    public static final String COURTYARD_PATH_SHAPE_TAG = "workspace_courtyard_path_shape";
    public static final String COURTYARD_PATH_LANE_INSET_TAG = "workspace_courtyard_path_lane_inset";
    private static final int DEFAULT_COURTYARD_CLEARANCE = 5;
    private static final String SLOT_POOL_PREFIX = "keep_slots/";
    private static final String COURTYARD_CONTENT_SOURCE = "keep_courtyard_content";
    private static final String COURTYARD_PATH_T_SOURCE = "keep_courtyard_path_t";
    private static final String COURTYARD_PATH_CORNER_T_SOURCE = "keep_courtyard_path_corner_t";
    private static final List<String> CONCRETE_CORNER_SLOTS = List.of(
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west"
    );
    private static final List<CourtyardSocketDefinition> COURTYARD_SOCKET_DEFINITIONS = List.of(
            new CourtyardSocketDefinition("north_west", Direction.SOUTH),
            new CourtyardSocketDefinition("north", Direction.SOUTH),
            new CourtyardSocketDefinition("north_east", Direction.SOUTH),
            new CourtyardSocketDefinition("west", Direction.EAST),
            new CourtyardSocketDefinition("east", Direction.WEST),
            new CourtyardSocketDefinition("south_west", Direction.EAST),
            new CourtyardSocketDefinition("south_east", Direction.WEST)
    );
    private static final List<CourtyardPathDefinition> COURTYARD_PATH_DEFINITIONS = List.of(
            new CourtyardPathDefinition("south_west", "corner_t", COURTYARD_PATH_CORNER_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270, Direction.EAST, Direction.NORTH,
                    "keep.courtyard.path.west", Direction.WEST, "keep.courtyard.south_west"),
            new CourtyardPathDefinition("west", "t", COURTYARD_PATH_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_NONE, Direction.SOUTH, Direction.NORTH,
                    "keep.courtyard.path.north_west", Direction.WEST, "keep.courtyard.west"),
            new CourtyardPathDefinition("north_west", "corner_t", COURTYARD_PATH_CORNER_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_NONE, Direction.SOUTH, Direction.EAST,
                    "keep.courtyard.path.north", Direction.NORTH, "keep.courtyard.north_west"),
            new CourtyardPathDefinition("north", "t", COURTYARD_PATH_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90, Direction.WEST, Direction.EAST,
                    "keep.courtyard.path.north_east", Direction.NORTH, "keep.courtyard.north"),
            new CourtyardPathDefinition("north_east", "corner_t", COURTYARD_PATH_CORNER_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180, Direction.WEST, Direction.SOUTH,
                    "keep.courtyard.path.east", Direction.NORTH, "keep.courtyard.north_east"),
            new CourtyardPathDefinition("east", "t", COURTYARD_PATH_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180, Direction.NORTH, Direction.SOUTH,
                    "keep.courtyard.path.south_east", Direction.EAST, "keep.courtyard.east"),
            new CourtyardPathDefinition("south_east", "corner_t", COURTYARD_PATH_CORNER_T_SOURCE,
                    MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270, Direction.NORTH, Direction.WEST,
                    null, Direction.EAST, "keep.courtyard.south_east")
    );
    private static final Set<String> KNOWN_KEEP_SLOTS = Set.of(
            "keep.center.entry",
            "keep.center.main_floor",
            "keep.center.top_cap",
            "keep.center.basement_floor",
            "keep.center.basement_cap",
            "keep.corner.shared",
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west",
            PERIMETER_ROOT_SLOT,
            "keep.perimeter.north",
            "keep.perimeter.east",
            "keep.perimeter.south",
            "keep.perimeter.west",
            "keep.walkway.north",
            WALKWAY_EAST_ROOT_SLOT,
            "keep.walkway.south",
            WALKWAY_WEST_ROOT_SLOT,
            ENTRY_APPROACH_SLOT,
            "keep.gate.main"
    );
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();
    private final MKWorkspaceVerticalStackPlanner towerStackPlanner = new MKWorkspaceVerticalStackPlanner();
    private final MKFloorTopologyPlanner floorTopologyPlanner = new MKFloorTopologyPlanner();

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
    }

    private record IngressConnection(ResolvedOpeningProfile opening, int lateralOffset, int verticalOffset) {
    }

    private record PerimeterSegment(String chainId,
                                    String side,
                                    int index,
                                    int count,
                                    String slotId,
                                    String pieceName,
                                    boolean eastWest,
                                    Direction incomingFacing,
                                    Direction outgoingFacing,
                                    String outgoingTargetSlotId,
                                    boolean terminal,
                                    MKWorkspaceLinearRunFamilyDefinition family) {
    }

    private record PerimeterPlan(List<PerimeterSegment> southWest,
                                 List<PerimeterSegment> west,
                                 List<PerimeterSegment> northWest,
                                 List<PerimeterSegment> southEast,
                                 List<PerimeterSegment> east,
                                 List<PerimeterSegment> northEast) {
        private List<PerimeterSegment> allSegments() {
            ArrayList<PerimeterSegment> segments = new ArrayList<>();
            segments.addAll(southWest);
            segments.addAll(west);
            segments.addAll(northWest);
            segments.addAll(southEast);
            segments.addAll(east);
            segments.addAll(northEast);
            return List.copyOf(segments);
        }

        private Optional<PerimeterSegment> firstSouthWest() {
            return first(southWest);
        }

        private Optional<PerimeterSegment> firstWest() {
            return first(west);
        }

        private Optional<PerimeterSegment> firstNorthWest() {
            return first(northWest);
        }

        private Optional<PerimeterSegment> firstEast() {
            return first(east);
        }

        private Optional<PerimeterSegment> firstSouthEast() {
            return first(southEast);
        }

        private Optional<PerimeterSegment> firstNorthEast() {
            return first(northEast);
        }

        private Set<String> slotIds() {
            return allSegments().stream()
                    .map(PerimeterSegment::slotId)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }

        private static Optional<PerimeterSegment> first(List<PerimeterSegment> segments) {
            return segments.stream().findFirst();
        }
    }

    private record SlotAvailability(Set<String> availableSlots, Set<String> sharedCornerSlots,
                                    PerimeterPlan perimeterPlan, CourtyardPlan courtyardPlan) {
    }

    private record CourtyardSocketDefinition(String suffix, Direction contentConnectorFacing) {
        private String slotId() {
            return COURTYARD_SLOT_PREFIX + suffix;
        }
    }

    private record CourtyardPathDefinition(String suffix,
                                           String shape,
                                           String sourcePieceName,
                                           String rotation,
                                           Direction incomingFacing,
                                           Direction outgoingFacing,
                                           String outgoingTargetSlotId,
                                           Direction contentFacing,
                                           String contentSlotId) {
        private String slotId() {
            return COURTYARD_PATH_SLOT_PREFIX + suffix;
        }

        private String pieceName() {
            return sourcePieceName + "_" + suffix;
        }
    }

    private record CourtyardSocket(String slotId, int maxSquareSize,
                                   Direction contentConnectorFacing, boolean enabled) {
    }

    private record CourtyardPlan(List<CourtyardSocket> sockets, List<CourtyardPathDefinition> paths,
                                 Optional<String> disabledReason, int availableHorizontalSpan,
                                 int availableVerticalSpan, int requestedMaxSocketSize) {
        private static CourtyardPlan empty() {
            return new CourtyardPlan(List.of(), List.of(), Optional.empty(), 0, 0, 0);
        }

        private static CourtyardPlan disabled(String reason, int availableHorizontalSpan, int availableVerticalSpan,
                                              int requestedMaxSocketSize) {
            return new CourtyardPlan(List.of(), List.of(), Optional.of(reason), availableHorizontalSpan,
                    availableVerticalSpan, requestedMaxSocketSize);
        }

        private Set<String> slotIds() {
            LinkedHashSet<String> slotIds = sockets.stream()
                    .filter(CourtyardSocket::enabled)
                    .map(CourtyardSocket::slotId)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            paths.stream()
                    .map(CourtyardPathDefinition::slotId)
                    .forEach(slotIds::add);
            return slotIds;
        }
    }

    @Override
    public net.minecraft.resources.ResourceLocation plannerId() {
        return PLANNER_ID;
    }

    public static MKWorkspaceTopologyProfile defaultTopologyProfile(boolean uniqueCornerTowers) {
        return MKWorkspaceTopologyProfile.walledKeep(uniqueCornerTowers);
    }

    public static MKWorkspaceTopologyProfile defaultTopologyProfile(boolean uniqueNorthWestCornerTower,
                                                                    boolean uniqueNorthEastCornerTower,
                                                                    boolean uniqueSouthEastCornerTower,
                                                                    boolean uniqueSouthWestCornerTower) {
        return MKWorkspaceTopologyProfile.walledKeep(uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                uniqueSouthEastCornerTower, uniqueSouthWestCornerTower);
    }

    @Override
    public MKWorkspaceTopologySchema schema() {
        return new MKWorkspaceTopologySchema(
                plannerId(),
                List.of(
                        new MKWorkspaceRegionSchema("keep.center_tower", "tower_stack", true),
                        new MKWorkspaceRegionSchema("keep.corner_towers", "tower_stack", true),
                        new MKWorkspaceRegionSchema("keep.perimeter_runs", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.walkways", "linear_run", true),
                        new MKWorkspaceRegionSchema("keep.gates", "entry", false),
                        new MKWorkspaceRegionSchema("keep.courtyard", "open_area", false)
                ),
                walledKeepSlots(),
                List.of(
                        new MKWorkspaceLinkSchema("keep.center.vertical", "keep.center.basement_cap", "keep.center.top_cap", "vertical_access_group:keep.center"),
                        new MKWorkspaceLinkSchema("keep.corner.north_west.vertical", "keep.corner.north_west.basement_cap", "keep.corner.north_west.top_cap", "vertical_access_group:keep.corner.north_west"),
                        new MKWorkspaceLinkSchema("keep.corner.north_east.vertical", "keep.corner.north_east.basement_cap", "keep.corner.north_east.top_cap", "vertical_access_group:keep.corner.north_east"),
                        new MKWorkspaceLinkSchema("keep.corner.south_east.vertical", "keep.corner.south_east.basement_cap", "keep.corner.south_east.top_cap", "vertical_access_group:keep.corner.south_east"),
                        new MKWorkspaceLinkSchema("keep.corner.south_west.vertical", "keep.corner.south_west.basement_cap", "keep.corner.south_west.top_cap", "vertical_access_group:keep.corner.south_west"),
                        new MKWorkspaceLinkSchema("keep.perimeter.west_branch", "keep.gate.main", "keep.perimeter.north_west", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.perimeter.east_branch", "keep.gate.main", "keep.perimeter.north_east", "linear_run")
                ),
                walledKeepRoles()
        );
    }

    private static List<MKWorkspaceSlotSchema> walledKeepSlots() {
        ArrayList<MKWorkspaceSlotSchema> slots = new ArrayList<>();
        addTowerStackSlots(slots, "keep.center", "keep.center_tower");
        addTowerStackSlots(slots, "keep.corner.shared", "keep.corner_towers");
        for (String cornerSlot : CONCRETE_CORNER_SLOTS) {
            addTowerStackSlots(slots, cornerSlot, "keep.corner_towers");
        }
        slots.add(new MKWorkspaceSlotSchema(PERIMETER_ROOT_SLOT, "keep.perimeter_runs", "defensive_run",
                PERIMETER_ROOT_SLOT, MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.walkway.north", "keep.walkways", "open_walkway",
                "keep.walkway.north", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema(WALKWAY_EAST_ROOT_SLOT, "keep.walkways", "open_walkway",
                WALKWAY_EAST_ROOT_SLOT, MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.walkway.south", "keep.walkways", "open_walkway",
                "keep.walkway.south", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema(WALKWAY_WEST_ROOT_SLOT, "keep.walkways", "open_walkway",
                WALKWAY_WEST_ROOT_SLOT, MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema(ENTRY_APPROACH_SLOT, "keep.walkways", "entry_approach",
                ENTRY_APPROACH_SLOT, MKWorkspaceSlotSchema.Repeat.FIXED));
        for (CourtyardPathDefinition path : COURTYARD_PATH_DEFINITIONS) {
            slots.add(new MKWorkspaceSlotSchema(path.slotId(), "keep.courtyard", "path",
                    path.slotId(), MKWorkspaceSlotSchema.Repeat.OPTIONAL));
        }
        for (CourtyardSocketDefinition socket : COURTYARD_SOCKET_DEFINITIONS) {
            slots.add(new MKWorkspaceSlotSchema(socket.slotId(), "keep.courtyard", "content_socket",
                    socket.slotId(), MKWorkspaceSlotSchema.Repeat.OPTIONAL));
        }
        slots.add(new MKWorkspaceSlotSchema("keep.gate.main", "keep.gates", "entry", "keep.gate.main",
                MKWorkspaceSlotSchema.Repeat.OPTIONAL));
        return List.copyOf(slots);
    }

    private static void addTowerStackSlots(List<MKWorkspaceSlotSchema> slots, String stackId, String regionId) {
        for (MKWorkspaceVerticalStackSlot slot : MKWorkspaceVerticalStackSlot.schemaOrder()) {
            slots.add(new MKWorkspaceSlotSchema(slot.slotId(stackId), regionId, slot.roleKind(),
                    slot.slotId(stackId), repeatForStackSlot(slot)));
        }
    }

    private static MKWorkspaceSlotSchema.Repeat repeatForStackSlot(MKWorkspaceVerticalStackSlot slot) {
        return switch (slot) {
            case MAIN_FLOOR, BASEMENT_FLOOR -> MKWorkspaceSlotSchema.Repeat.RANGE;
            case TOP_CAP_APPROACH, BASEMENT_CAP_APPROACH -> MKWorkspaceSlotSchema.Repeat.OPTIONAL;
            default -> MKWorkspaceSlotSchema.Repeat.FIXED;
        };
    }

    private static List<MKWorkspaceRoleSchema> walledKeepRoles() {
        ArrayList<MKWorkspaceRoleSchema> roles = new ArrayList<>();
        addTowerStackRoles(roles, "keep.center", Set.of("center_tower"));
        addTowerStackRoles(roles, "keep.corner.shared", Set.of("corner_tower", "shared_corner_template"));
        for (String cornerSlot : CONCRETE_CORNER_SLOTS) {
            addTowerStackRoles(roles, cornerSlot, Set.of("corner_tower", "unique_corner_template"));
        }
        roles.add(new MKWorkspaceRoleSchema(PERIMETER_ROOT_SLOT, "linear_run", "defensive_run",
                false, false, Set.of("defensive_wall", "solid_wall", "parapet")));
        roles.add(new MKWorkspaceRoleSchema("keep.walkway.north", "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema(WALKWAY_EAST_ROOT_SLOT, "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema("keep.walkway.south", "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema(WALKWAY_WEST_ROOT_SLOT, "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema(ENTRY_APPROACH_SLOT, "linear_run", "entry_approach",
                false, false, Set.of("open_walkway", "entry_approach", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema("keep.courtyard.path", "linear_run", "courtyard_path",
                false, false, Set.of("open_walkway", "courtyard_path", "strict_socket")));
        roles.add(new MKWorkspaceRoleSchema("keep.courtyard.content", "content", "courtyard",
                false, false, Set.of("courtyard_content", "strict_socket")));
        roles.add(new MKWorkspaceRoleSchema("keep.gate.main", "entry", "room",
                false, true, Set.of("gate")));
        return List.copyOf(roles);
    }

    private static void addTowerStackRoles(List<MKWorkspaceRoleSchema> roles, String stackId, Set<String> extraTags) {
        for (MKWorkspaceVerticalStackSlot slot : MKWorkspaceVerticalStackSlot.schemaOrder()) {
            roles.add(towerStackRole(slot.slotId(stackId), slot.roleKind(), slot.pieceKind(), slot.terminal(),
                    slot == MKWorkspaceVerticalStackSlot.ENTRY && stackId.equals("keep.center"), extraTags));
        }
    }

    private static MKWorkspaceRoleSchema towerStackRole(String roleId, String roleKind, String pieceKind,
                                                        boolean terminal, boolean start, Set<String> extraTags) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("vertical_access");
        tags.addAll(extraTags);
        return new MKWorkspaceRoleSchema(roleId, roleKind, pieceKind, terminal, start, Set.copyOf(tags));
    }

    @Override
    public List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        PerimeterPlan perimeterPlan = createPerimeterPlan(workspace);
        CourtyardPlan courtyardPlan = createCourtyardPlan(workspace, perimeterPlan);
        SlotAvailability slots = collectAvailableSlots(workspace, perimeterPlan, courtyardPlan);
        pieces.addAll(createCenterStackPieces(workspace, slots));
        pieces.addAll(createCornerStackPieces(workspace, slots));
        workspace.familyDefinitions().stream()
                .filter(family -> isActiveKeepSlot(workspace, family.topologySlotId()))
                .filter(family -> !isCenterStackSlot(family.topologySlotId()))
                .filter(family -> !isCornerStackSlot(family.topologySlotId()))
                .map(family -> createRoomPiece(workspace, family, slots))
                .forEach(pieces::add);
        workspace.linearRunFamilies().stream()
                .filter(linearRun -> isKnownKeepSlot(linearRun.topologySlotId()))
                .filter(linearRun -> !isPerimeterRunFamily(linearRun))
                .filter(linearRun -> !isCourtyardWalkwayRootFamily(linearRun))
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun, slots).stream())
                .forEach(pieces::add);
        pieces.addAll(createCourtyardPathPieces(workspace, courtyardPlan, slots.availableSlots()));
        pieces.addAll(createPerimeterPieces(workspace, perimeterPlan));
        pieces.addAll(createCourtyardContentPieces(workspace, courtyardPlan));
        pieces.addAll(floorTopologyPlanner.createFloorTopologyPieces(workspace,
                activeTowerStackFamiliesForFloorTopology(workspace)));
        return List.copyOf(pieces);
    }

    private List<MKWorkspaceRoomFamilyDefinition> activeTowerStackFamiliesForFloorTopology(MKStructureWorkspace workspace) {
        return workspace.familyDefinitions().stream()
                .filter(family -> isActiveKeepSlot(workspace, family.topologySlotId()))
                .filter(family -> isCenterStackSlot(family.topologySlotId()) || isCornerStackSlot(family.topologySlotId()))
                .toList();
    }

    private List<MKPlannedPiece> createCenterStackPieces(MKStructureWorkspace workspace, SlotAvailability slots) {
        List<MKWorkspaceRoomFamilyDefinition> centerFamilies = workspace.familyDefinitions().stream()
                .filter(family -> isActiveKeepSlot(workspace, family.topologySlotId()))
                .filter(family -> isCenterStackSlot(family.topologySlotId()))
                .toList();
        MKWorkspaceVerticalStackSettings settings = workspace.topologyProfile().verticalStackSettingsOrDefault("keep.center");
        MKWorkspaceVerticalStackDefinition stackDefinition = MKWorkspaceVerticalStackDefinition.scoped("keep.center", true, settings);
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
        return towerStackPlanner.createRoomPieces(workspace, stackDefinition, centerFamilies).stream()
                .map(piece -> withRoomLayoutConnectors(workspace, piece, slots, opening))
                .toList();
    }

    private List<MKPlannedPiece> createCornerStackPieces(MKStructureWorkspace workspace, SlotAvailability slots) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        List<MKWorkspaceRoomFamilyDefinition> sharedFamilies = workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().startsWith("keep.corner.shared."))
                .toList();
        MKWorkspaceVerticalStackSettings sharedCornerSettings = normalizeSharedCornerSettings(
                workspace.topologyProfile().verticalStackSettingsOrDefault("keep.corner.shared"));
        for (String stackId : CONCRETE_CORNER_SLOTS) {
            List<MKWorkspaceRoomFamilyDefinition> stackFamilies;
            if (workspace.topologyProfile().uniqueCornerTower(stackId)) {
                stackFamilies = workspace.familyDefinitions().stream()
                        .filter(family -> family.topologySlotId().startsWith(stackId + "."))
                        .toList();
            } else {
                stackFamilies = sharedFamilies.stream()
                        .map(family -> remapSharedCornerFamily(family, stackId, sharedCornerSettings))
                        .toList();
            }
            addCornerStackPieces(pieces, workspace, slots, stackId, stackFamilies);
        }
        return List.copyOf(pieces);
    }

    private void addCornerStackPieces(List<MKPlannedPiece> pieces, MKStructureWorkspace workspace,
                                      SlotAvailability slots, String stackId,
                                      List<MKWorkspaceRoomFamilyDefinition> stackFamilies) {
        if (stackFamilies.isEmpty()) {
            return;
        }
        boolean uniqueCorner = workspace.topologyProfile().uniqueCornerTower(stackId);
        MKWorkspaceVerticalStackSettings settings = workspace.topologyProfile().verticalStackSettingsOrDefault(
                uniqueCorner ? stackId : "keep.corner.shared");
        if (!uniqueCorner) {
            settings = normalizeSharedCornerSettings(settings);
        }
        MKWorkspaceVerticalStackDefinition stackDefinition = MKWorkspaceVerticalStackDefinition.scoped(stackId, false, settings);
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
        towerStackPlanner.createRoomPieces(workspace, stackDefinition, stackFamilies).stream()
                .map(piece -> withRoomLayoutConnectors(workspace, piece, slots, opening))
                .map(piece -> uniqueCorner ? piece : withSharedCornerTemplateReuse(piece, stackId))
                .forEach(pieces::add);
    }

    private MKWorkspaceRoomFamilyDefinition remapSharedCornerFamily(MKWorkspaceRoomFamilyDefinition family,
                                                                     String targetStackId,
                                                                     MKWorkspaceVerticalStackSettings sharedSettings) {
        String targetBasePrefix = targetStackId.replace('.', '_');
        String baseName = family.baseName().replace("keep_corner_shared", targetBasePrefix);
        String topologySlotId = family.topologySlotId().replace("keep.corner.shared", targetStackId);
        int normalizedRoomWidth = normalizeSharedCornerFamilyDimension(family.roomWidth(), family.roomLength(),
                sharedSettings.width());
        int normalizedRoomLength = normalizedRoomWidth;
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                baseName,
                topologySlotId,
                targetStackId,
                family.supportsVerticalAccess(),
                normalizedRoomWidth,
                normalizedRoomLength,
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private MKWorkspaceVerticalStackSettings normalizeSharedCornerSettings(MKWorkspaceVerticalStackSettings settings) {
        int size = Math.max(settings.width(), settings.length());
        return new MKWorkspaceVerticalStackSettings(
                settings.stackId(),
                settings.minMainFloors(),
                settings.mainFloors(),
                settings.minBasementFloors(),
                settings.basementFloors(),
                settings.heights(),
                size,
                size,
                settings.shaftSize(),
                settings.verticalAccessPlacement(),
                settings.stairConfig(),
                settings.topCapApproachEnabled(),
                settings.basementEntryEnabled(),
                settings.basementCapApproachEnabled(),
                settings.horizontalExtrusionMode(),
                settings.foundationPolicy(),
                settings.paletteOverride()
        );
    }

    private int normalizeSharedCornerFamilyDimension(int width, int length, int fallbackSize) {
        if (width <= 0 && length <= 0) {
            return fallbackSize;
        }
        return Math.max(width, length);
    }

    private MKPlannedPiece withSharedCornerTemplateReuse(MKPlannedPiece piece, String stackId) {
        String sourceId = piece.pieceName().replace(stackId.replace('.', '_'), "keep_corner_north_west");
        return withTemplateReuse(piece, sourceId, rotationForCornerStack(stackId),
                "keep.corner.north_west".equals(stackId));
    }

    private String rotationForCornerStack(String stackId) {
        return switch (stackId) {
            case "keep.corner.north_east" -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90;
            case "keep.corner.south_east" -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180;
            case "keep.corner.south_west" -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270;
            default -> MKWorkspaceTemplateReuseTags.ROTATION_NONE;
        };
    }

    private MKPlannedPiece withRoomLayoutConnectors(MKStructureWorkspace workspace, MKPlannedPiece piece,
                                                    SlotAvailability slots,
                                                    ResolvedOpeningProfile opening) {
        String topologySlotId = piece.tags().getOrDefault("workspace_topology_slot_id", "");
        List<MKPlannedConnector> layoutConnectors = roomLayoutConnectors(workspace, topologySlotId, slots, opening);
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>(piece.connectors());
        connectors.addAll(layoutConnectors);
        return new MKPlannedPiece(
                piece.roleId(),
                piece.pieceName(),
                piece.interiorWidth(),
                piece.interiorLength(),
                piece.interiorHeight(),
                List.copyOf(connectors),
                keepRoomRuntimeTags(workspace, piece.tags())
        );
    }

    private Map<String, String> keepRoomRuntimeTags(MKStructureWorkspace workspace, Map<String, String> sourceTags) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(sourceTags);
        effectiveRoomExtrusionMode(workspace, tags.getOrDefault("workspace_topology_slot_id", ""))
                .ifPresent(mode -> tags.put("workspace_horizontal_extrusion_mode", mode.getSerializedName()));
        MKWorkspaceRuntimePieceInfo.fromTags(sourceTags)
                .map(info -> new MKWorkspaceRuntimePieceInfo(
                        info.start(),
                        info.role(),
                        info.progressionDelta(),
                        info.verticalLevelDelta(),
                        info.allowOnMainPath(),
                        true,
                        info.terminal(),
                        info.topCapOnly(),
                        info.topologyGroup(),
                        info.mainPathEnding(),
                        info.branchCap()
                ))
                .ifPresent(info -> info.applyToTags(tags));
        return tags;
    }

    private SlotAvailability collectAvailableSlots(MKStructureWorkspace workspace, PerimeterPlan perimeterPlan,
                                                   CourtyardPlan courtyardPlan) {
        LinkedHashSet<String> slots = new LinkedHashSet<>();
        workspace.familyDefinitions().stream()
                .map(MKWorkspaceRoomFamilyDefinition::topologySlotId)
                .filter(MKWalledKeepWorkspacePlanner::isKnownKeepSlot)
                .filter(slot -> isActiveKeepSlot(workspace, slot))
                .forEach(slot -> addAvailableSlot(slots, slot));
        workspace.linearRunFamilies().stream()
                .map(MKWorkspaceLinearRunFamilyDefinition::topologySlotId)
                .filter(MKWalledKeepWorkspacePlanner::isKnownKeepSlot)
                .forEach(slot -> addAvailableLinearRunSlot(slots, slot));
        slots.addAll(perimeterPlan.slotIds());
        slots.addAll(courtyardPlan.slotIds());
        if (!courtyardPlan.slotIds().isEmpty()) {
            slots.add(ENTRY_APPROACH_SLOT);
        }
        LinkedHashSet<String> sharedCornerSlots = new LinkedHashSet<>();
        if (slots.contains("keep.corner.shared")) {
            CONCRETE_CORNER_SLOTS.stream()
                    .filter(slot -> !workspace.topologyProfile().uniqueCornerTower(slot))
                    .filter(slot -> !slots.contains(slot))
                    .forEach(sharedCornerSlots::add);
            slots.addAll(sharedCornerSlots);
        }
        return new SlotAvailability(Set.copyOf(slots), Set.copyOf(sharedCornerSlots), perimeterPlan, courtyardPlan);
    }

    private static void addAvailableSlot(Set<String> slots, String topologySlotId) {
        slots.add(topologySlotId);
        cornerStackIdForSlot(topologySlotId).ifPresent(slots::add);
    }

    private static void addAvailableLinearRunSlot(Set<String> slots, String topologySlotId) {
        slots.add(topologySlotId);
    }

    private MKPlannedPiece createRoomPiece(MKStructureWorkspace workspace, MKWorkspaceRoomFamilyDefinition family,
                                           SlotAvailability slots) {
        MKWorkspaceResolvedFamilySettings resolvedFamily = workspace.resolveFamilySettings(family);
        int shaftSize = resolvedFamily.verticalAccessSpec().shaftSize();
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
        connectors.addAll(roomLayoutConnectors(workspace, family.topologySlotId(), slots, opening));
        return new MKPlannedPiece(
                resolvedFamily.slotMetadata().topologySlotId(),
                family.baseName(),
                resolvedFamily.roomWidth(),
                resolvedFamily.roomLength(),
                resolvedFamily.roomHeight(),
                connectors,
                buildRoomTags(workspace, family)
        );
    }

    private List<MKPlannedPiece> createLinearRunPieces(MKStructureWorkspace workspace,
                                                       MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                       SlotAvailability slots) {
        if (!linearRun.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
            return List.of();
        }
        ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, linearRun.openingProfileId())
                .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " +
                        linearRun.openingProfileId()));
        DirectionPair directions = directionsForSlot(linearRun.topologySlotId());
        int effectiveLength = effectiveLinearRunLength(workspace, linearRun, slots.courtyardPlan(), opening);
        MKPlannedPiece piece = new MKPlannedPiece(
                linearRun.topologySlotId(),
                linearRun.linearRunId(),
                directions.eastWest() ? effectiveLength : linearRun.interiorWidth(),
                directions.eastWest() ? linearRun.interiorWidth() : effectiveLength,
                linearRun.interiorHeight() + Math.abs(linearRun.slopeDelta()),
                linearRunLayoutConnectors(workspace, linearRun, directions, slots.availableSlots(), opening,
                        effectiveLength),
                buildLinearRunTags(workspace, linearRun)
        );
        if (ENTRY_APPROACH_SLOT.equals(linearRun.topologySlotId())) {
            piece = withCourtyardDisabledDiagnostics(piece, slots.courtyardPlan());
        }
        return List.of(piece);
    }

    private List<MKPlannedPiece> createCourtyardPathPieces(MKStructureWorkspace workspace,
                                                           CourtyardPlan courtyardPlan,
                                                           Set<String> availableSlots) {
        if (courtyardPlan.paths().isEmpty()) {
            return List.of();
        }
        MKWorkspaceLinearRunFamilyDefinition family = courtyardPathFamily(workspace);
        ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, family.openingProfileId())
                .orElseGet(() -> defaultOpeningProfile(workspace));
        int laneInset = courtyardPathLaneCenterInset(workspace, opening);
        int pathSize = courtyardPathSize(workspace, family, laneInset);
        int height = family.interiorHeight() + Math.abs(family.slopeDelta());
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        pieces.add(createCourtyardPathSourcePiece(workspace, family, COURTYARD_PATH_T_SOURCE, "t",
                pathSize, height, laneInset, opening));
        pieces.add(createCourtyardPathSourcePiece(workspace, family, COURTYARD_PATH_CORNER_T_SOURCE, "corner_t",
                pathSize, height, laneInset, opening));
        for (CourtyardPathDefinition path : courtyardPlan.paths()) {
            pieces.add(createCourtyardPathRuntimePiece(workspace, family, path, pathSize, height, laneInset,
                    opening, availableSlots));
        }
        return List.copyOf(pieces);
    }

    private MKWorkspaceLinearRunFamilyDefinition courtyardPathFamily(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(WALKWAY_WEST_ROOT_SLOT))
                .findFirst()
                .or(() -> workspace.linearRunFamilies().stream()
                        .filter(linearRun -> linearRun.topologySlotId().equals(WALKWAY_EAST_ROOT_SLOT))
                        .findFirst())
                .orElseGet(() -> new MKWorkspaceLinearRunFamilyDefinition(
                        COURTYARD_PATH_T_SOURCE,
                        "keep.courtyard.path",
                        MKWorkspaceLinearRunKind.OPEN_WALKWAY,
                        "branch_opening",
                        9,
                        3,
                        7,
                        0,
                        false,
                        true,
                        MKWorkspaceLinearRunProjection.RIGID,
                        List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT),
                        MKWorkspaceFoundationPolicy.none(),
                        null
                ));
    }

    private Optional<MKWorkspaceLinearRunFamilyDefinition> entryApproachFamily(MKStructureWorkspace workspace) {
        return workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(ENTRY_APPROACH_SLOT))
                .findFirst();
    }

    private MKPlannedPiece createCourtyardPathSourcePiece(MKStructureWorkspace workspace,
                                                          MKWorkspaceLinearRunFamilyDefinition family,
                                                          String pieceName, String shape, int pathSize, int height,
                                                          int laneInset, ResolvedOpeningProfile opening) {
        MKPlannedPiece piece = new MKPlannedPiece(
                "keep.courtyard.path",
                pieceName,
                pathSize,
                pathSize,
                height,
                sourceCourtyardPathConnectors(shape, opening),
                buildCourtyardPathTags(workspace, family, "keep.courtyard.path." + shape, shape, laneInset)
        );
        return withTemplateReuse(piece, pieceName, MKWorkspaceTemplateReuseTags.ROTATION_NONE, true);
    }

    private MKPlannedPiece createCourtyardPathRuntimePiece(MKStructureWorkspace workspace,
                                                           MKWorkspaceLinearRunFamilyDefinition family,
                                                           CourtyardPathDefinition path, int pathSize, int height,
                                                           int laneInset, ResolvedOpeningProfile opening,
                                                           Set<String> availableSlots) {
        MKPlannedPiece piece = new MKPlannedPiece(
                path.slotId(),
                path.pieceName(),
                pathSize,
                pathSize,
                height,
                courtyardPathConnectors(workspace, path, pathSize, availableSlots, opening),
                buildCourtyardPathTags(workspace, family, path.slotId(), path.shape(), laneInset)
        );
        return withExportCrop(withTemplateReuse(piece, path.sourcePieceName(), path.rotation(), false));
    }

    private int courtyardPathLaneCenterInset(MKStructureWorkspace workspace, ResolvedOpeningProfile opening) {
        return workspace.shellMargin() + workspace.exteriorAirMargin() +
                workspace.topologyProfile().courtyardSettings().courtyardPathInnerMargin() +
                opening.openingWidth() / 2;
    }

    private int courtyardPathSize(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition family,
                                  int laneInset) {
        int centerSpan = Math.max(centerWidth(workspace), centerLength(workspace));
        int calculated = centerSpan + (2 * laneInset);
        return smallestOddAtLeast(Math.max(calculated, Math.max(family.length(), family.interiorWidth())));
    }

    private int effectiveLinearRunLength(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition linearRun,
                                         CourtyardPlan courtyardPlan, ResolvedOpeningProfile opening) {
        if (!ENTRY_APPROACH_SLOT.equals(linearRun.topologySlotId()) || courtyardPlan.paths().isEmpty()) {
            return linearRun.length();
        }
        return effectiveEntryApproachLength(workspace, linearRun.length(), opening);
    }

    private int effectiveEntryApproachLength(MKStructureWorkspace workspace, int requestedLength,
                                             ResolvedOpeningProfile entryOpening) {
        MKWorkspaceLinearRunFamilyDefinition pathFamily = courtyardPathFamily(workspace);
        ResolvedOpeningProfile pathOpening = resolveOpeningProfile(workspace, pathFamily.openingProfileId())
                .orElseGet(() -> defaultOpeningProfile(workspace));
        int laneInset = courtyardPathLaneCenterInset(workspace, pathOpening);
        int pathSize = courtyardPathSize(workspace, pathFamily, laneInset);
        return smallestOddAtLeast(Math.max(requestedLength,
                pathSize + entryApproachGatehouseClearance(workspace, entryOpening, pathOpening)));
    }

    private int entryApproachGatehouseClearance(MKStructureWorkspace workspace, ResolvedOpeningProfile entryOpening,
                                                ResolvedOpeningProfile pathOpening) {
        return workspace.shellMargin() + workspace.exteriorAirMargin() +
                Math.max(entryOpening.openingWidth(), pathOpening.openingWidth());
    }

    private PerimeterPlan createPerimeterPlan(MKStructureWorkspace workspace) {
        Optional<MKWorkspaceLinearRunFamilyDefinition> south = perimeterFamilyForSide(workspace, "keep.perimeter.south");
        Optional<MKWorkspaceLinearRunFamilyDefinition> west = perimeterFamilyForSide(workspace, "keep.perimeter.west");
        Optional<MKWorkspaceLinearRunFamilyDefinition> north = perimeterFamilyForSide(workspace, "keep.perimeter.north");
        Optional<MKWorkspaceLinearRunFamilyDefinition> east = perimeterFamilyForSide(workspace, "keep.perimeter.east");
        int horizontalSegments = south.map(family -> segmentCountForSpan(family, horizontalPerimeterSpan(workspace)))
                .or(() -> north.map(family -> segmentCountForSpan(family, horizontalPerimeterSpan(workspace))))
                .orElse(0);
        int verticalSegments = west.map(family -> verticalSegmentCountForSpan(workspace, family,
                        verticalPerimeterSpan(workspace)))
                .or(() -> east.map(family -> verticalSegmentCountForSpan(workspace, family,
                        verticalPerimeterSpan(workspace))))
                .orElse(0);
        int frontBranchSegments = horizontalSegments > 0 ?
                Math.max(1, (int) Math.ceil(horizontalSegments / 2.0)) : 0;
        int backWallSegments = backWallSegmentCount(south.isPresent(), north.isPresent(), horizontalSegments,
                frontBranchSegments);
        int northWestSegments = backWallSegments > 0 ?
                Math.max(1, (int) Math.ceil(backWallSegments / 2.0)) : 0;
        int northEastSegments = Math.max(0, backWallSegments - northWestSegments);
        return new PerimeterPlan(
                south.map(family -> createPerimeterChain("south_west", "south", family, frontBranchSegments,
                        true, Direction.EAST, Direction.WEST, "keep.corner.south_west"))
                        .orElse(List.of()),
                west.map(family -> createPerimeterChain("west", "west", family, verticalSegments,
                        false, Direction.SOUTH, Direction.NORTH, "keep.corner.north_west"))
                        .orElse(List.of()),
                north.map(family -> createPerimeterChain("north_west", "north", family, northWestSegments,
                        true, Direction.WEST, Direction.EAST, null))
                        .orElse(List.of()),
                south.map(family -> createPerimeterChain("south_east", "south", family, frontBranchSegments,
                        true, Direction.WEST, Direction.EAST, "keep.corner.south_east"))
                        .orElse(List.of()),
                east.map(family -> createPerimeterChain("east", "east", family, verticalSegments,
                        false, Direction.SOUTH, Direction.NORTH, "keep.corner.north_east"))
                        .orElse(List.of()),
                north.map(family -> createPerimeterChain("north_east", "north", family, northEastSegments,
                        true, Direction.EAST, Direction.WEST, null))
                        .orElse(List.of())
        );
    }

    private int backWallSegmentCount(boolean hasFrontWall, boolean hasBackWall, int horizontalSegments,
                                     int frontBranchSegments) {
        if (!hasBackWall) {
            return 0;
        }
        if (hasFrontWall && frontBranchSegments > 0) {
            return frontBranchSegments * 2 + 1;
        }
        return horizontalSegments;
    }

    private List<PerimeterSegment> createPerimeterChain(String chainId, String side,
                                                        MKWorkspaceLinearRunFamilyDefinition family,
                                                        int segmentCount, boolean eastWest,
                                                        Direction incomingFacing, Direction outgoingFacing,
                                                        String terminalTargetSlotId) {
        ArrayList<PerimeterSegment> segments = new ArrayList<>();
        for (int index = 0; index < segmentCount; index++) {
            String slotId = PERIMETER_ROOT_SLOT + "." + chainId + "." + index;
            String pieceName = family.linearRunId() + "_" + chainId + "_" + index;
            String nextTarget = index + 1 < segmentCount ?
                    PERIMETER_ROOT_SLOT + "." + chainId + "." + (index + 1) :
                    terminalTargetSlotId;
            boolean terminal = nextTarget == null;
            segments.add(new PerimeterSegment(chainId, side, index, segmentCount, slotId, pieceName, eastWest,
                    incomingFacing, outgoingFacing, nextTarget, terminal, family));
        }
        return List.copyOf(segments);
    }

    private Optional<MKWorkspaceLinearRunFamilyDefinition> perimeterFamilyForSide(MKStructureWorkspace workspace,
                                                                                  String sideSlotId) {
        Optional<MKWorkspaceLinearRunFamilyDefinition> rootFamily = workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(PERIMETER_ROOT_SLOT))
                .findFirst();
        Optional<MKWorkspaceLinearRunFamilyDefinition> sideFamily = workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(sideSlotId))
                .findFirst();
        return sideFamily.or(() -> rootFamily);
    }

    private int horizontalPerimeterSpan(MKStructureWorkspace workspace) {
        return cornerWidth(workspace) + DEFAULT_COURTYARD_CLEARANCE + centerWidth(workspace) +
                DEFAULT_COURTYARD_CLEARANCE + cornerWidth(workspace);
    }

    private int verticalPerimeterSpan(MKStructureWorkspace workspace) {
        int baseSpan = cornerLength(workspace) + DEFAULT_COURTYARD_CLEARANCE + centerLength(workspace) +
                DEFAULT_COURTYARD_CLEARANCE + cornerLength(workspace);
        return Math.max(baseSpan, entryDrivenVerticalPerimeterSpan(workspace));
    }

    private int entryDrivenVerticalPerimeterSpan(MKStructureWorkspace workspace) {
        MKWorkspaceLinearRunFamilyDefinition pathFamily = courtyardPathFamily(workspace);
        MKWorkspaceLinearRunFamilyDefinition entryFamily = entryApproachFamily(workspace).orElse(pathFamily);
        ResolvedOpeningProfile pathOpening = resolveOpeningProfile(workspace, pathFamily.openingProfileId())
                .orElseGet(() -> defaultOpeningProfile(workspace));
        ResolvedOpeningProfile entryOpening = resolveOpeningProfile(workspace, entryFamily.openingProfileId())
                .orElseGet(() -> defaultOpeningProfile(workspace));
        int laneInset = courtyardPathLaneCenterInset(workspace, pathOpening);
        int entryLength = effectiveEntryApproachLength(workspace, entryFamily.length(), entryOpening);
        int northBand = DEFAULT_COURTYARD_CLEARANCE + cornerLength(workspace);
        MKWalledKeepCourtyardSettings settings = workspace.topologyProfile().courtyardSettings();
        if (settings.courtyardContentEnabled() && settings.courtyardSocketGenerationEnabled()) {
            northBand = Math.max(northBand, courtyardBandSize(workspace, settings, laneInset, pathOpening));
        }
        return smallestOddAtLeast(entryLength + (centerLength(workspace) / 2) + northBand);
    }

    private int segmentCountForSpan(MKWorkspaceLinearRunFamilyDefinition family, int span) {
        return Math.max(1, (int) Math.ceil(span / (double) Math.max(1, family.length())));
    }

    private int verticalSegmentCountForSpan(MKStructureWorkspace workspace, MKWorkspaceLinearRunFamilyDefinition family,
                                            int span) {
        return segmentCountForSpan(family, span) + courtyardRearWallBufferSegments(workspace);
    }

    private int courtyardRearWallBufferSegments(MKStructureWorkspace workspace) {
        MKWalledKeepCourtyardSettings settings = workspace.topologyProfile().courtyardSettings();
        return settings.courtyardContentEnabled() && settings.courtyardSocketGenerationEnabled() ? 1 : 0;
    }

    private int courtyardBandSize(MKStructureWorkspace workspace, MKWalledKeepCourtyardSettings settings, int laneInset,
                                  ResolvedOpeningProfile opening) {
        return laneInset + walkwayHalfWidth(opening) + settings.courtyardSocketClearance() +
                courtyardContentCollisionSpan(workspace, settings);
    }

    private int walkwayHalfWidth(ResolvedOpeningProfile opening) {
        return Math.max(1, opening.openingWidth() / 2);
    }

    private int courtyardContentCollisionSpan(MKStructureWorkspace workspace, MKWalledKeepCourtyardSettings settings) {
        return settings.courtyardContentTemplateSize() + (2 * (workspace.shellMargin() + workspace.exteriorAirMargin()));
    }

    private CourtyardPlan createCourtyardPlan(MKStructureWorkspace workspace, PerimeterPlan perimeterPlan) {
        MKWalledKeepCourtyardSettings settings = workspace.topologyProfile().courtyardSettings();
        if (!settings.courtyardContentEnabled() || !settings.courtyardSocketGenerationEnabled()) {
            return CourtyardPlan.empty();
        }
        MKWalledKeepSizingReport sizingReport = new MKWalledKeepSizingCalculator().calculate(workspace);
        int freeHorizontal = sizingReport.courtyardFreeHorizontalSpan();
        int freeVertical = sizingReport.courtyardFreeVerticalSpan();
        int socketMax = sizingReport.courtyardSocketMaxSize();
        int requestedSocketSize = sizingReport.courtyardRequestedSocketSize();
        if (socketMax < requestedSocketSize) {
            String reason = "walled keep courtyard disabled: requested socket size " + requestedSocketSize +
                    " but available interior socket span is " + socketMax + " (horizontal free span " +
                    freeHorizontal + ", vertical free span " + freeVertical + ")";
            MKNpc.LOGGER.warn(reason);
            return CourtyardPlan.disabled(reason, freeHorizontal, freeVertical, requestedSocketSize);
        }
        ArrayList<CourtyardSocket> sockets = new ArrayList<>();
        for (CourtyardSocketDefinition definition : COURTYARD_SOCKET_DEFINITIONS) {
            sockets.add(new CourtyardSocket(definition.slotId(), socketMax, definition.contentConnectorFacing(), true));
        }
        return new CourtyardPlan(List.copyOf(sockets), List.copyOf(COURTYARD_PATH_DEFINITIONS), Optional.empty(),
                freeHorizontal, freeVertical, requestedSocketSize);
    }

    private int largestOddAtMost(int value) {
        if (value < 1) {
            return 0;
        }
        return value % 2 == 0 ? value - 1 : value;
    }

    private int smallestOddAtLeast(int value) {
        int clamped = Math.max(1, value);
        return clamped % 2 == 0 ? clamped + 1 : clamped;
    }

    private int centerWidth(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().verticalStackSettingsOrDefault("keep.center").width();
    }

    private int centerLength(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().verticalStackSettingsOrDefault("keep.center").length();
    }

    private int cornerWidth(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().verticalStackSettingsOrDefault(cornerSettingsSlot(workspace)).width();
    }

    private int cornerLength(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().verticalStackSettingsOrDefault(cornerSettingsSlot(workspace)).length();
    }

    private String cornerSettingsSlot(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().anySharedCornerTower() ? "keep.corner.shared" : "keep.corner.north_west";
    }

    private List<MKPlannedPiece> createPerimeterPieces(MKStructureWorkspace workspace, PerimeterPlan perimeterPlan) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        String templateSourceId = perimeterPlan.allSegments().stream()
                .findFirst()
                .map(PerimeterSegment::pieceName)
                .orElse("");
        for (PerimeterSegment segment : perimeterPlan.allSegments()) {
            MKWorkspaceLinearRunFamilyDefinition family = segment.family();
            if (!family.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
                continue;
            }
            ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, family.openingProfileId())
                    .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " +
                            family.openingProfileId()));
            MKPlannedPiece piece = new MKPlannedPiece(
                    segment.slotId(),
                    segment.pieceName(),
                    segment.eastWest() ? family.length() : family.interiorWidth(),
                    segment.eastWest() ? family.interiorWidth() : family.length(),
                    family.interiorHeight() + Math.abs(family.slopeDelta()),
                    perimeterSegmentConnectors(segment, opening),
                    buildLinearRunTags(workspace, family, segment)
            );
            boolean authoringSource = segment.pieceName().equals(templateSourceId);
            pieces.add(templateSourceId.isBlank() ? piece : withTemplateReuse(piece, templateSourceId,
                    authoringSource ? MKWorkspaceTemplateReuseTags.ROTATION_NONE : rotationForPerimeterSegment(segment),
                    authoringSource));
        }
        return List.copyOf(pieces);
    }

    private List<MKPlannedPiece> createCourtyardContentPieces(MKStructureWorkspace workspace,
                                                              CourtyardPlan courtyardPlan) {
        MKWalledKeepCourtyardSettings settings = workspace.topologyProfile().courtyardSettings();
        if (!settings.courtyardContentEnabled()) {
            return List.of();
        }
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
        int contentSize = settings.courtyardContentTemplateSize();
        pieces.add(createCourtyardContentSourcePiece(workspace, settings, contentSize, opening));
        for (CourtyardSocket socket : courtyardPlan.sockets()) {
            if (!socket.enabled()) {
                continue;
            }
            if (contentSize <= 0 || contentSize > socket.maxSquareSize()) {
                continue;
            }
            pieces.add(createCourtyardSocketRuntimePiece(workspace, settings, socket, contentSize, opening));
        }
        return List.copyOf(pieces);
    }

    private MKPlannedPiece createCourtyardContentSourcePiece(MKStructureWorkspace workspace,
                                                             MKWalledKeepCourtyardSettings settings,
                                                             int size, ResolvedOpeningProfile opening) {
        return new MKPlannedPiece(
                "keep.courtyard.content",
                COURTYARD_CONTENT_SOURCE,
                size,
                size,
                settings.courtyardContentTemplateHeight(),
                List.of(MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.SOUTH,
                        opening.openingWidth(), opening.openingHeight(), 0, 0)),
                buildCourtyardContentTags(workspace, settings, size, Direction.SOUTH, false, null)
        );
    }

    private MKPlannedPiece createCourtyardSocketRuntimePiece(MKStructureWorkspace workspace,
                                                             MKWalledKeepCourtyardSettings settings,
                                                             CourtyardSocket socket, int size,
                                                             ResolvedOpeningProfile opening) {
        Direction facing = socket.contentConnectorFacing();
        String socketSuffix = socket.slotId().substring(COURTYARD_SLOT_PREFIX.length());
        String pieceName = COURTYARD_CONTENT_SOURCE + "_" + socketSuffix;
        MKPlannedPiece piece = new MKPlannedPiece(
                "keep.courtyard.content",
                pieceName,
                size,
                size,
                settings.courtyardContentTemplateHeight(),
                List.of(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                        opening.openingWidth(), opening.openingHeight(), EMPTY_POOL, slotPool(socket.slotId()))),
                buildCourtyardContentTags(workspace, settings, size, facing, true, socket)
        );
        return withTemplateReuse(piece, COURTYARD_CONTENT_SOURCE, rotationFromSouthTo(facing), false);
    }

    private Map<String, String> buildCourtyardContentTags(MKStructureWorkspace workspace,
                                                          MKWalledKeepCourtyardSettings settings,
                                                          int size, Direction connectorFacing,
                                                          boolean socketRuntime, CourtyardSocket socket) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", "keep.courtyard.content");
        tags.put("workspace_topology_slot_id", socketRuntime && socket != null ? socket.slotId() :
                "keep.courtyard.content");
        tags.put("workspace_topology_role_id", "keep.courtyard.content");
        tags.put("tower_piece_kind", "courtyard_content");
        tags.put("workspace_piece_kind", "instance");
        tags.put(CONTENT_KIND_TAG, COURTYARD_CONTENT_KIND);
        tags.put(CONTENT_SIZE_TAG, Integer.toString(size));
        tags.put(CONTENT_WIDTH_TAG, Integer.toString(size));
        tags.put(CONTENT_LENGTH_TAG, Integer.toString(size));
        tags.put(CONTENT_HEIGHT_TAG, Integer.toString(settings.courtyardContentTemplateHeight()));
        tags.put(CONTENT_REQUIRES_PATH_TAG, Boolean.toString(true));
        tags.put(CONTENT_CONNECTOR_EDGE_TAG, connectorFacing.getSerializedName());
        tags.put(CONTENT_WALKWAY_CONTINUATION_LENGTH_TAG,
                Integer.toString(settings.courtyardWalkwayContinuationLength()));
        if (socket != null) {
            tags.put(COURTYARD_SOCKET_ID_TAG, socket.slotId());
            tags.put(COURTYARD_SOCKET_MAX_SIZE_TAG, Integer.toString(socket.maxSquareSize()));
        }
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, false, false).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, workspace.palette());
        return tags;
    }

    private Map<String, String> buildCourtyardPathTags(MKStructureWorkspace workspace,
                                                       MKWorkspaceLinearRunFamilyDefinition family,
                                                       String topologySlotId, String shape, int laneInset) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(buildLinearRunTags(workspace, family,
                topologySlotId));
        tags.put("topology_role", "keep.courtyard.path");
        tags.put("workspace_topology_role_id", "keep.courtyard.path");
        tags.put("workspace_linear_run_family_id", "keep_courtyard_path");
        tags.put("workspace_linear_run_kind", MKWorkspaceLinearRunKind.OPEN_WALKWAY.getSerializedName());
        tags.put("workspace_linear_run_path_kind", "courtyard_path");
        tags.put(CONTENT_KIND_TAG, COURTYARD_PATH_KIND);
        tags.put(COURTYARD_PATH_SLOT_ID_TAG, topologySlotId);
        tags.put(COURTYARD_PATH_SHAPE_TAG, shape);
        tags.put(COURTYARD_PATH_LANE_INSET_TAG, Integer.toString(laneInset));
        return tags;
    }

    private String rotationFromSouthTo(Direction facing) {
        return switch (facing) {
            case WEST -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90;
            case NORTH -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180;
            case EAST -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270;
            default -> MKWorkspaceTemplateReuseTags.ROTATION_NONE;
        };
    }

    private String rotationForPerimeterSegment(PerimeterSegment segment) {
        return switch (segment.side()) {
            case "west" -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_90;
            case "north" -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180;
            case "east" -> MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_270;
            default -> MKWorkspaceTemplateReuseTags.ROTATION_NONE;
        };
    }

    private MKPlannedPiece withTemplateReuse(MKPlannedPiece piece, String sourceId, String rotation,
                                             boolean authoringSource) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG, sourceId);
        tags.put(MKWorkspaceTemplateReuseTags.ROTATION_TAG, rotation);
        tags.put(MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG,
                MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT);
        tags.put(MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, Boolean.toString(authoringSource));
        return new MKPlannedPiece(
                piece.roleId(),
                piece.pieceName(),
                piece.interiorWidth(),
                piece.interiorLength(),
                piece.interiorHeight(),
                piece.connectors(),
                tags
        );
    }

    private MKPlannedPiece withExportCrop(MKPlannedPiece piece) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put(MKWorkspaceTemplateReuseTags.CROP_MODE_TAG,
                MKWorkspaceTemplateReuseTags.CROP_MODE_NON_STRUCTURE_VOID);
        return new MKPlannedPiece(
                piece.roleId(),
                piece.pieceName(),
                piece.interiorWidth(),
                piece.interiorLength(),
                piece.interiorHeight(),
                piece.connectors(),
                tags
        );
    }

    private MKPlannedPiece withCourtyardDisabledDiagnostics(MKPlannedPiece piece, CourtyardPlan courtyardPlan) {
        if (courtyardPlan.disabledReason().isEmpty()) {
            return piece;
        }
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        tags.put(COURTYARD_DISABLED_REASON_TAG, courtyardPlan.disabledReason().get());
        tags.put(COURTYARD_AVAILABLE_HORIZONTAL_SPAN_TAG,
                Integer.toString(courtyardPlan.availableHorizontalSpan()));
        tags.put(COURTYARD_AVAILABLE_VERTICAL_SPAN_TAG,
                Integer.toString(courtyardPlan.availableVerticalSpan()));
        tags.put(COURTYARD_REQUESTED_MAX_SOCKET_SIZE_TAG,
                Integer.toString(courtyardPlan.requestedMaxSocketSize()));
        return new MKPlannedPiece(
                piece.roleId(),
                piece.pieceName(),
                piece.interiorWidth(),
                piece.interiorLength(),
                piece.interiorHeight(),
                piece.connectors(),
                tags
        );
    }

    private List<MKPlannedConnector> perimeterSegmentConnectors(PerimeterSegment segment,
                                                                ResolvedOpeningProfile opening) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, segment.incomingFacing(),
                opening.openingWidth(), opening.openingHeight(), 0, 0, EMPTY_POOL, slotPool(segment.slotId())));
        if (segment.terminal()) {
            connectors.add(MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, segment.outgoingFacing(),
                    opening.openingWidth(), opening.openingHeight(), 0, 0));
        } else {
            connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, segment.outgoingFacing(),
                    opening.openingWidth(), opening.openingHeight(), slotPool(segment.outgoingTargetSlotId())));
        }
        return List.copyOf(connectors);
    }

    private List<MKPlannedConnector> roomLayoutConnectors(MKStructureWorkspace workspace, String topologySlotId,
                                                          SlotAvailability slots,
                                                          ResolvedOpeningProfile opening) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        Set<String> availableSlots = slots.availableSlots();
        switch (topologySlotId) {
            case "keep.center.entry" -> {
                if (availableSlots.contains(ENTRY_APPROACH_SLOT)) {
                    IngressConnection ingress = centerEntryIngressConnection(workspace, opening);
                    connectors.add(new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                            ingress.opening().openingWidth(), ingress.opening().openingHeight(),
                            ingress.lateralOffset(), ingress.verticalOffset(), slotPool(ENTRY_APPROACH_SLOT), null,
                            MKWorkspaceHorizontalExtrusionMode.FLOOR_ONLY));
                }
            }
            case "keep.gate.main" -> {
                connectors.add(new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH,
                        opening.openingWidth(), opening.openingHeight(), poolOrEmpty(ENTRY_APPROACH_SLOT,
                        availableSlots), slotPool("keep.gate.main")));
                connectors.add(MKPlannedConnector.openingOnly(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                        opening.openingWidth(), opening.openingHeight(), 0, 0));
                slots.perimeterPlan().firstSouthWest()
                        .ifPresent(segment -> addFullFaceWallTarget(connectors, Direction.WEST,
                                segment.slotId(), opening));
                slots.perimeterPlan().firstSouthEast()
                        .ifPresent(segment -> addFullFaceWallTarget(connectors, Direction.EAST,
                                segment.slotId(), opening));
            }
            case "keep.corner.shared" -> addSharedCornerConnectors(connectors, slots.sharedCornerSlots(),
                    slots.perimeterPlan(), opening);
            case "keep.corner.shared.entry" -> addSharedCornerConnectors(connectors, slots.sharedCornerSlots(),
                    slots.perimeterPlan(), opening);
            case "keep.corner.north_west", "keep.corner.north_east", "keep.corner.south_east",
                 "keep.corner.south_west" -> addConcreteCornerConnectors(connectors, topologySlotId,
                    slots.perimeterPlan(),
                    opening);
            case "keep.corner.north_west.entry", "keep.corner.north_east.entry", "keep.corner.south_east.entry",
                 "keep.corner.south_west.entry" -> addConcreteCornerConnectors(connectors,
                    topologySlotId.substring(0, topologySlotId.length() - ".entry".length()),
                    slots.perimeterPlan(), opening);
            default -> {
            }
        }
        return List.copyOf(connectors);
    }

    private List<MKPlannedConnector> linearRunLayoutConnectors(MKStructureWorkspace workspace,
                                                               MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                               DirectionPair directions,
                                                               Set<String> availableSlots,
                                                               ResolvedOpeningProfile opening,
                                                               int effectiveLength) {
        String slotId = linearRun.topologySlotId();
        int negativeOffset = Math.max(0, -linearRun.slopeDelta());
        int positiveOffset = Math.max(0, linearRun.slopeDelta());
        return switch (slotId) {
            case ENTRY_APPROACH_SLOT -> entryApproachConnectors(workspace, effectiveLength, slotId, availableSlots,
                    opening, negativeOffset, positiveOffset);
            case "keep.walkway.west" -> courtyardWalkwayConnectors(slotId, Direction.SOUTH, Direction.NORTH,
                    "keep.courtyard.north", Direction.WEST, "keep.courtyard.west", availableSlots, opening,
                    negativeOffset, positiveOffset);
            case "keep.walkway.east" -> courtyardWalkwayConnectors(slotId, Direction.SOUTH, Direction.NORTH,
                    null, Direction.EAST, "keep.courtyard.east", availableSlots, opening, negativeOffset,
                    positiveOffset);
            case "keep.walkway.south" -> List.of(
                    new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH,
                            opening.openingWidth(), opening.openingHeight(), 0, negativeOffset,
                            EMPTY_POOL, slotPool(slotId)),
                    new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                            opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                            poolOrEmpty("keep.gate.main", availableSlots), EMPTY_POOL)
            );
            case "keep.perimeter.north" -> perimeterConnectors(slotId, directions, "keep.corner.north_west",
                    "keep.corner.north_east", availableSlots, opening, negativeOffset, positiveOffset);
            case "keep.perimeter.east" -> perimeterConnectors(slotId, directions, "keep.corner.north_east",
                    "keep.corner.south_east", availableSlots, opening, negativeOffset, positiveOffset);
            case "keep.perimeter.south" -> perimeterConnectors(slotId, directions, "keep.corner.south_west",
                    "keep.corner.south_east", availableSlots, opening, negativeOffset, positiveOffset);
            case "keep.perimeter.west" -> perimeterConnectors(slotId, directions, "keep.corner.north_west",
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

    private List<MKPlannedConnector> courtyardWalkwayConnectors(String slotId, Direction incomingFacing,
                                                                Direction terminalFacing, String terminalTargetSlotId,
                                                                Direction contentFacing, String contentSlotId,
                                                                Set<String> availableSlots,
                                                                ResolvedOpeningProfile opening, int negativeOffset,
                                                                int positiveOffset) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, incomingFacing,
                opening.openingWidth(), opening.openingHeight(), 0, negativeOffset,
                EMPTY_POOL, slotPool(slotId)));
        if (terminalTargetSlotId == null || !availableSlots.contains(terminalTargetSlotId)) {
            connectors.add(MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, terminalFacing,
                    opening.openingWidth(), opening.openingHeight(), 0, positiveOffset));
        } else {
            connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, terminalFacing,
                    opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                    slotPool(terminalTargetSlotId)));
        }
        addBranchTarget(connectors, contentFacing, contentSlotId, availableSlots, opening);
        return List.copyOf(connectors);
    }

    private List<MKPlannedConnector> sourceCourtyardPathConnectors(String shape, ResolvedOpeningProfile opening) {
        if ("corner_t".equals(shape)) {
            return List.of(
                    MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.EAST,
                            opening.openingWidth(), opening.openingHeight(), 0, 0),
                    MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.NORTH,
                            opening.openingWidth(), opening.openingHeight(), 0, 0),
                    MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.SOUTH,
                            opening.openingWidth(), opening.openingHeight(), 0, 0)
            );
        }
        return List.of(
                MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.SOUTH,
                        opening.openingWidth(), opening.openingHeight(), 0, 0),
                MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.NORTH,
                        opening.openingWidth(), opening.openingHeight(), 0, 0),
                MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, Direction.WEST,
                        opening.openingWidth(), opening.openingHeight(), 0, 0)
        );
    }

    private List<MKPlannedConnector> courtyardPathConnectors(MKStructureWorkspace workspace,
                                                             CourtyardPathDefinition path,
                                                             int pathSize,
                                                             Set<String> availableSlots,
                                                             ResolvedOpeningProfile opening) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, path.incomingFacing(),
                opening.openingWidth(), opening.openingHeight(), 0, 0,
                EMPTY_POOL, slotPool(path.slotId())));
        if (path.outgoingTargetSlotId() == null || !availableSlots.contains(path.outgoingTargetSlotId())) {
            connectors.add(MKPlannedConnector.openingOnly(MKConnectorRole.BRANCH, path.outgoingFacing(),
                    opening.openingWidth(), opening.openingHeight(), 0, 0));
        } else {
            addBranchTargetDirect(connectors, path.outgoingFacing(), path.outgoingTargetSlotId(), opening);
        }
        addBranchTarget(connectors, path.contentFacing(), path.contentSlotId(), availableSlots, opening);
        return List.copyOf(connectors);
    }

    private List<MKPlannedConnector> entryApproachConnectors(MKStructureWorkspace workspace,
                                                             int entryLength, String slotId,
                                                             Set<String> availableSlots,
                                                             ResolvedOpeningProfile opening, int negativeOffset,
                                                             int positiveOffset) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        IngressConnection ingress = centerEntryIngressConnection(workspace, opening);
        connectors.add(new MKPlannedConnector(MKConnectorRole.MAIN_FORWARD, Direction.NORTH,
                ingress.opening().openingWidth(), ingress.opening().openingHeight(),
                ingress.lateralOffset(), ingress.verticalOffset() + negativeOffset,
                EMPTY_POOL, slotPool(slotId)));
        connectors.add(new MKPlannedConnector(MKConnectorRole.MAIN_BACK, Direction.SOUTH,
                opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                poolOrEmpty("keep.gate.main", availableSlots), EMPTY_POOL));
        int branchOffset = entryApproachCourtyardBranchOffset(workspace, entryLength, opening);
        firstAvailableSlot(availableSlots, "keep.courtyard.path.south_west")
                .ifPresent(targetSlot -> addBranchTargetDirect(connectors, Direction.WEST, targetSlot, opening,
                        branchOffset));
        return List.copyOf(connectors);
    }

    private int entryApproachCourtyardBranchOffset(MKStructureWorkspace workspace, int entryLength,
                                                   ResolvedOpeningProfile opening) {
        return entryApproachCourtyardBranchCenter(workspace, entryLength, opening) - (entryLength / 2);
    }

    private int entryApproachCourtyardBranchCenter(MKStructureWorkspace workspace, int entryLength,
                                                   ResolvedOpeningProfile entryOpening) {
        MKWorkspaceLinearRunFamilyDefinition pathFamily = courtyardPathFamily(workspace);
        ResolvedOpeningProfile pathOpening = resolveOpeningProfile(workspace, pathFamily.openingProfileId())
                .orElseGet(() -> defaultOpeningProfile(workspace));
        int laneInset = courtyardPathLaneCenterInset(workspace, pathOpening);
        int pathSize = courtyardPathSize(workspace, pathFamily, laneInset);
        int pathCenter = pathSize / 2;
        int gatehouseLimit = entryLength - entryApproachGatehouseClearance(workspace, entryOpening, pathOpening) -
                (entryOpening.openingWidth() / 2);
        return Math.max(0, Math.min(pathCenter, gatehouseLimit));
    }

    private List<MKPlannedConnector> perimeterConnectors(String perimeterSlotId, DirectionPair directions,
                                                         String negativeCornerSlotId, String positiveCornerSlotId,
                                                         Set<String> availableSlots, ResolvedOpeningProfile opening,
                                                         int negativeOffset, int positiveOffset) {
        return List.of(
                new MKPlannedConnector(MKConnectorRole.BRANCH, directions.negative(),
                        opening.openingWidth(), opening.openingHeight(), 0, negativeOffset,
                        poolOrEmpty(negativeCornerSlotId, availableSlots), slotPool(perimeterSlotId)),
                new MKPlannedConnector(MKConnectorRole.BRANCH, directions.positive(),
                        opening.openingWidth(), opening.openingHeight(), 0, positiveOffset,
                        poolOrEmpty(positiveCornerSlotId, availableSlots), slotPool(perimeterSlotId))
        );
    }

    private void addSharedCornerConnectors(List<MKPlannedConnector> connectors, Set<String> sharedCornerSlots,
                                           PerimeterPlan perimeterPlan, ResolvedOpeningProfile opening) {
        if (sharedCornerSlots.contains("keep.corner.north_west")) {
            addConcreteCornerConnectors(connectors, "keep.corner.north_west", perimeterPlan, opening);
        }
        if (sharedCornerSlots.contains("keep.corner.north_east")) {
            addConcreteCornerConnectors(connectors, "keep.corner.north_east", perimeterPlan, opening);
        }
        if (sharedCornerSlots.contains("keep.corner.south_east")) {
            addConcreteCornerConnectors(connectors, "keep.corner.south_east", perimeterPlan, opening);
        }
        if (sharedCornerSlots.contains("keep.corner.south_west")) {
            addConcreteCornerConnectors(connectors, "keep.corner.south_west", perimeterPlan, opening);
        }
    }

    private void addConcreteCornerConnectors(List<MKPlannedConnector> connectors, String cornerSlotId,
                                             PerimeterPlan perimeterPlan, ResolvedOpeningProfile opening) {
        switch (cornerSlotId) {
            case "keep.corner.north_west" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.SOUTH, opening);
                perimeterPlan.firstNorthWest().ifPresent(segment -> addFullFaceWallTarget(connectors, Direction.EAST,
                        segment.slotId(), opening));
            }
            case "keep.corner.north_east" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.SOUTH, opening);
                perimeterPlan.firstNorthEast().ifPresent(segment -> addFullFaceWallTarget(connectors, Direction.WEST,
                        segment.slotId(), opening));
            }
            case "keep.corner.south_east" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.WEST, opening);
                perimeterPlan.firstEast().ifPresent(segment -> addFullFaceWallTarget(connectors, Direction.NORTH,
                        segment.slotId(), opening));
            }
            case "keep.corner.south_west" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.EAST, opening);
                perimeterPlan.firstWest().ifPresent(segment -> addFullFaceWallTarget(connectors, Direction.NORTH,
                        segment.slotId(), opening));
            }
            default -> {
            }
        }
    }

    private void addIncomingCornerConnector(List<MKPlannedConnector> connectors, String cornerSlotId,
                                            Direction facing, ResolvedOpeningProfile opening) {
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                opening.openingWidth(), opening.openingHeight(), 0, 0,
                EMPTY_POOL, slotPool(cornerSlotId), MKWorkspaceHorizontalExtrusionMode.FULL_FACE));
    }

    private void addFullFaceWallTarget(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                       ResolvedOpeningProfile opening) {
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                opening.openingWidth(), opening.openingHeight(), 0, 0, slotPool(targetSlotId), null,
                MKWorkspaceHorizontalExtrusionMode.FULL_FACE));
    }

    private void addBranchTarget(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                 Set<String> availableSlots, ResolvedOpeningProfile opening) {
        if (availableSlots.contains(targetSlotId)) {
            addBranchTargetDirect(connectors, facing, targetSlotId, opening);
        }
    }

    private void addBranchTargetWithOffset(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                           Set<String> availableSlots, ResolvedOpeningProfile opening,
                                           int lateralOffset) {
        if (availableSlots.contains(targetSlotId)) {
            connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                    opening.openingWidth(), opening.openingHeight(), lateralOffset, 0, slotPool(targetSlotId)));
        }
    }

    private void addBranchTargetDirect(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                       ResolvedOpeningProfile opening) {
        addBranchTargetDirect(connectors, facing, targetSlotId, opening, 0);
    }

    private void addBranchTargetDirect(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                       ResolvedOpeningProfile opening, int lateralOffset) {
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                opening.openingWidth(), opening.openingHeight(), lateralOffset, 0, slotPool(targetSlotId)));
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, MKWorkspaceRoomFamilyDefinition family) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", family.topologySlotId());
        tags.put("workspace_topology_slot_id", family.topologySlotId());
        tags.put("workspace_topology_role_id", family.topologySlotId());
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", effectiveRoomExtrusionMode(workspace, family).getSerializedName());
        verticalStackSettingsForTopologySlot(workspace, family.topologySlotId())
                .ifPresent(settings -> {
                    tags.put("workspace_tower_stack_id", settings.stackId());
                    tags.put("workspace_tower_stack_main_floors", Integer.toString(settings.mainFloors()));
                    tags.put("workspace_tower_stack_basement_floors", Integer.toString(settings.basementFloors()));
                });
        MKWorkspaceResolvedFamilySettings resolvedFamily = workspace.resolveFamilySettings(family);
        tags.put("workspace_topology_group", resolvedFamily.slotMetadata().topologyGroupId());
        applyFoundationTags(resolvedFamily.foundationPolicy(), tags);
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
            tags.put("workspace_vertical_access_group_id", family.verticalAccessGroupId());
            tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG,
                    resolvedFamily.verticalAccessSpec().placement().getSerializedName());
            tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, verticalAccessDirectionTag(family));
        }
        runtimeInfoForRoom(family).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, resolvedFamily.palette());
        return tags;
    }

    private MKWorkspaceHorizontalExtrusionMode effectiveRoomExtrusionMode(MKStructureWorkspace workspace,
                                                                          MKWorkspaceRoomFamilyDefinition family) {
        return verticalStackSettingsForTopologySlot(workspace, family.topologySlotId())
                .map(MKWorkspaceVerticalStackSettings::horizontalExtrusionMode)
                .orElse(family.horizontalExtrusionMode());
    }

    private Optional<MKWorkspaceHorizontalExtrusionMode> effectiveRoomExtrusionMode(MKStructureWorkspace workspace,
                                                                                   String topologySlotId) {
        return verticalStackSettingsForTopologySlot(workspace, topologySlotId)
                .map(MKWorkspaceVerticalStackSettings::horizontalExtrusionMode);
    }

    private Optional<MKWorkspaceVerticalStackSettings> verticalStackSettingsForTopologySlot(MKStructureWorkspace workspace,
                                                                                     String topologySlotId) {
        String stackId = stackIdForTopologySlot(topologySlotId);
        if (stackId.isBlank()) {
            return Optional.empty();
        }
        if (stackId.startsWith("keep.corner.") &&
                !"keep.corner.shared".equals(stackId) &&
                !workspace.topologyProfile().uniqueCornerTower(stackId)) {
            return workspace.topologyProfile().verticalStackSettings("keep.corner.shared");
        }
        return workspace.topologyProfile().verticalStackSettings(stackId);
    }

    private Map<String, String> buildLinearRunTags(MKStructureWorkspace workspace,
                                                   MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return buildLinearRunTags(workspace, linearRun, linearRun.topologySlotId());
    }

    private Map<String, String> buildLinearRunTags(MKStructureWorkspace workspace,
                                                   MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                   PerimeterSegment segment) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(
                buildLinearRunTags(workspace, linearRun, segment.slotId()));
        tags.put("workspace_perimeter_source_slot_id", linearRun.topologySlotId());
        tags.put("workspace_perimeter_chain_id", segment.chainId());
        tags.put("workspace_perimeter_side", segment.side());
        tags.put("workspace_perimeter_segment_index", Integer.toString(segment.index()));
        tags.put("workspace_perimeter_segment_count", Integer.toString(segment.count()));
        return tags;
    }

    private Map<String, String> buildLinearRunTags(MKStructureWorkspace workspace,
                                                   MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                   String topologySlotId) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", topologySlotId);
        tags.put("workspace_topology_slot_id", topologySlotId);
        tags.put("workspace_topology_role_id", topologySlotId);
        tags.put("tower_piece_kind", "linear_run");
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_linear_run_family_id", linearRun.linearRunId());
        tags.put("workspace_linear_run_kind", linearRun.kind().getSerializedName());
        tags.put("workspace_linear_run_projection", linearRun.projection().getSerializedName());
        tags.put("workspace_linear_run_shape", MKWorkspaceLinearRunPieceShape.STRAIGHT.getSerializedName());
        tags.put("workspace_linear_run_path_kind", "keep");
        tags.put("workspace_linear_run_slope_delta", Integer.toString(linearRun.slopeDelta()));
        tags.put("workspace_opening_profile_id", linearRun.openingProfileId());
        if (linearRun.kind() == MKWorkspaceLinearRunKind.DEFENSIVE_WALL) {
            tags.put("workspace_horizontal_extrusion_mode",
                    MKWorkspaceHorizontalExtrusionMode.FULL_FACE.getSerializedName());
        }
        if (linearRun.topVoidMargin() > 0) {
            tags.put(MKWorkspaceVoidMarginTags.TOP_VOID_MARGIN_TAG, Integer.toString(linearRun.topVoidMargin()));
        }
        applyFoundationTags(linearRun.foundationPolicy(), tags);
        new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, false, false).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, paletteResolver.resolveFamily(workspace, linearRun));
        return tags;
    }

    private String stackIdForFamily(MKWorkspaceRoomFamilyDefinition family) {
        return stackIdForTopologySlot(family.topologySlotId());
    }

    private String stackIdForTopologySlot(String topologySlotId) {
        if (topologySlotId.startsWith("keep.center.")) {
            return "keep.center";
        }
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isPresent()) {
            return cornerStackId.get();
        }
        if ("keep.corner.shared".equals(topologySlotId) ||
                CONCRETE_CORNER_SLOTS.contains(topologySlotId)) {
            return topologySlotId;
        }
        return "";
    }

    private MKWorkspaceRuntimePieceInfo runtimeInfoForRoom(MKWorkspaceRoomFamilyDefinition family) {
        MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
        boolean start = family.topologySlotId().equals("keep.center.entry");
        return new MKWorkspaceRuntimePieceInfo(start, slotMetadata.jigsawPieceRole(), 0, 0, true, true,
                slotMetadata.terminal(), false, slotMetadata.topologyGroupId(), false, false);
    }

    private IngressConnection centerEntryIngressConnection(MKStructureWorkspace workspace,
                                                           ResolvedOpeningProfile fallbackOpening) {
        return centerEntryIngress(workspace)
                .map(exit -> new IngressConnection(
                        resolveOpeningProfile(workspace, exit.openingProfileId()).orElse(fallbackOpening),
                        connectorLateralOffset(exit.direction(), exit.sideOffset()),
                        exit.verticalOffset()
                ))
                .orElseGet(() -> new IngressConnection(fallbackOpening, 0, 0));
    }

    private Optional<MKWorkspaceFamilyHorizontalExitDefinition> centerEntryIngress(MKStructureWorkspace workspace) {
        return workspace.familyDefinitions().stream()
                .filter(family -> "keep.center.entry".equals(family.topologySlotId()))
                .flatMap(family -> family.horizontalOnlyExits().stream())
                .filter(exit -> exit.direction() == Direction.SOUTH)
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.INGRESS)
                .findFirst();
    }

    private int connectorLateralOffset(Direction direction, int sideOffset) {
        return switch (direction) {
            case NORTH, EAST -> sideOffset;
            case SOUTH, WEST -> -sideOffset;
            default -> 0;
        };
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

    private String verticalAccessDirectionTag(MKWorkspaceRoomFamilyDefinition family) {
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

    private Optional<String> firstAvailableSlot(Set<String> availableSlots, String... topologySlotIds) {
        for (String topologySlotId : topologySlotIds) {
            if (availableSlots.contains(topologySlotId)) {
                return Optional.of(topologySlotId);
            }
        }
        return Optional.empty();
    }

    private static boolean isKnownKeepSlot(String topologySlotId) {
        return KNOWN_KEEP_SLOTS.contains(topologySlotId) ||
                topologySlotId.startsWith(PERIMETER_ROOT_SLOT + ".") ||
                topologySlotId.startsWith(WALKWAY_WEST_ROOT_SLOT + ".") ||
                topologySlotId.startsWith(WALKWAY_EAST_ROOT_SLOT + ".") ||
                topologySlotId.startsWith(COURTYARD_SLOT_PREFIX) ||
                topologySlotId.startsWith("keep.courtyard.content.") ||
                isCenterStackSlot(topologySlotId) ||
                isCornerStackSlot(topologySlotId);
    }

    private static boolean isPerimeterRunFamily(MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return linearRun.topologySlotId().equals(PERIMETER_ROOT_SLOT) ||
                linearRun.topologySlotId().startsWith(PERIMETER_ROOT_SLOT + ".");
    }

    private static boolean isCourtyardWalkwayRootFamily(MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return linearRun.topologySlotId().equals(WALKWAY_WEST_ROOT_SLOT) ||
                linearRun.topologySlotId().equals(WALKWAY_EAST_ROOT_SLOT);
    }

    private static boolean isCenterStackSlot(String topologySlotId) {
        return topologySlotId.startsWith("keep.center.");
    }

    private static boolean isCornerStackSlot(String topologySlotId) {
        return MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(topologySlotId)
                .filter(stackId -> stackId.equals("keep.corner.shared") || CONCRETE_CORNER_SLOTS.contains(stackId))
                .isPresent();
    }

    private static Optional<String> cornerStackIdForSlot(String topologySlotId) {
        if ("keep.corner.shared".equals(topologySlotId) || topologySlotId.startsWith("keep.corner.shared.")) {
            return Optional.of("keep.corner.shared");
        }
        Optional<String> stackSlotId = MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(topologySlotId)
                .filter(CONCRETE_CORNER_SLOTS::contains);
        if (stackSlotId.isPresent()) {
            return stackSlotId;
        }
        return CONCRETE_CORNER_SLOTS.stream()
                .filter(topologySlotId::equals)
                .findFirst();
    }

    private static boolean isActiveKeepSlot(MKStructureWorkspace workspace, String topologySlotId) {
        Optional<String> cornerStackId = cornerStackIdForSlot(topologySlotId);
        if (cornerStackId.isPresent()) {
            topologySlotId = cornerStackId.get();
        }
        if ("keep.corner.shared".equals(topologySlotId)) {
            return workspace.topologyProfile().anySharedCornerTower();
        }
        if (CONCRETE_CORNER_SLOTS.contains(topologySlotId)) {
            return workspace.topologyProfile().uniqueCornerTower(topologySlotId);
        }
        return isKnownKeepSlot(topologySlotId);
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
