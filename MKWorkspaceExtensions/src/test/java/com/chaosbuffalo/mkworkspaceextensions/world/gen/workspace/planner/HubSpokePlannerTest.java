package com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubSpokePlannerTest {
    private final HubSpokePlanner planner = new HubSpokePlanner();

    @Test
    void defaultPlannerProfileUsesFlatHubSpokeTopology() {
        var profile = planner.createDefaultTopologyProfile();

        assertEquals(HubSpokePlanner.PLANNER_ID, profile.plannerId());
        assertEquals(TerrainAdjustment.NONE, profile.terrainAdjustment());
        assertTrue(profile.verticalStackSettings().isEmpty());
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(profile);
        assertEquals(1, settings.spokeTemplates().size());
        assertEquals(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST),
                settings.spokeTemplates().getFirst().validDirections());
        assertTrue(planner.validateTopology(defaultWorkspace()).isEmpty());
        assertTrue(planner.createDefaultLinearRunFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions(),
                MKWorkspaceMaterialPalette.defaultPalette()).isEmpty());
        assertEquals(List.of(
                HubSpokePlanner.CENTER_BASE_NAME,
                HubSpokePlanner.SPOKE_BASE_NAME,
                HubSpokePlanner.CORNER_BASE_NAME
        ), planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()).stream()
                .map(family -> family.baseName())
                .toList());
        assertTrue(planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()).stream()
                .allMatch(family -> family.roomHeight() == HubSpokePlanner.MIN_PLATFORM_HEIGHT));
    }

    @Test
    void topologySchemaExposesSharedAndConcreteCornerSlots() {
        List<String> slots = planner.schema().slots().stream()
                .map(slot -> slot.slotId())
                .toList();

        assertTrue(slots.contains(HubSpokePlanner.CORNER_SLOT));
        assertTrue(slots.containsAll(HubSpokePlanner.concreteCornerSlots()));
    }

    @Test
    void topologySchemaExposesSharedAndConcreteSpokeSlots() {
        List<String> slots = planner.schema().slots().stream()
                .map(slot -> slot.slotId())
                .toList();

        assertTrue(slots.contains(HubSpokePlanner.SPOKE_SLOT));
        assertTrue(slots.containsAll(HubSpokePlanner.concreteSpokeSlots()));
    }

    @Test
    void customSpokeTemplatesExposeOnlySourceSpokeSlots() {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("fire_shrine_tower", "Tower", 15, 30,
                        List.of(Direction.NORTH, Direction.SOUTH)),
                new HubSpokePlannerSettings.SpokeTemplate("fire_shrine_platform", "Platform", 15, 30,
                        List.of(Direction.EAST, Direction.WEST))
        ));

        assertFalse(HubSpokePlanner.usesSharedSpokeSlot(settings));
        assertEquals(List.of("hub_spoke.spoke.north", "hub_spoke.spoke.east"),
                HubSpokePlanner.sourceSpokeSlots(settings));
    }

    @Test
    void spokeTemplatesCanSplitAuthoringSourcesByDirectionMask() {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("hub_spoke_spoke_ns", "North South", 13, 5,
                        List.of(Direction.NORTH, Direction.SOUTH)),
                new HubSpokePlannerSettings.SpokeTemplate("hub_spoke_spoke_ew", "East West", 7, 11,
                        List.of(Direction.EAST, Direction.WEST))
        ));
        MKWorkspaceTopologyProfile profile = settings.applyTo(planner.createDefaultTopologyProfile());
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));

        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(workspace);
        List<MKPlannedPiece> spokes = pieces.stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.SPOKE_SLOT))
                .toList();

        assertEquals(List.of(
                "hub_spoke_spoke_ns_north",
                "hub_spoke_spoke_ew_east",
                "hub_spoke_spoke_ns_south",
                "hub_spoke_spoke_ew_west"
        ), spokes.stream().map(MKPlannedPiece::pieceName).toList());
        assertEquals(List.of(
                "hub_spoke_spoke_ns_north",
                "hub_spoke_spoke_ew_east"
        ), spokes.stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKPlannedPiece::pieceName)
                .toList());
        assertEquals(List.of(5, 11, 5, 11), spokes.stream()
                .map(MKPlannedPiece::interiorHeight)
                .toList());
        assertEquals(List.of(5, 7, 5, 7), spokes.stream()
                .map(MKPlannedPiece::interiorWidth)
                .toList());
        assertEquals(List.of(13, 5, 13, 5), spokes.stream()
                .map(MKPlannedPiece::interiorLength)
                .toList());
        assertTrue(planner.validateTopology(workspace).isEmpty());
    }

    @Test
    void addedAllDirectionSpokeTemplatesStillReceiveAuthoringSources() {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.defaults().withAddedSpokeTemplate();
        MKWorkspaceTopologyProfile profile = settings.applyTo(planner.createDefaultTopologyProfile());
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));

        List<String> authoringSpokes = planner.createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.SPOKE_SLOT))
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKPlannedPiece::pieceName)
                .toList();

        assertEquals(List.of("hub_spoke_spoke_north", "hub_spoke_spoke_2_east"), authoringSpokes);
        assertTrue(planner.validateTopology(workspace).isEmpty());
    }

    @Test
    void singleSpokeDirectionGeneratesItsTwoAnchoredCorners() {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("hub_spoke_spoke_north", "North Only", 9, 3,
                        List.of(Direction.NORTH))
        ));
        MKWorkspaceTopologyProfile profile = settings.applyTo(planner.createDefaultTopologyProfile());
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));

        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(workspace);

        assertTrue(planner.validateTopology(workspace).isEmpty());
        assertEquals(4, pieces.size());
        assertEquals(List.of(
                HubSpokePlanner.CENTER_BASE_NAME,
                "hub_spoke_spoke_north_north",
                "hub_spoke_corner_north_west",
                "hub_spoke_corner_north_east"
        ), pieces.stream().map(MKPlannedPiece::pieceName).toList());
        assertEquals(3, pieces.getFirst().connectors().size());
        assertEquals(List.of(
                "hub_spoke_slots/hub_spoke/spoke/north",
                "hub_spoke_slots/hub_spoke/corner/north_west",
                "hub_spoke_slots/hub_spoke/corner/north_east"
        ), pieces.getFirst().connectors().stream()
                .map(connector -> connector.targetPoolName())
                .toList());
    }

    @Test
    void eastWestOnlySpokesAnchorAllCornersOnEastWestAxis() {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("hub_spoke_spoke_ew", "East West", 9, 3,
                        List.of(Direction.EAST, Direction.WEST))
        ));
        MKWorkspaceTopologyProfile profile = settings.applyTo(planner.createDefaultTopologyProfile());
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));

        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(workspace);
        MKPlannedPiece center = pieces.getFirst();

        assertEquals(List.of(
                HubSpokePlanner.CENTER_BASE_NAME,
                "hub_spoke_spoke_ew_east",
                "hub_spoke_spoke_ew_west",
                "hub_spoke_corner_north_west",
                "hub_spoke_corner_north_east",
                "hub_spoke_corner_south_east",
                "hub_spoke_corner_south_west"
        ), pieces.stream().map(MKPlannedPiece::pieceName).toList());
        assertEquals(List.of(
                Direction.EAST,
                Direction.WEST,
                Direction.WEST,
                Direction.EAST,
                Direction.EAST,
                Direction.WEST
        ), center.connectors().stream()
                .map(connector -> connector.facing())
                .toList());
    }

    @Test
    void northSouthCornerAnchorsTakePriorityOverEastWestAnchors() {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("hub_spoke_spoke_new", "North East West", 9, 3,
                        List.of(Direction.NORTH, Direction.EAST, Direction.WEST))
        ));
        MKWorkspaceTopologyProfile profile = settings.applyTo(planner.createDefaultTopologyProfile());
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));

        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(workspace);

        assertEquals(List.of(
                HubSpokePlanner.CENTER_BASE_NAME,
                "hub_spoke_spoke_new_north",
                "hub_spoke_spoke_new_east",
                "hub_spoke_spoke_new_west",
                "hub_spoke_corner_north_west",
                "hub_spoke_corner_north_east"
        ), pieces.stream().map(MKPlannedPiece::pieceName).toList());
        assertEquals(2, pieces.stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.CORNER_SLOT))
                .count());
    }

    @Test
    void modelAndRegistryValidationIgnoreObsoleteHubSpokeSyntheticStack() {
        MKWorkspaceVerticalStackSettings obsoleteInvalidStack = new MKWorkspaceVerticalStackSettings(
                HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID, 1, 0, 200, 15, 15, false, false);
        MKWorkspaceTopologyProfile profile = planner.createDefaultTopologyProfile()
                .withVerticalStackSettings(obsoleteInvalidStack);
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));
        MKWorkspacePlannerRegistry registry = new MKWorkspacePlannerRegistry();
        registry.register(planner);

        assertFalse(workspace.validate().stream()
                .anyMatch(error -> error.startsWith("vertical stack " + HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID)));
        assertFalse(registry.validate(workspace).stream()
                .anyMatch(error -> error.startsWith("vertical stack " + HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID)));
    }

    @Test
    void canonicalPiecesCollapseToThreeAuthoringTemplates() {
        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(defaultWorkspace());

        assertEquals(9, pieces.size());
        assertEquals(HubSpokePlanner.authoringTemplateBaseNames(), pieces.stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKPlannedPiece::pieceName)
                .toList());
        assertEquals(List.of(
                "hub_spoke.center",
                "hub_spoke.spoke.north",
                "hub_spoke.spoke.east",
                "hub_spoke.spoke.south",
                "hub_spoke.spoke.west",
                "hub_spoke.corner.north_west",
                "hub_spoke.corner.north_east",
                "hub_spoke.corner.south_east",
                "hub_spoke.corner.south_west"
        ), pieces.stream()
                .map(piece -> piece.tags().get(HubSpokePlanner.HUB_SPOKE_CONCRETE_SLOT_TAG))
                .toList());
    }

    @Test
    void hubOwnsAllSpokeAndCornerBranchConnectors() {
        MKPlannedPiece center = planner.createCanonicalPieces(defaultWorkspace()).getFirst();

        assertEquals(HubSpokePlanner.CENTER_BASE_NAME, center.pieceName());
        assertEquals(8, center.connectors().size());
        assertEquals(List.of(
                "hub_spoke_slots/hub_spoke/spoke/north",
                "hub_spoke_slots/hub_spoke/spoke/east",
                "hub_spoke_slots/hub_spoke/spoke/south",
                "hub_spoke_slots/hub_spoke/spoke/west",
                "hub_spoke_slots/hub_spoke/corner/north_west",
                "hub_spoke_slots/hub_spoke/corner/north_east",
                "hub_spoke_slots/hub_spoke/corner/south_east",
                "hub_spoke_slots/hub_spoke/corner/south_west"
        ), center.connectors().stream()
                .map(connector -> connector.targetPoolName())
                .collect(Collectors.toList()));
        assertEquals(4, center.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.MAIN_BACK)
                .count());
        assertEquals(4, center.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH)
                .count());
        assertEquals(List.of(-6, 6, 6, -6), center.connectors().stream()
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH)
                .map(connector -> connector.lateralOffset())
                .toList());
    }

    @Test
    void perimeterPiecesUseMainForwardSpokesAndBranchCapCornersForPreviewPlacement() {
        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(defaultWorkspace()).stream()
                .filter(piece -> !piece.roleId().equals(HubSpokePlanner.CENTER_SLOT))
                .toList();

        assertEquals(8, pieces.size());
        assertTrue(pieces.stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.SPOKE_SLOT))
                .flatMap(piece -> piece.connectors().stream())
                .filter(connector -> connector.incomingPoolName() != null)
                .filter(connector -> !connector.incomingPoolName().equals("minecraft:empty"))
                .allMatch(connector -> connector.role() == MKConnectorRole.MAIN_FORWARD));
        assertTrue(pieces.stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.CORNER_SLOT))
                .flatMap(piece -> piece.connectors().stream())
                .filter(connector -> connector.incomingPoolName() != null)
                .filter(connector -> !connector.incomingPoolName().equals("minecraft:empty"))
                .allMatch(connector -> connector.role() == MKConnectorRole.BRANCH));
    }

    @Test
    void flatPlatformConnectorsFitMinimumPieceHeight() {
        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(defaultWorkspace());

        assertTrue(pieces.stream()
                .flatMap(piece -> piece.connectors().stream()
                        .map(connector -> (connector.verticalOffset() - 1) + connector.openingHeight()
                                <= piece.interiorHeight()))
                .allMatch(Boolean::booleanValue));
        assertTrue(pieces.stream()
                .flatMap(piece -> piece.connectors().stream())
                .allMatch(connector -> connector.verticalOffset() >= 1));
    }

    @Test
    void cornerBranchesAreTerminalsAndUseCroppedRotatedTemplateReuse() {
        List<MKPlannedPiece> corners = planner.createCanonicalPieces(defaultWorkspace()).stream()
                .filter(piece -> "corner".equals(piece.tags().get(HubSpokePlanner.FLAT_PLATFORM_KIND_TAG)) ||
                        "corner_chamfer".equals(piece.tags().get(HubSpokePlanner.FLAT_PLATFORM_KIND_TAG)))
                .toList();

        assertEquals(4, corners.size());
        for (MKPlannedPiece corner : corners) {
            assertEquals("true", corner.tags().get("runtime_terminal"));
            assertEquals("true", corner.tags().get("runtime_branch_cap"));
            assertEquals(MKWorkspaceTemplateReuseTags.CROP_MODE_NON_STRUCTURE_VOID,
                    corner.tags().get(MKWorkspaceTemplateReuseTags.CROP_MODE_TAG));
            assertEquals(HubSpokePlanner.CORNER_BASE_NAME + "_north_west",
                    MKWorkspaceTemplateReuseTags.sourceId(corner.tags()));
        }
        assertFalse(MKWorkspaceTemplateReuseTags.isDerived(corners.getFirst().tags()));
        assertTrue(corners.stream().skip(1).allMatch(piece -> MKWorkspaceTemplateReuseTags.isDerived(piece.tags())));
    }

    @Test
    void pairedCornerModeUsesTwoDiagonalAuthoringTemplates() {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(
                HubSpokePlannerSettings.defaults().spokeTemplates(),
                HubSpokePlannerSettings.CornerTemplateMode.PAIRED);
        MKWorkspaceTopologyProfile profile = settings.applyTo(planner.createDefaultTopologyProfile());
        MKStructureWorkspace workspace = workspaceWithProfileAndFamilies(profile,
                planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));

        List<MKPlannedPiece> corners = planner.createCanonicalPieces(workspace).stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.CORNER_SLOT))
                .toList();

        assertEquals(List.of(
                "hub_spoke_corner_north_west",
                "hub_spoke_corner_north_east"
        ), corners.stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKPlannedPiece::pieceName)
                .toList());
        assertEquals(List.of(
                "hub_spoke_corner_north_west",
                "hub_spoke_corner_north_east",
                "hub_spoke_corner_north_west",
                "hub_spoke_corner_north_east"
        ), corners.stream()
                .map(piece -> piece.tags().get(MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG))
                .toList());
        assertEquals(List.of(
                MKWorkspaceTemplateReuseTags.ROTATION_NONE,
                MKWorkspaceTemplateReuseTags.ROTATION_NONE,
                MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180,
                MKWorkspaceTemplateReuseTags.ROTATION_CLOCKWISE_180
        ), corners.stream()
                .map(piece -> piece.tags().get(MKWorkspaceTemplateReuseTags.ROTATION_TAG))
                .toList());
    }

    @Test
    void fullWidthSpokesMoveCornerBranchesOntoSpokeAttachments() {
        MKStructureWorkspace workspace = workspaceWithFamilies(
                HubSpokePlanner.defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()).stream()
                        .map(family -> {
                            if (family.topologySlotId().equals(HubSpokePlanner.SPOKE_SLOT)) {
                                return copyFamily(family, 15, 9, family.roomHeight());
                            }
                            if (family.topologySlotId().equals(HubSpokePlanner.CORNER_SLOT)) {
                                return copyFamily(family, 3, 3, family.roomHeight());
                            }
                            return family;
                        })
                        .toList());
        List<MKPlannedPiece> pieces = planner.createCanonicalPieces(workspace);
        MKPlannedPiece center = pieces.getFirst();
        List<MKPlannedPiece> spokes = pieces.stream()
                .filter(piece -> piece.roleId().equals(HubSpokePlanner.SPOKE_SLOT))
                .toList();

        assertEquals(HubSpokePlanner.CornerAttachmentMode.SPOKE,
                HubSpokePlanner.cornerAttachmentMode(15, 15, 3));
        assertEquals(4, center.connectors().size());
        assertEquals(4, spokes.stream()
                .flatMap(piece -> piece.connectors().stream())
                .filter(connector -> connector.targetPoolName() != null)
                .filter(connector -> connector.targetPoolName().contains("corner"))
                .filter(connector -> connector.role() == MKConnectorRole.BRANCH)
                .count());
        assertEquals(List.of(
                "hub_spoke_slots/hub_spoke/corner/north_west",
                "hub_spoke_slots/hub_spoke/corner/north_east",
                "hub_spoke_slots/hub_spoke/corner/south_west",
                "hub_spoke_slots/hub_spoke/corner/south_east"
        ), spokes.stream()
                .flatMap(piece -> piece.connectors().stream())
                .filter(connector -> connector.targetPoolName() != null)
                .filter(connector -> connector.targetPoolName().contains("corner"))
                .map(connector -> connector.targetPoolName())
                .toList());
        assertEquals(List.of(3, 3, -3, -3), spokes.stream()
                .flatMap(piece -> piece.connectors().stream())
                .filter(connector -> connector.targetPoolName() != null)
                .filter(connector -> connector.targetPoolName().contains("corner"))
                .map(connector -> connector.lateralOffset())
                .toList());
        assertEquals(0, spokes.stream()
                .filter(piece -> piece.pieceName().endsWith("_east") || piece.pieceName().endsWith("_west"))
                .flatMap(piece -> piece.connectors().stream())
                .filter(connector -> connector.targetPoolName() != null)
                .filter(connector -> connector.targetPoolName().contains("corner"))
                .count());
    }

    private MKStructureWorkspace defaultWorkspace() {
        return workspaceWithFamilies(planner.createDefaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()));
    }

    private MKStructureWorkspace workspaceWithFamilies(List<MKWorkspaceRoomFamilyDefinition> families) {
        return workspaceWithProfileAndFamilies(planner.createDefaultTopologyProfile(), families);
    }

    private MKStructureWorkspace workspaceWithProfileAndFamilies(MKWorkspaceTopologyProfile profile,
                                                                 List<MKWorkspaceRoomFamilyDefinition> families) {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        MKWorkspaceMaterialPalette palette = MKWorkspaceMaterialPalette.defaultPalette();
        long now = 1L;
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                BlockPos.ZERO,
                "mkdev",
                "hub_spoke_workspace",
                profile,
                dimensions,
                palette,
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                1,
                2,
                2,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                families,
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                List.of(),
                now,
                now,
                List.of(),
                List.of()
        );
    }

    private MKWorkspaceRoomFamilyDefinition copyFamily(MKWorkspaceRoomFamilyDefinition family, int width, int length,
                                                       int height) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                width,
                length,
                height,
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }
}
