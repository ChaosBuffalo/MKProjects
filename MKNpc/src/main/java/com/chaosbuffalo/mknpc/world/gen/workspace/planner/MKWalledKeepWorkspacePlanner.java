package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceResolvedFamilySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVoidMarginTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
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
    private static final String PERIMETER_ROOT_SLOT = "keep.perimeter";
    private static final int DEFAULT_COURTYARD_CLEARANCE = 5;
    private static final String SLOT_POOL_PREFIX = "keep_slots/";
    private static final List<String> CONCRETE_CORNER_SLOTS = List.of(
            "keep.corner.north_west",
            "keep.corner.north_east",
            "keep.corner.south_east",
            "keep.corner.south_west"
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
            "keep.walkway.east",
            "keep.walkway.south",
            "keep.walkway.west",
            "keep.gate.main"
    );
    private final MKWorkspacePaletteResolver paletteResolver = new MKWorkspacePaletteResolver();
    private final MKTowerStackPlanner towerStackPlanner = new MKTowerStackPlanner();

    private record ResolvedOpeningProfile(String profileId, int openingWidth, int openingHeight) {
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
                                 List<PerimeterSegment> north,
                                 List<PerimeterSegment> east,
                                 List<PerimeterSegment> southEast) {
        private List<PerimeterSegment> allSegments() {
            ArrayList<PerimeterSegment> segments = new ArrayList<>();
            segments.addAll(southWest);
            segments.addAll(west);
            segments.addAll(north);
            segments.addAll(east);
            segments.addAll(southEast);
            return List.copyOf(segments);
        }

        private Optional<PerimeterSegment> firstSouthWest() {
            return first(southWest);
        }

        private Optional<PerimeterSegment> firstWest() {
            return first(west);
        }

        private Optional<PerimeterSegment> firstNorth() {
            return first(north);
        }

        private Optional<PerimeterSegment> firstEast() {
            return first(east);
        }

        private Optional<PerimeterSegment> firstSouthEast() {
            return first(southEast);
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
                                    PerimeterPlan perimeterPlan) {
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
                        new MKWorkspaceLinkSchema("keep.perimeter.clockwise", "keep.gate.main", "keep.gate.main", "linear_run")
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
        slots.add(new MKWorkspaceSlotSchema("keep.walkway.east", "keep.walkways", "open_walkway",
                "keep.walkway.east", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.walkway.south", "keep.walkways", "open_walkway",
                "keep.walkway.south", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.walkway.west", "keep.walkways", "open_walkway",
                "keep.walkway.west", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.gate.main", "keep.gates", "entry", "keep.gate.main",
                MKWorkspaceSlotSchema.Repeat.OPTIONAL));
        return List.copyOf(slots);
    }

    private static void addTowerStackSlots(List<MKWorkspaceSlotSchema> slots, String stackId, String regionId) {
        for (MKTowerWorkspaceStackSlot slot : MKTowerWorkspaceStackSlot.schemaOrder()) {
            slots.add(new MKWorkspaceSlotSchema(slot.slotId(stackId), regionId, slot.roleKind(),
                    slot.slotId(stackId), repeatForStackSlot(slot)));
        }
    }

    private static MKWorkspaceSlotSchema.Repeat repeatForStackSlot(MKTowerWorkspaceStackSlot slot) {
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
        roles.add(new MKWorkspaceRoleSchema("keep.walkway.east", "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema("keep.walkway.south", "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema("keep.walkway.west", "linear_run", "walkway",
                false, false, Set.of("open_walkway", "terrain_matched_allowed")));
        roles.add(new MKWorkspaceRoleSchema("keep.gate.main", "entry", "room",
                false, true, Set.of("gate")));
        return List.copyOf(roles);
    }

    private static void addTowerStackRoles(List<MKWorkspaceRoleSchema> roles, String stackId, Set<String> extraTags) {
        for (MKTowerWorkspaceStackSlot slot : MKTowerWorkspaceStackSlot.schemaOrder()) {
            roles.add(towerStackRole(slot.slotId(stackId), slot.roleKind(), slot.pieceKind(), slot.terminal(),
                    slot == MKTowerWorkspaceStackSlot.ENTRY && stackId.equals("keep.center"), extraTags));
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
        SlotAvailability slots = collectAvailableSlots(workspace, perimeterPlan);
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
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun, slots.availableSlots()).stream())
                .forEach(pieces::add);
        pieces.addAll(createPerimeterPieces(workspace, perimeterPlan));
        return List.copyOf(pieces);
    }

    private List<MKPlannedPiece> createCenterStackPieces(MKStructureWorkspace workspace, SlotAvailability slots) {
        List<MKTowerWorkspaceFamilyDefinition> centerFamilies = workspace.familyDefinitions().stream()
                .filter(family -> isActiveKeepSlot(workspace, family.topologySlotId()))
                .filter(family -> isCenterStackSlot(family.topologySlotId()))
                .toList();
        MKWorkspaceTowerStackSettings settings = workspace.topologyProfile().towerStackSettingsOrDefault("keep.center");
        MKTowerStackDefinition stackDefinition = MKTowerStackDefinition.scoped("keep.center", true, settings);
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
        return towerStackPlanner.createRoomPieces(workspace, stackDefinition, centerFamilies).stream()
                .map(piece -> withRoomLayoutConnectors(piece, slots, opening))
                .toList();
    }

    private List<MKPlannedPiece> createCornerStackPieces(MKStructureWorkspace workspace, SlotAvailability slots) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        List<MKTowerWorkspaceFamilyDefinition> sharedFamilies = workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().startsWith("keep.corner.shared."))
                .toList();
        for (String stackId : CONCRETE_CORNER_SLOTS) {
            List<MKTowerWorkspaceFamilyDefinition> stackFamilies;
            if (workspace.topologyProfile().uniqueCornerTower(stackId)) {
                stackFamilies = workspace.familyDefinitions().stream()
                        .filter(family -> family.topologySlotId().startsWith(stackId + "."))
                        .toList();
            } else {
                stackFamilies = sharedFamilies.stream()
                        .map(family -> remapSharedCornerFamily(family, stackId))
                        .toList();
            }
            addCornerStackPieces(pieces, workspace, slots, stackId, stackFamilies);
        }
        return List.copyOf(pieces);
    }

    private void addCornerStackPieces(List<MKPlannedPiece> pieces, MKStructureWorkspace workspace,
                                      SlotAvailability slots, String stackId,
                                      List<MKTowerWorkspaceFamilyDefinition> stackFamilies) {
        if (stackFamilies.isEmpty()) {
            return;
        }
        MKWorkspaceTowerStackSettings settings = workspace.topologyProfile().towerStackSettingsOrDefault(
                workspace.topologyProfile().uniqueCornerTower(stackId) ? stackId : "keep.corner.shared");
        MKTowerStackDefinition stackDefinition = MKTowerStackDefinition.scoped(stackId, false, settings);
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
        towerStackPlanner.createRoomPieces(workspace, stackDefinition, stackFamilies).stream()
                .map(piece -> withRoomLayoutConnectors(piece, slots, opening))
                .forEach(pieces::add);
    }

    private MKTowerWorkspaceFamilyDefinition remapSharedCornerFamily(MKTowerWorkspaceFamilyDefinition family,
                                                                     String targetStackId) {
        String targetBasePrefix = targetStackId.replace('.', '_');
        String baseName = family.baseName().replace("keep_corner_shared", targetBasePrefix);
        String topologySlotId = family.topologySlotId().replace("keep.corner.shared", targetStackId);
        return MKTowerWorkspaceFamilyDefinition.forTopologySlot(
                baseName,
                topologySlotId,
                targetStackId,
                family.supportsVerticalAccess(),
                family.roomWidth(),
                family.roomLength(),
                family.roomHeight(),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private MKPlannedPiece withRoomLayoutConnectors(MKPlannedPiece piece, SlotAvailability slots,
                                                    ResolvedOpeningProfile opening) {
        String topologySlotId = piece.tags().getOrDefault("workspace_topology_slot_id", "");
        List<MKPlannedConnector> layoutConnectors = roomLayoutConnectors(topologySlotId, slots, opening);
        if (layoutConnectors.isEmpty()) {
            return piece;
        }
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>(piece.connectors());
        connectors.addAll(layoutConnectors);
        return new MKPlannedPiece(
                piece.roleId(),
                piece.pieceName(),
                piece.interiorWidth(),
                piece.interiorLength(),
                piece.interiorHeight(),
                List.copyOf(connectors),
                piece.tags()
        );
    }

    private SlotAvailability collectAvailableSlots(MKStructureWorkspace workspace, PerimeterPlan perimeterPlan) {
        LinkedHashSet<String> slots = new LinkedHashSet<>();
        workspace.familyDefinitions().stream()
                .map(MKTowerWorkspaceFamilyDefinition::topologySlotId)
                .filter(MKWalledKeepWorkspacePlanner::isKnownKeepSlot)
                .filter(slot -> isActiveKeepSlot(workspace, slot))
                .forEach(slot -> addAvailableSlot(slots, slot));
        workspace.linearRunFamilies().stream()
                .map(MKWorkspaceLinearRunFamilyDefinition::topologySlotId)
                .filter(MKWalledKeepWorkspacePlanner::isKnownKeepSlot)
                .forEach(slots::add);
        slots.addAll(perimeterPlan.slotIds());
        LinkedHashSet<String> sharedCornerSlots = new LinkedHashSet<>();
        if (slots.contains("keep.corner.shared")) {
            CONCRETE_CORNER_SLOTS.stream()
                    .filter(slot -> !workspace.topologyProfile().uniqueCornerTower(slot))
                    .filter(slot -> !slots.contains(slot))
                    .forEach(sharedCornerSlots::add);
            slots.addAll(sharedCornerSlots);
        }
        return new SlotAvailability(Set.copyOf(slots), Set.copyOf(sharedCornerSlots), perimeterPlan);
    }

    private static void addAvailableSlot(Set<String> slots, String topologySlotId) {
        slots.add(topologySlotId);
        cornerStackIdForSlot(topologySlotId).ifPresent(slots::add);
    }

    private MKPlannedPiece createRoomPiece(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family,
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
        connectors.addAll(roomLayoutConnectors(family.topologySlotId(), slots, opening));
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
                                                       Set<String> availableSlots) {
        if (!linearRun.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
            return List.of();
        }
        ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, linearRun.openingProfileId())
                .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " +
                        linearRun.openingProfileId()));
        DirectionPair directions = directionsForSlot(linearRun.topologySlotId());
        return List.of(new MKPlannedPiece(
                linearRun.topologySlotId(),
                linearRun.linearRunId(),
                directions.eastWest() ? linearRun.length() : linearRun.interiorWidth(),
                directions.eastWest() ? linearRun.interiorWidth() : linearRun.length(),
                linearRun.interiorHeight() + Math.abs(linearRun.slopeDelta()),
                linearRunLayoutConnectors(linearRun, directions, availableSlots, opening),
                buildLinearRunTags(workspace, linearRun)
        ));
    }

    private PerimeterPlan createPerimeterPlan(MKStructureWorkspace workspace) {
        Optional<MKWorkspaceLinearRunFamilyDefinition> south = perimeterFamilyForSide(workspace, "keep.perimeter.south");
        Optional<MKWorkspaceLinearRunFamilyDefinition> west = perimeterFamilyForSide(workspace, "keep.perimeter.west");
        Optional<MKWorkspaceLinearRunFamilyDefinition> north = perimeterFamilyForSide(workspace, "keep.perimeter.north");
        Optional<MKWorkspaceLinearRunFamilyDefinition> east = perimeterFamilyForSide(workspace, "keep.perimeter.east");
        int horizontalSegments = south.map(family -> segmentCountForSpan(family, horizontalPerimeterSpan(workspace)))
                .or(() -> north.map(family -> segmentCountForSpan(family, horizontalPerimeterSpan(workspace))))
                .orElse(0);
        int verticalSegments = west.map(family -> segmentCountForSpan(family, verticalPerimeterSpan(workspace)))
                .or(() -> east.map(family -> segmentCountForSpan(family, verticalPerimeterSpan(workspace))))
                .orElse(0);
        int southWestSegments = horizontalSegments > 0 ? Math.max(1, (int) Math.ceil(horizontalSegments / 2.0)) : 0;
        int southEastSegments = horizontalSegments > 0 ? Math.max(1, horizontalSegments - southWestSegments) : 0;
        return new PerimeterPlan(
                south.map(family -> createPerimeterChain("south_west", "south", family, southWestSegments,
                        true, Direction.EAST, Direction.WEST, "keep.corner.south_west"))
                        .orElse(List.of()),
                west.map(family -> createPerimeterChain("west", "west", family, verticalSegments,
                        false, Direction.SOUTH, Direction.NORTH, "keep.corner.north_west"))
                        .orElse(List.of()),
                north.map(family -> createPerimeterChain("north", "north", family, horizontalSegments,
                        true, Direction.WEST, Direction.EAST, "keep.corner.north_east"))
                        .orElse(List.of()),
                east.map(family -> createPerimeterChain("east", "east", family, verticalSegments,
                        false, Direction.NORTH, Direction.SOUTH, "keep.corner.south_east"))
                        .orElse(List.of()),
                south.map(family -> createPerimeterChain("south_east", "south", family, southEastSegments,
                        true, Direction.EAST, Direction.WEST, null))
                        .orElse(List.of())
        );
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
        return cornerLength(workspace) + DEFAULT_COURTYARD_CLEARANCE + centerLength(workspace) +
                DEFAULT_COURTYARD_CLEARANCE + cornerLength(workspace);
    }

    private int segmentCountForSpan(MKWorkspaceLinearRunFamilyDefinition family, int span) {
        return Math.max(1, (int) Math.ceil(span / (double) Math.max(1, family.length())));
    }

    private int centerWidth(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().towerStackSettingsOrDefault("keep.center").width();
    }

    private int centerLength(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().towerStackSettingsOrDefault("keep.center").length();
    }

    private int cornerWidth(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().towerStackSettingsOrDefault(cornerSettingsSlot(workspace)).width();
    }

    private int cornerLength(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().towerStackSettingsOrDefault(cornerSettingsSlot(workspace)).length();
    }

    private String cornerSettingsSlot(MKStructureWorkspace workspace) {
        return workspace.topologyProfile().anySharedCornerTower() ? "keep.corner.shared" : "keep.corner.north_west";
    }

    private List<MKPlannedPiece> createPerimeterPieces(MKStructureWorkspace workspace, PerimeterPlan perimeterPlan) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        for (PerimeterSegment segment : perimeterPlan.allSegments()) {
            MKWorkspaceLinearRunFamilyDefinition family = segment.family();
            if (!family.supportedShapes().contains(MKWorkspaceLinearRunPieceShape.STRAIGHT)) {
                continue;
            }
            ResolvedOpeningProfile opening = resolveOpeningProfile(workspace, family.openingProfileId())
                    .orElseThrow(() -> new IllegalStateException("missing linear run opening profile " +
                            family.openingProfileId()));
            pieces.add(new MKPlannedPiece(
                    segment.slotId(),
                    segment.pieceName(),
                    segment.eastWest() ? family.length() : family.interiorWidth(),
                    segment.eastWest() ? family.interiorWidth() : family.length(),
                    family.interiorHeight() + Math.abs(family.slopeDelta()),
                    perimeterSegmentConnectors(segment, opening),
                    buildLinearRunTags(workspace, family, segment)
            ));
        }
        return List.copyOf(pieces);
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

    private List<MKPlannedConnector> roomLayoutConnectors(String topologySlotId, SlotAvailability slots,
                                                          ResolvedOpeningProfile opening) {
        ArrayList<MKPlannedConnector> connectors = new ArrayList<>();
        Set<String> availableSlots = slots.availableSlots();
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
                slots.perimeterPlan().firstSouthWest()
                        .ifPresent(segment -> addBranchTargetDirect(connectors, Direction.WEST,
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
                perimeterPlan.firstNorth().ifPresent(segment -> addBranchTargetDirect(connectors, Direction.EAST,
                        segment.slotId(), opening));
            }
            case "keep.corner.north_east" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.WEST, opening);
                perimeterPlan.firstEast().ifPresent(segment -> addBranchTargetDirect(connectors, Direction.SOUTH,
                        segment.slotId(), opening));
            }
            case "keep.corner.south_east" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.NORTH, opening);
                perimeterPlan.firstSouthEast().ifPresent(segment -> addBranchTargetDirect(connectors, Direction.WEST,
                        segment.slotId(), opening));
            }
            case "keep.corner.south_west" -> {
                addIncomingCornerConnector(connectors, cornerSlotId, Direction.EAST, opening);
                perimeterPlan.firstWest().ifPresent(segment -> addBranchTargetDirect(connectors, Direction.NORTH,
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
                EMPTY_POOL, slotPool(cornerSlotId)));
    }

    private void addBranchTarget(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                 Set<String> availableSlots, ResolvedOpeningProfile opening) {
        if (availableSlots.contains(targetSlotId)) {
            addBranchTargetDirect(connectors, facing, targetSlotId, opening);
        }
    }

    private void addBranchTargetDirect(List<MKPlannedConnector> connectors, Direction facing, String targetSlotId,
                                       ResolvedOpeningProfile opening) {
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                opening.openingWidth(), opening.openingHeight(), slotPool(targetSlotId)));
    }

    private Map<String, String> buildRoomTags(MKStructureWorkspace workspace, MKTowerWorkspaceFamilyDefinition family) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put("topology_role", family.topologySlotId());
        tags.put("workspace_topology_slot_id", family.topologySlotId());
        tags.put("workspace_topology_role_id", family.topologySlotId());
        tags.put("tower_piece_kind", "room");
        tags.put("workspace_piece_kind", "instance");
        tags.put("workspace_family_id", family.baseName());
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", family.horizontalExtrusionMode().getSerializedName());
        tags.put("workspace_connector_stitch", "full_face");
        workspace.topologyProfile().towerStackSettings(stackIdForFamily(family))
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
            tags.put("workspace_connector_stitch", "wall_run");
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

    private String stackIdForFamily(MKTowerWorkspaceFamilyDefinition family) {
        if (family.topologySlotId().startsWith("keep.center.")) {
            return "keep.center";
        }
        Optional<String> cornerStackId = cornerStackIdForSlot(family.topologySlotId());
        if (cornerStackId.isPresent()) {
            return cornerStackId.get();
        }
        if ("keep.corner.shared".equals(family.topologySlotId()) ||
                CONCRETE_CORNER_SLOTS.contains(family.topologySlotId())) {
            return family.topologySlotId();
        }
        return "";
    }

    private MKWorkspaceRuntimePieceInfo runtimeInfoForRoom(MKTowerWorkspaceFamilyDefinition family) {
        MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
        boolean start = family.topologySlotId().equals("keep.center.entry");
        return new MKWorkspaceRuntimePieceInfo(start, slotMetadata.jigsawPieceRole(), 0, 0, true, true,
                slotMetadata.terminal(), false, slotMetadata.topologyGroupId(), false, false);
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

    private static boolean isKnownKeepSlot(String topologySlotId) {
        return KNOWN_KEEP_SLOTS.contains(topologySlotId) ||
                topologySlotId.startsWith(PERIMETER_ROOT_SLOT + ".") ||
                isCenterStackSlot(topologySlotId) ||
                isCornerStackSlot(topologySlotId);
    }

    private static boolean isPerimeterRunFamily(MKWorkspaceLinearRunFamilyDefinition linearRun) {
        return linearRun.topologySlotId().equals(PERIMETER_ROOT_SLOT) ||
                linearRun.topologySlotId().startsWith(PERIMETER_ROOT_SLOT + ".");
    }

    private static boolean isCenterStackSlot(String topologySlotId) {
        return topologySlotId.startsWith("keep.center.");
    }

    private static boolean isCornerStackSlot(String topologySlotId) {
        return MKTowerWorkspaceStackSlot.stackIdForTopologySlot(topologySlotId)
                .filter(stackId -> stackId.equals("keep.corner.shared") || CONCRETE_CORNER_SLOTS.contains(stackId))
                .isPresent();
    }

    private static Optional<String> cornerStackIdForSlot(String topologySlotId) {
        if ("keep.corner.shared".equals(topologySlotId) || topologySlotId.startsWith("keep.corner.shared.")) {
            return Optional.of("keep.corner.shared");
        }
        Optional<String> stackSlotId = MKTowerWorkspaceStackSlot.stackIdForTopologySlot(topologySlotId)
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
