package com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceDraftSession;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerClientContributor;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerClientRegistry;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspacePlannerRegistry;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateCloneTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubSpokeWorkspaceDraftAdapterTest {
    private final HubSpokePlanner planner = new HubSpokePlanner();

    @Test
    void templateFamiliesAddSpokeFamilyUpdatesPlannerSettingsAndCatalog() {
        registerHubSpokeClient();
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, fireShrineLikeWorkspace(), List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspaceSlotSchema eastSpokeSlot = session.roomTopologySlots().stream()
                .filter(slot -> "hub_spoke.spoke.east".equals(slot.slotId()))
                .findFirst()
                .orElseThrow();

        int addedIndex = session.addFamilyDefinition(eastSpokeSlot);
        MKStructureWorkspace requested = session.buildWorkspaceDraft();

        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(requested.topologyProfile());
        assertEquals(3, settings.spokeTemplates().size());
        assertEquals("fire_shrine_platform_2", settings.spokeTemplates().get(2).baseName());
        assertTrue(addedIndex >= 0);
        assertEquals("hub_spoke.spoke.east", session.familyDefinitions().get(addedIndex).topologySlotId());
        assertEquals(List.of(
                "fire_shrine_tower_north",
                "fire_shrine_platform_east",
                "fire_shrine_platform_2_east"
        ), authoredSpokePieceNames(requested));
    }

    @Test
    void templateFamiliesRemoveSpokeFamilyUpdatesPlannerSettingsAndCatalog() {
        registerHubSpokeClient();
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, fireShrineLikeWorkspace(), List.of());
        WorkspaceDraftSession session = screen.draftSession();
        int eastSpokeFamilyIndex = session.familyIndexesForTopologySlot("hub_spoke.spoke.east").getFirst();

        session.removeFamilyDefinition(eastSpokeFamilyIndex);
        MKStructureWorkspace requested = session.buildWorkspaceDraft();

        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(requested.topologyProfile());
        assertEquals(1, settings.spokeTemplates().size());
        assertEquals("fire_shrine_tower", settings.spokeTemplates().getFirst().baseName());
        assertEquals(List.of("fire_shrine_tower_north"), authoredSpokePieceNames(requested));
    }

    @Test
    void templateFamiliesAddCornerFamilyCreatesAuthoredTemplate() {
        registerHubSpokeClient();
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, fireShrineLikeWorkspace(), List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspaceSlotSchema northWestCornerSlot = session.roomTopologySlots().stream()
                .filter(slot -> "hub_spoke.corner.north_west".equals(slot.slotId()))
                .findFirst()
                .orElseThrow();

        int addedIndex = session.addFamilyDefinition(northWestCornerSlot);
        MKStructureWorkspace requested = session.buildWorkspaceDraft();
        MKWorkspaceRoomFamilyDefinition added = session.familyDefinitions().get(addedIndex);

        assertEquals("family_1", added.baseName());
        assertEquals("hub_spoke.corner.north_west", added.topologySlotId());
        assertTrue(authoredCornerPieceNames(requested).contains("family_1"));
        assertEquals(List.of(
                "hub_spoke_corner_north_west",
                "family_1"
        ), cornerChildrenForIncomingPool(requested, "hub_spoke_slots/hub_spoke/corner/north_west"));
        assertEquals(List.of(
                "hub_spoke_corner_south_east",
                "family_1_south_east"
        ), cornerChildrenForIncomingPool(requested, "hub_spoke_slots/hub_spoke/corner/south_east"));
    }

    @Test
    void templateFamiliesAddSpokeFamilyFromPieceTagsNewPlannerTemplateWithCloneSource() {
        registerHubSpokeClient();
        MKStructureWorkspace workspace = fireShrineLikeWorkspace();
        MKWorkspaceScreen screen = new MKWorkspaceScreen(BlockPos.ZERO, workspace, List.of());
        WorkspaceDraftSession session = screen.draftSession();
        MKWorkspacePieceDefinition sourcePiece = hubSpokePiece(
                workspace.id(),
                "fire_shrine_platform_east_1",
                "fire_shrine_platform_east",
                "fire_shrine_platform_east",
                "hub_spoke.spoke.east",
                HubSpokePlanner.SPOKE_SLOT,
                Direction.EAST,
                1,
                15,
                11,
                30);

        WorkspaceDraftSession.TemplateFamilyCreationSelection selection =
                session.addFamilyDefinitionFromPiece(sourcePiece).orElseThrow();
        int addedIndex = Integer.parseInt(selection.editId().substring("family:".length()));
        MKWorkspaceRoomFamilyDefinition added = session.familyDefinitions().get(addedIndex);
        MKStructureWorkspace requested = session.buildWorkspaceDraft();

        assertEquals("fire_shrine_platform_east_1", added.templateCloneSourcePieceName());
        assertTrue(planner.createCanonicalPieces(requested).stream()
                        .filter(piece -> HubSpokePlanner.SPOKE_SLOT.equals(piece.roleId()))
                        .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                        .anyMatch(piece -> "fire_shrine_platform_east_1".equals(piece.tags()
                                .get(MKWorkspaceTemplateCloneTags.SOURCE_PIECE_NAME_TAG))),
                () -> "new authored spoke template should carry clone source");
    }

    private List<String> authoredSpokePieceNames(MKStructureWorkspace workspace) {
        return planner.createCanonicalPieces(workspace).stream()
                .filter(piece -> HubSpokePlanner.SPOKE_SLOT.equals(piece.roleId()))
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKPlannedPiece::pieceName)
                .toList();
    }

    private List<String> authoredCornerPieceNames(MKStructureWorkspace workspace) {
        return planner.createCanonicalPieces(workspace).stream()
                .filter(piece -> HubSpokePlanner.CORNER_SLOT.equals(piece.roleId()))
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .map(MKPlannedPiece::pieceName)
                .toList();
    }

    private List<String> cornerChildrenForIncomingPool(MKStructureWorkspace workspace, String poolName) {
        return planner.createCanonicalPieces(workspace).stream()
                .filter(piece -> HubSpokePlanner.CORNER_SLOT.equals(piece.roleId()))
                .filter(piece -> piece.connectors().stream()
                        .anyMatch(connector -> poolName.equals(connector.incomingPoolName())))
                .map(MKPlannedPiece::pieceName)
                .toList();
    }

    private MKStructureWorkspace fireShrineLikeWorkspace() {
        MKWorkspaceDimensions dimensions = new MKWorkspaceDimensions(11, 11, 30, 30, 30, 3, 3, 2);
        MKWorkspaceTopologyProfile topologyProfile = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate("fire_shrine_tower", "Tower", 15, 30,
                        List.of(Direction.NORTH, Direction.SOUTH)),
                new HubSpokePlannerSettings.SpokeTemplate("fire_shrine_platform", "Platform", 15, 30,
                        List.of(Direction.EAST, Direction.WEST))
        ), HubSpokePlannerSettings.CornerTemplateMode.PAIRED).applyTo(new MKWorkspaceTopologyProfile(
                HubSpokePlanner.PLANNER_ID,
                List.of(),
                TerrainAdjustment.NONE
        ));
        return new MKStructureWorkspace(
                UUID.nameUUIDFromBytes("mkultra:fire_shrine_new".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                BlockPos.ZERO,
                "mkultra",
                "fire_shrine_new",
                topologyProfile,
                dimensions,
                MKWorkspaceMaterialPalette.defaultPalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                0,
                2,
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                List.of(
                        family(HubSpokePlanner.CENTER_BASE_NAME, HubSpokePlanner.CENTER_SLOT, "hub", "room", false,
                                11, 11, 30),
                        family(HubSpokePlanner.SPOKE_BASE_NAME, HubSpokePlanner.SPOKE_SLOT, "spoke", "room", false,
                                11, 15, 30),
                        family("fire_shrine_tower_north", "hub_spoke.spoke.north", "spoke", "room", false,
                                11, 15, 30),
                        family("fire_shrine_platform_east", "hub_spoke.spoke.east", "spoke", "room", false,
                                15, 11, 30),
                        family(HubSpokePlanner.CORNER_BASE_NAME, HubSpokePlanner.CORNER_SLOT, "corner", "room", true,
                                15, 15, 30),
                        family("hub_spoke_corner_north_west", "hub_spoke.corner.north_west", "corner", "room", true,
                                15, 15, 30),
                        family("hub_spoke_corner_north_east", "hub_spoke.corner.north_east", "corner", "room", true,
                                15, 15, 30)
                ),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                0,
                0,
                List.of(),
                List.of()
        );
    }

    private MKWorkspaceRoomFamilyDefinition family(String baseName, String topologySlotId, String topologyGroup,
                                                   String pieceKind, boolean terminal, int width, int length,
                                                   int height) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                baseName,
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, topologyGroup, pieceKind, terminal),
                "",
                false,
                width,
                length,
                height,
                com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode
                        .NO_EXTRUSION,
                List.of(),
                0,
                0,
                null,
                null
        );
    }

    private MKWorkspacePieceDefinition hubSpokePiece(UUID workspaceId, String pieceName, String baseName,
                                                     String familyId, String concreteSlotId, String archetypeSlotId,
                                                     Direction direction, int variantIndex, int width, int length,
                                                     int height) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspaceId,
                pieceName,
                archetypeSlotId,
                variantIndex,
                new MKWorkspaceDimensions(width, length, height, height, height, 3, 3, 2),
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, width - 1, height - 1, length - 1),
                new BoundingBox(0, 0, 0, width - 1, height - 1, length - 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                Map.of(
                        "workspace_piece_kind", "instance",
                        "workspace_topology_slot_id", concreteSlotId,
                        "workspace_family_id", familyId,
                        "workspace_base_name", baseName,
                        HubSpokePlanner.HUB_SPOKE_ARCHETYPE_SLOT_TAG, archetypeSlotId,
                        HubSpokePlanner.HUB_SPOKE_CONCRETE_SLOT_TAG, concreteSlotId,
                        HubSpokePlanner.HUB_SPOKE_DIRECTION_TAG, direction.getSerializedName()
                )
        );
    }

    private static void registerHubSpokeClient() {
        MKWorkspacePlannerRegistry.registerShared(new HubSpokePlanner());
        WorkspacePlannerClientRegistry.register(new WorkspacePlannerClientRegistry.PlannerClientDefinition() {
            @Override
            public ResourceLocation getPlannerId() {
                return HubSpokePlanner.PLANNER_ID;
            }

            @Override
            public Component getDisplayName() {
                return Component.literal("Hub Spoke");
            }

            @Override
            public WorkspacePlannerClientContributor createClientContributor() {
                return new HubSpokePlannerClientContributor();
            }
        });
    }
}
