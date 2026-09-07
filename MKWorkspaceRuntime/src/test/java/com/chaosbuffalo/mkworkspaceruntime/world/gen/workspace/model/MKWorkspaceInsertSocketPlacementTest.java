package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceInsertSocketPlacementTest {
    @Test
    void validatesOddWidthAndLengthButAllowsArbitraryHeight() {
        List<String> errors = MKWorkspaceInsertSocketPlacement.validateInsertDimensions(4, 6, 2);

        assertTrue(errors.contains("insert width must be odd"));
        assertTrue(errors.contains("insert length must be odd"));
        assertTrue(MKWorkspaceInsertSocketPlacement.validateInsertDimensions(5, 6, 3).isEmpty());
    }

    @Test
    void horizontalFacesLockJigsawToExteriorBounds() {
        assertEquals(new BlockPos(2, 1, 0), MKWorkspaceInsertSocketPlacement.jigsawLocalPos(
                5, 4, 7, MKWorkspaceInsertAttachmentFace.NORTH, 2, 1));
        assertEquals(new BlockPos(4, 1, 3), MKWorkspaceInsertSocketPlacement.jigsawLocalPos(
                5, 4, 7, MKWorkspaceInsertAttachmentFace.EAST, 3, 1));
        assertTrue(MKWorkspaceInsertSocketPlacement.validateExteriorFaceOffset(
                5, 4, 7, MKWorkspaceInsertAttachmentFace.EAST, 6, 1).isEmpty());
    }

    @Test
    void bottomAndTopFacesLockVerticalJigsawToExteriorBounds() {
        assertEquals(new BlockPos(2, 0, 4), MKWorkspaceInsertSocketPlacement.jigsawLocalPos(
                5, 6, 7, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 4));
        assertEquals(new BlockPos(2, 5, 4), MKWorkspaceInsertSocketPlacement.jigsawLocalPos(
                5, 6, 7, MKWorkspaceInsertAttachmentFace.TOP, 2, 4));
    }

    @Test
    void rejectsFaceOffsetsOutsideTheSelectedExteriorFace() {
        List<String> horizontalErrors = MKWorkspaceInsertSocketPlacement.validateExteriorFaceOffset(
                5, 4, 7, MKWorkspaceInsertAttachmentFace.NORTH, 5, 0);
        List<String> verticalErrors = MKWorkspaceInsertSocketPlacement.validateExteriorFaceOffset(
                5, 4, 7, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 7);

        assertTrue(horizontalErrors.contains("jigsaw face u offset must be between 0 and 4"));
        assertTrue(verticalErrors.contains("jigsaw face v offset must be between 0 and 6"));
    }

    @Test
    void validatesProjectedInsertBoundsAgainstHostAuthorialPiece() {
        BoundingBox hostBounds = new BoundingBox(0, 0, 0, 10, 8, 10);
        BlockPos centeredSocket = new BlockPos(5, 0, 5);

        assertTrue(MKWorkspaceInsertSocketPlacement.validateFits(
                hostBounds, centeredSocket, 5, 6, 5, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 2).isEmpty());

        List<String> errors = MKWorkspaceInsertSocketPlacement.validateFits(
                hostBounds, centeredSocket, 9, 10, 9, MKWorkspaceInsertAttachmentFace.BOTTOM, 0, 0);
        assertTrue(errors.contains("too wide for this socket"));
        assertTrue(errors.contains("too long for this socket"));
        assertTrue(errors.contains("too tall for this socket"));
    }

    @Test
    void projectsAuthoredVerticalInsertOntoHorizontalSocketOrientation() {
        BlockPos socket = new BlockPos(10, 4, 10);

        BoundingBox verticalBounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(
                socket, 5, 7, 3, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 1,
                MKWorkspaceInsertAttachmentFace.BOTTOM, Direction.NORTH);
        BoundingBox horizontalBounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(
                socket, 5, 7, 3, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 1,
                MKWorkspaceInsertAttachmentFace.WEST, Direction.UP);

        assertBounds(verticalBounds, 8, 4, 9, 12, 10, 11);
        assertBounds(horizontalBounds, 10, 3, 8, 16, 5, 12);
    }

    @Test
    void validatesOrientedBoundsAgainstHostAuthorialPiece() {
        BoundingBox hostBounds = new BoundingBox(0, 0, 0, 12, 8, 12);
        BlockPos socket = new BlockPos(6, 4, 6);

        assertTrue(MKWorkspaceInsertSocketPlacement.validateOrientedFits(hostBounds, socket,
                5, 6, 3, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 1,
                MKWorkspaceInsertAttachmentFace.WEST, Direction.UP).isEmpty());

        List<String> errors = MKWorkspaceInsertSocketPlacement.validateOrientedFits(hostBounds, socket,
                5, 9, 3, MKWorkspaceInsertAttachmentFace.BOTTOM, 2, 1,
                MKWorkspaceInsertAttachmentFace.WEST, Direction.UP);
        assertTrue(errors.contains("too wide for this socket"));
    }

    @Test
    void legacyCourtyardSocketKindNormalizesToInsertSocket() {
        MKWorkspaceInsertFamilyDefinition definition = new MKWorkspaceInsertFamilyDefinition(
                "legacy", MKWorkspaceInsertFamilyKind.COURTYARD_SOCKET, 5, 3, 5);

        assertEquals(MKWorkspaceInsertFamilyKind.INSERT_SOCKET, definition.kind());
        assertEquals("insert_socket", definition.toTag().getString("kind"));
    }

    private void assertBounds(BoundingBox bounds, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        assertEquals(minX, bounds.minX());
        assertEquals(minY, bounds.minY());
        assertEquals(minZ, bounds.minZ());
        assertEquals(maxX, bounds.maxX());
        assertEquals(maxY, bounds.maxY());
        assertEquals(maxZ, bounds.maxZ());
    }
}
