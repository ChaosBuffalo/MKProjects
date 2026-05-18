package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteResolver;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
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

    private record SlotAvailability(Set<String> availableSlots, Set<String> sharedCornerSlots) {
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
                        new MKWorkspaceLinkSchema("keep.perimeter.north", "keep.corner.north_west.entry", "keep.corner.north_east.entry", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.perimeter.east", "keep.corner.north_east.entry", "keep.corner.south_east.entry", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.perimeter.south", "keep.corner.south_west.entry", "keep.corner.south_east.entry", "linear_run"),
                        new MKWorkspaceLinkSchema("keep.perimeter.west", "keep.corner.north_west.entry", "keep.corner.south_west.entry", "linear_run")
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
        slots.add(new MKWorkspaceSlotSchema("keep.perimeter.north", "keep.perimeter_runs", "defensive_run",
                "keep.perimeter.north", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.perimeter.east", "keep.perimeter_runs", "defensive_run",
                "keep.perimeter.east", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.perimeter.south", "keep.perimeter_runs", "defensive_run",
                "keep.perimeter.south", MKWorkspaceSlotSchema.Repeat.DERIVED));
        slots.add(new MKWorkspaceSlotSchema("keep.perimeter.west", "keep.perimeter_runs", "defensive_run",
                "keep.perimeter.west", MKWorkspaceSlotSchema.Repeat.DERIVED));
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
        roles.add(new MKWorkspaceRoleSchema("keep.perimeter.north", "linear_run", "defensive_run",
                false, false, Set.of("solid_wall", "parapet")));
        roles.add(new MKWorkspaceRoleSchema("keep.perimeter.east", "linear_run", "defensive_run",
                false, false, Set.of("solid_wall", "parapet")));
        roles.add(new MKWorkspaceRoleSchema("keep.perimeter.south", "linear_run", "defensive_run",
                false, false, Set.of("solid_wall", "parapet")));
        roles.add(new MKWorkspaceRoleSchema("keep.perimeter.west", "linear_run", "defensive_run",
                false, false, Set.of("solid_wall", "parapet")));
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
        SlotAvailability slots = collectAvailableSlots(workspace);
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
                .flatMap(linearRun -> createLinearRunPieces(workspace, linearRun, slots.availableSlots()).stream())
                .forEach(pieces::add);
        return List.copyOf(pieces);
    }

    private List<MKPlannedPiece> createCenterStackPieces(MKStructureWorkspace workspace, SlotAvailability slots) {
        List<MKTowerWorkspaceFamilyDefinition> centerFamilies = workspace.familyDefinitions().stream()
                .filter(family -> isActiveKeepSlot(workspace, family.topologySlotId()))
                .filter(family -> isCenterStackSlot(family.topologySlotId()))
                .toList();
        MKWorkspaceTowerStackSettings settings = workspace.topologyProfile().towerStackSettingsOrDefault("keep.center");
        MKTowerStackDefinition stackDefinition = MKTowerStackDefinition.scoped("keep.center",
                floorSettingsForStack(settings), true, settings);
        ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
        return towerStackPlanner.createRoomPieces(workspace, stackDefinition, centerFamilies).stream()
                .map(piece -> withRoomLayoutConnectors(piece, slots, opening))
                .toList();
    }

    private List<MKPlannedPiece> createCornerStackPieces(MKStructureWorkspace workspace, SlotAvailability slots) {
        ArrayList<MKPlannedPiece> pieces = new ArrayList<>();
        for (String stackId : activeCornerStackIds(workspace)) {
            List<MKTowerWorkspaceFamilyDefinition> stackFamilies = workspace.familyDefinitions().stream()
                    .filter(family -> family.topologySlotId().startsWith(stackId + "."))
                    .toList();
            MKWorkspaceTowerStackSettings settings = workspace.topologyProfile().towerStackSettingsOrDefault(stackId);
            MKTowerStackDefinition stackDefinition = MKTowerStackDefinition.scoped(stackId,
                    floorSettingsForStack(settings), false, settings);
            ResolvedOpeningProfile opening = defaultOpeningProfile(workspace);
            towerStackPlanner.createRoomPieces(workspace, stackDefinition, stackFamilies).stream()
                    .map(piece -> withRoomLayoutConnectors(piece, slots, opening))
                    .forEach(pieces::add);
        }
        return List.copyOf(pieces);
    }

    private List<String> activeCornerStackIds(MKStructureWorkspace workspace) {
        ArrayList<String> stackIds = new ArrayList<>();
        if (workspace.topologyProfile().anySharedCornerTower()) {
            stackIds.add("keep.corner.shared");
        }
        CONCRETE_CORNER_SLOTS.stream()
                .filter(workspace.topologyProfile()::uniqueCornerTower)
                .forEach(stackIds::add);
        return List.copyOf(stackIds);
    }

    private MKTowerWorkspaceFloorSettings floorSettingsForStack(MKWorkspaceTowerStackSettings settings) {
        return new MKTowerWorkspaceFloorSettings(
                settings.mainFloors(),
                settings.basementFloors(),
                settings.topCapApproachEnabled(),
                settings.basementCapApproachEnabled()
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
                piece.role(),
                piece.pieceName(),
                piece.interiorWidth(),
                piece.interiorLength(),
                piece.interiorHeight(),
                List.copyOf(connectors),
                piece.tags()
        );
    }

    private SlotAvailability collectAvailableSlots(MKStructureWorkspace workspace) {
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
        LinkedHashSet<String> sharedCornerSlots = new LinkedHashSet<>();
        if (slots.contains("keep.corner.shared")) {
            CONCRETE_CORNER_SLOTS.stream()
                    .filter(slot -> !workspace.topologyProfile().uniqueCornerTower(slot))
                    .filter(slot -> !slots.contains(slot))
                    .forEach(sharedCornerSlots::add);
            slots.addAll(sharedCornerSlots);
        }
        return new SlotAvailability(Set.copyOf(slots), Set.copyOf(sharedCornerSlots));
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
                resolvedFamily.slotMetadata().pieceRole(),
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
                MKWorkspacePieceRole.HALLWAY,
                linearRun.linearRunId(),
                directions.eastWest() ? linearRun.length() : linearRun.interiorWidth(),
                directions.eastWest() ? linearRun.interiorWidth() : linearRun.length(),
                linearRun.interiorHeight() + Math.abs(linearRun.slopeDelta()),
                linearRunLayoutConnectors(linearRun, directions, availableSlots, opening),
                buildLinearRunTags(workspace, linearRun)
        ));
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
                addBranchTarget(connectors, Direction.WEST, "keep.perimeter.south", availableSlots, opening);
                addBranchTarget(connectors, Direction.EAST, "keep.perimeter.south", availableSlots, opening);
            }
            case "keep.corner.shared" -> addSharedCornerConnectors(connectors, slots.sharedCornerSlots(),
                    availableSlots, opening);
            case "keep.corner.shared.entry" -> addSharedCornerConnectors(connectors, slots.sharedCornerSlots(),
                    availableSlots, opening);
            case "keep.corner.north_west", "keep.corner.north_east", "keep.corner.south_east",
                 "keep.corner.south_west" -> addConcreteCornerConnectors(connectors, topologySlotId, availableSlots,
                    opening);
            case "keep.corner.north_west.entry", "keep.corner.north_east.entry", "keep.corner.south_east.entry",
                 "keep.corner.south_west.entry" -> addConcreteCornerConnectors(connectors,
                    topologySlotId.substring(0, topologySlotId.length() - ".entry".length()), availableSlots,
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
                                           Set<String> availableSlots,
                                           ResolvedOpeningProfile opening) {
        if (sharedCornerSlots.contains("keep.corner.north_west")) {
            addCornerEntryConnector(connectors, "keep.corner.north_west", Direction.EAST, "keep.perimeter.north",
                    availableSlots, opening);
        }
        if (sharedCornerSlots.contains("keep.corner.north_east")) {
            addCornerEntryConnector(connectors, "keep.corner.north_east", Direction.WEST, "keep.perimeter.north",
                    availableSlots, opening);
        }
        if (sharedCornerSlots.contains("keep.corner.south_east")) {
            addCornerEntryConnector(connectors, "keep.corner.south_east", Direction.WEST, "keep.perimeter.south",
                    availableSlots, opening);
        }
        if (sharedCornerSlots.contains("keep.corner.south_west")) {
            addCornerEntryConnector(connectors, "keep.corner.south_west", Direction.EAST, "keep.perimeter.south",
                    availableSlots, opening);
        }
    }

    private void addConcreteCornerConnectors(List<MKPlannedConnector> connectors, String cornerSlotId,
                                             Set<String> availableSlots, ResolvedOpeningProfile opening) {
        switch (cornerSlotId) {
            case "keep.corner.north_west" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.EAST, "keep.perimeter.north",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.SOUTH, "keep.perimeter.west", availableSlots, opening);
            }
            case "keep.corner.north_east" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.WEST, "keep.perimeter.north",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.SOUTH, "keep.perimeter.east", availableSlots, opening);
            }
            case "keep.corner.south_east" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.WEST, "keep.perimeter.south",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.NORTH, "keep.perimeter.east", availableSlots, opening);
            }
            case "keep.corner.south_west" -> {
                addCornerEntryConnector(connectors, cornerSlotId, Direction.EAST, "keep.perimeter.south",
                        availableSlots, opening);
                addBranchTarget(connectors, Direction.NORTH, "keep.perimeter.west", availableSlots, opening);
            }
            default -> {
            }
        }
    }

    private void addCornerEntryConnector(List<MKPlannedConnector> connectors, String cornerSlotId, Direction facing,
                                         String targetPerimeterSlotId, Set<String> availableSlots,
                                         ResolvedOpeningProfile opening) {
        connectors.add(new MKPlannedConnector(MKConnectorRole.BRANCH, facing,
                opening.openingWidth(), opening.openingHeight(), 0, 0,
                poolOrEmpty(targetPerimeterSlotId, availableSlots), slotPool(cornerSlotId)));
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
        tags.put("workspace_horizontal_exits", family.horizontalExitSummary());
        tags.put("workspace_horizontal_extrusion_mode", family.horizontalExtrusionMode().getSerializedName());
        workspace.topologyProfile().towerStackSettings(stackIdForFamily(family))
                .ifPresent(settings -> {
                    tags.put("workspace_tower_stack_id", settings.stackId());
                    tags.put("workspace_tower_stack_main_floors", Integer.toString(settings.mainFloors()));
                    tags.put("workspace_tower_stack_basement_floors", Integer.toString(settings.basementFloors()));
                });
        MKWorkspaceResolvedFamilySettings resolvedFamily = workspace.resolveFamilySettings(family);
        tags.put("workspace_category", resolvedFamily.slotMetadata().category().getSerializedName());
        applyFoundationTags(resolvedFamily.foundationPolicy(), tags);
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, Boolean.toString(family.supportsVerticalAccess()));
        if (family.supportsVerticalAccess()) {
            tags.put("workspace_vertical_access_group_id", family.verticalAccessGroupId());
            tags.put(MKWorkspaceVerticalAccessTags.PLACEMENT_TAG, workspace.verticalAccessSpec().placement().getSerializedName());
            tags.put(MKWorkspaceVerticalAccessTags.DIRECTION_TAG, verticalAccessDirectionTag(family));
        }
        runtimeInfoForRoom(family).applyToTags(tags);
        MKWorkspacePaletteTags.apply(tags, resolvedFamily.palette());
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
                slotMetadata.terminal(), false, slotMetadata.category().getSerializedName(), false, false);
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
                isCenterStackSlot(topologySlotId) ||
                isCornerStackSlot(topologySlotId);
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
