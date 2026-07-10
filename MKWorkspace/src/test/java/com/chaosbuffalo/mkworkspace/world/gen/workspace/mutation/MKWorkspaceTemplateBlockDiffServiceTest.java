package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceTemplateBlockDiffServiceTest {
    private final MKWorkspaceTemplateBlockDiffService service = new MKWorkspaceTemplateBlockDiffService();

    @Test
    void unchangedBlocksHaveNoAuthoredChanges() {
        Map<BlockPos, String> expected = Map.of(
                pos(0, 0, 0), "stone",
                pos(1, 0, 0), "oak_planks"
        );

        MKWorkspaceTemplateBlockDiffReport report = service.compare(expected, expected, Set.of());

        assertFalse(report.hasAuthoredChanges());
        assertEquals(2, report.comparedBlockCount());
        assertEquals(0, report.changedBlockCount());
    }

    @Test
    void changedBlocksAreReportedAsAuthoredChanges() {
        BlockPos changedPos = pos(1, 0, 0);
        Map<BlockPos, String> expected = Map.of(
                pos(0, 0, 0), "stone",
                changedPos, "oak_planks"
        );
        Map<BlockPos, String> actual = Map.of(
                pos(0, 0, 0), "stone",
                changedPos, "cobblestone"
        );

        MKWorkspaceTemplateBlockDiffReport report = service.compare(expected, actual, Set.of());

        assertTrue(report.hasAuthoredChanges());
        assertEquals(1, report.changedBlockCount());
        assertEquals(changedPos, report.changedPositions().getFirst());
    }

    @Test
    void missingAndExtraBlocksAreReportedAsChanges() {
        BlockPos missingPos = pos(1, 0, 0);
        BlockPos extraPos = pos(2, 0, 0);
        Map<BlockPos, String> expected = Map.of(
                pos(0, 0, 0), "stone",
                missingPos, "oak_planks"
        );
        Map<BlockPos, String> actual = Map.of(
                pos(0, 0, 0), "stone",
                extraPos, "cobblestone"
        );

        MKWorkspaceTemplateBlockDiffReport report = service.compare(expected, actual, Set.of());

        assertTrue(report.hasAuthoredChanges());
        assertEquals(2, report.changedBlockCount());
        assertTrue(report.changedPositions().contains(missingPos));
        assertTrue(report.changedPositions().contains(extraPos));
    }

    @Test
    void generatedOwnedSidecarPositionsAreIgnored() {
        BlockPos sidecarPos = pos(1, 0, 0);
        Map<BlockPos, String> expected = Map.of(
                pos(0, 0, 0), "stone",
                sidecarPos, "structure_block"
        );
        Map<BlockPos, String> actual = Map.of(
                pos(0, 0, 0), "stone",
                sidecarPos, "oak_sign"
        );

        MKWorkspaceTemplateBlockDiffReport report = service.compare(expected, actual, Set.of(sidecarPos));

        assertFalse(report.hasAuthoredChanges());
        assertEquals(1, report.comparedBlockCount());
    }

    @Test
    void reportRoundTripsForClientDisplay() {
        MKWorkspaceTemplateBlockDiffReport report = new MKWorkspaceTemplateBlockDiffReport(
                3,
                1,
                java.util.List.of(pos(1, 2, 3))
        );

        MKWorkspaceTemplateBlockDiffReport decoded = MKWorkspaceTemplateBlockDiffReport.CODEC.parse(
                com.mojang.serialization.JsonOps.INSTANCE,
                MKWorkspaceTemplateBlockDiffReport.CODEC.encodeStart(
                        com.mojang.serialization.JsonOps.INSTANCE, report).getOrThrow()
        ).getOrThrow();

        assertEquals(report, decoded);
    }

    private static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}
