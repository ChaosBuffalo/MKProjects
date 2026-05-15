package com.chaosbuffalo.mknpc.world.gen.workspace.stairs;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceStairBuilderTest {
    @Test
    void capGenerationGeometryUsesExpectedTraversalRange() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();

        MKWorkspaceVerticalAccessGeometry.ShaftGeometry topGeometry = builder.getGenerationGeometry(workspace,
                verticalCapPiece(Direction.DOWN, MKWorkspaceVerticalAccessTags.TOP_CAP_TAG));
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry bottomGeometry = builder.getGenerationGeometry(workspace,
                verticalCapPiece(Direction.UP, MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG));

        assertEquals(0, topGeometry.interiorMinY());
        assertEquals(0, topGeometry.interiorMaxY());
        assertEquals(0, bottomGeometry.interiorMinY());
        assertEquals(6, bottomGeometry.interiorMaxY());
    }

    @Test
    void bottomCapKeepsFullPhaseGeometryButClipsEditsAboveBottomShell() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspacePieceDefinition bottomCap = verticalCapPiece(Direction.UP,
                MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG);
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = builder.getGenerationGeometry(workspace, bottomCap);

        assertEquals(0, geometry.interiorMinY());
        assertEquals(6, geometry.interiorMaxY());
        assertEquals(1, builder.getEditableMinY(bottomCap, geometry));
    }

    @Test
    void verticalAccessGeometryIgnoresBottomVoidMarginTags() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspacePieceDefinition bottomCap = verticalCapPiece(Direction.UP,
                MKWorkspaceVerticalAccessTags.BOTTOM_CAP_TAG,
                new BoundingBox(0, 0, 0, 10, 8, 10),
                Map.of(MKTowerWorkspaceCategoryProfile.BOTTOM_VOID_MARGIN_TAG, "2"));

        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = builder.getGenerationGeometry(workspace, bottomCap);

        assertEquals(0, geometry.interiorMinY());
        assertEquals(8, geometry.interiorMaxY());
        assertEquals(1, builder.getEditableMinY(bottomCap, geometry));
    }

    @Test
    void verticalAccessGeometryIgnoresTopVoidMarginTags() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspacePieceDefinition topCap = verticalCapPiece(Direction.DOWN,
                MKWorkspaceVerticalAccessTags.TOP_CAP_TAG,
                new BoundingBox(0, 0, 0, 10, 8, 10),
                Map.of(MKTowerWorkspaceCategoryProfile.TOP_VOID_MARGIN_TAG, "2"));

        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = builder.getGenerationGeometry(workspace, topCap);

        assertEquals(0, geometry.interiorMinY());
        assertEquals(0, geometry.interiorMaxY());
    }

    @Test
    void profileResolutionUsesRoomHeightNotExportShellHeight() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspacePieceDefinition piece = verticalPiece();
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = builder.getGenerationGeometry(workspace, piece);

        assertEquals(7, geometry.interiorMaxY() - geometry.interiorMinY() + 1);
        assertEquals(5, builder.getProfileInteriorHeight(piece));
    }

    @Test
    void topCapApproachIsNotClippedLikeFinalTopCap() {
        MKStructureWorkspace workspace = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspaceVerticalAccessGeometry.ShaftGeometry geometry = builder.getGenerationGeometry(workspace,
                verticalPiece(MKWorkspacePieceRole.TOP_CAP_APPROACH, Map.of(MKWorkspaceVerticalAccessTags.ENABLED_TAG, "true")));

        assertEquals(0, geometry.interiorMinY());
        assertEquals(6, geometry.interiorMaxY());
    }

    @Test
    void topCapSlabContinuationCoversOneFullBlockOfRise() {
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        MKWorkspaceStairAuthoringConfig slabConfig = new MKWorkspaceStairAuthoringConfig(
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairRiseType.SLAB,
                1,
                ResourceLocation.parse("minecraft:stone_brick_stairs"),
                ResourceLocation.parse("minecraft:stone_brick_slab"),
                ResourceLocation.parse("minecraft:ladder")
        );

        assertEquals(List.of(
                        com.chaosbuffalo.mknpc.world.gen.workspace.model.MKResolvedVerticalAccessProfile.RiseStepKind.SLAB_BOTTOM,
                        com.chaosbuffalo.mknpc.world.gen.workspace.model.MKResolvedVerticalAccessProfile.RiseStepKind.SLAB_TOP),
                builder.getTopCapContinuationPattern(slabConfig, null));
    }

    @Test
    void topCapContinuationAddsEntryLandingBeforeFirstStep() {
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        List<BlockPos> perimeter = List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0),
                new BlockPos(2, 0, 1),
                new BlockPos(2, 0, 2),
                new BlockPos(1, 0, 2),
                new BlockPos(0, 0, 2),
                new BlockPos(0, 0, 1)
        );
        LinkedHashMap<BlockPos, BlockState> planned = new LinkedHashMap<>();
        LinkedHashSet<BlockPos> generated = new LinkedHashSet<>();
        BlockState landingState = null;

        builder.planEntryLanding(planned, perimeter, 5, 0,
                new BoundingBox(0, 0, 0, 2, 0, 2),
                new BoundingBox(0, 0, 0, 2, 0, 2),
                1,
                landingState,
                generated);

        BlockPos expectedLanding = new BlockPos(2, 0, 2);
        assertTrue(planned.containsKey(expectedLanding));
        assertEquals(landingState, planned.get(expectedLanding));
        assertEquals(List.of(expectedLanding), List.copyOf(generated));
    }

    @Test
    void stairBandTargetsCoverFullConfiguredWidth() {
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        BlockPos edgePos = new BlockPos(1, 0, 1);

        assertEquals(List.of(edgePos, edgePos.north()), builder.getStairBandTargets(
                edgePos,
                new BoundingBox(1, 0, 1, 3, 0, 3),
                new BoundingBox(0, 0, 0, 4, 0, 4),
                2
        ));
    }

    @Test
    void turnStairBandTargetsCoverFullConfiguredWidth() {
        MKWorkspaceStairBuilder builder = new MKWorkspaceStairBuilder();
        BlockPos cornerPos = new BlockPos(1, 0, 1);

        assertEquals(List.of(cornerPos, cornerPos.north()), builder.getTurnStairBandTargets(
                cornerPos,
                Direction.EAST,
                2,
                new BoundingBox(0, 0, 0, 4, 0, 4)
        ));
    }

    private MKWorkspacePieceDefinition verticalPiece() {
        return verticalPiece(MKWorkspacePieceRole.FLOOR_MAIN,
                Map.of(MKWorkspaceVerticalAccessTags.ENABLED_TAG, "true"));
    }

    private MKWorkspacePieceDefinition verticalPiece(MKWorkspacePieceRole role, Map<String, String> tags) {
        UUID workspaceId = UUID.randomUUID();
        BoundingBox bounds = new BoundingBox(0, 0, 0, 10, 6, 10);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspaceId,
                role.getSerializedName(),
                role,
                0,
                new MKWorkspaceDimensions(9, 9, 5, 5, 5, 3, 3, 3),
                1,
                List.of(verticalConnector(Direction.UP), verticalConnector(Direction.DOWN)),
                BlockPos.ZERO,
                bounds,
                bounds,
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private MKWorkspacePieceDefinition verticalCapPiece(Direction connectorFacing, String capTag) {
        return verticalCapPiece(connectorFacing, capTag, new BoundingBox(0, 0, 0, 10, 6, 10), Map.of());
    }

    private MKWorkspacePieceDefinition verticalCapPiece(Direction connectorFacing, String capTag, BoundingBox bounds,
                                                        Map<String, String> extraTags) {
        UUID workspaceId = UUID.randomUUID();
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put(MKWorkspaceVerticalAccessTags.ENABLED_TAG, "true");
        tags.put(capTag, "true");
        tags.putAll(extraTags);
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                workspaceId,
                "cap",
                MKWorkspacePieceRole.TOP_CAP,
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(verticalConnector(connectorFacing)),
                BlockPos.ZERO,
                bounds,
                bounds,
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }

    private MKWorkspaceConnectorDefinition verticalConnector(Direction facing) {
        ResourceLocation empty = ResourceLocation.parse("minecraft:empty");
        return new MKWorkspaceConnectorDefinition(
                facing == Direction.UP ? MKConnectorRole.CONNECT_UP : MKConnectorRole.CONNECT_DOWN,
                facing,
                BlockPos.ZERO,
                3,
                3,
                0,
                0,
                empty,
                empty,
                empty,
                empty
        );
    }

}
