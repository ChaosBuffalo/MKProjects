package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKFloorConnectorPatchTest {
    @Test
    void northSouthPatchUsesExactOddWidthFootprint() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.NORTH, new BlockPos(10, 3, 2), 3, 2);

        List<BlockPos> positions = MKFloorConnectorPatch.closedConnectorPatchPositions(piece);

        assertEquals(6, positions.size());
        assertEquals(Set.of(
                new BlockPos(9, 3, 2),
                new BlockPos(10, 3, 2),
                new BlockPos(11, 3, 2),
                new BlockPos(9, 4, 2),
                new BlockPos(10, 4, 2),
                new BlockPos(11, 4, 2)
        ), Set.copyOf(positions));
    }

    @Test
    void eastWestPatchUsesExactEvenWidthFootprint() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.EAST, new BlockPos(4, 1, 8), 4, 3);

        List<BlockPos> positions = MKFloorConnectorPatch.closedConnectorPatchPositions(piece);

        assertEquals(12, positions.size());
        assertEquals(Set.of(
                new BlockPos(4, 1, 7),
                new BlockPos(4, 1, 8),
                new BlockPos(4, 1, 9),
                new BlockPos(4, 1, 10),
                new BlockPos(4, 2, 7),
                new BlockPos(4, 2, 8),
                new BlockPos(4, 2, 9),
                new BlockPos(4, 2, 10),
                new BlockPos(4, 3, 7),
                new BlockPos(4, 3, 8),
                new BlockPos(4, 3, 9),
                new BlockPos(4, 3, 10)
        ), Set.copyOf(positions));
    }

    @Test
    void nonHorizontalClosedConnectorDoesNotPatch() {
        MKWorkspacePieceDefinition piece = closedConnectorPiece(Direction.UP, new BlockPos(4, 1, 8), 3, 3);

        assertTrue(MKFloorConnectorPatch.closedConnectorPatchPositions(piece).isEmpty());
    }

    private static MKWorkspacePieceDefinition closedConnectorPiece(Direction facing, BlockPos pos, int openingWidth,
                                                                   int openingHeight) {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put(MKFloorMaskVariantExporter.CLOSED_CONNECTOR_COUNT_TAG, "1");
        String prefix = MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + "0_";
        tags.put(prefix + "facing", facing.getSerializedName());
        tags.put(prefix + "x", Integer.toString(pos.getX()));
        tags.put(prefix + "y", Integer.toString(pos.getY()));
        tags.put(prefix + "z", Integer.toString(pos.getZ()));
        tags.put(prefix + "opening_width", Integer.toString(openingWidth));
        tags.put(prefix + "opening_height", Integer.toString(openingHeight));
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "floor_room_mask_none",
                "tower.primary.main_floor",
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(new MKWorkspaceConnectorDefinition(
                        MKConnectorRole.BRANCH,
                        Direction.SOUTH,
                        BlockPos.ZERO,
                        3,
                        3,
                        0,
                        0,
                        ResourceLocation.parse("mkdev:branch"),
                        ResourceLocation.parse("mkdev:target"),
                        ResourceLocation.parse("minecraft:empty"),
                        ResourceLocation.parse("minecraft:empty")
                )),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags
        );
    }
}
