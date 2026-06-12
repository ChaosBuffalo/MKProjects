package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MKWorkspaceTemplateBlockDiffService {
    public <T> MKWorkspaceTemplateBlockDiffReport compare(Map<BlockPos, T> expectedGeneratedBlocks,
                                                          Map<BlockPos, T> actualTemplateBlocks,
                                                          Set<BlockPos> generatedOwnedSidecarPositions) {
        Set<BlockPos> positions = new HashSet<>();
        positions.addAll(expectedGeneratedBlocks.keySet());
        positions.addAll(actualTemplateBlocks.keySet());
        positions.removeAll(generatedOwnedSidecarPositions);

        ArrayList<BlockPos> sortedPositions = new ArrayList<>(positions);
        sortedPositions.sort(Comparator
                .comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingInt(pos -> pos.getZ())
                .thenComparingInt(pos -> pos.getX()));

        ArrayList<BlockPos> changedPositions = new ArrayList<>();
        for (BlockPos pos : sortedPositions) {
            T expected = expectedGeneratedBlocks.get(pos);
            T actual = actualTemplateBlocks.get(pos);
            if (!Objects.equals(expected, actual)) {
                changedPositions.add(pos.immutable());
            }
        }
        return new MKWorkspaceTemplateBlockDiffReport(sortedPositions.size(), changedPositions.size(),
                changedPositions);
    }
}
