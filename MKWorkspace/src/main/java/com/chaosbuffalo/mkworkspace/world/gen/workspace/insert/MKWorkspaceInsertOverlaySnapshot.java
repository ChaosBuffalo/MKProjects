package com.chaosbuffalo.mkworkspace.world.gen.workspace.insert;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public record MKWorkspaceInsertOverlaySnapshot(List<Entry> entries) {
    public MKWorkspaceInsertOverlaySnapshot {
        entries = List.copyOf(entries);
    }

    public record Entry(String familyId, BlockPos socketWorldPos, BoundingBox bounds,
                        Direction front, Direction top) {
    }
}
