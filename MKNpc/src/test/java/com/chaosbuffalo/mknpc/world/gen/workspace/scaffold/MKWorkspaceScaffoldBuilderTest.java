package com.chaosbuffalo.mknpc.world.gen.workspace.scaffold;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MKWorkspaceScaffoldBuilderTest {
    @Test
    void workspaceClearBoundsExtendToBuildHeight() {
        MKWorkspaceScaffoldBuilder builder = new MKWorkspaceScaffoldBuilder();

        BoundingBox expanded = builder.extendToWorkspaceClearHeight(
                new BoundingBox(10, -80, 20, 30, 12, 40),
                -64,
                319
        );

        assertEquals(10, expanded.minX());
        assertEquals(-64, expanded.minY());
        assertEquals(20, expanded.minZ());
        assertEquals(30, expanded.maxX());
        assertEquals(319, expanded.maxY());
        assertEquals(40, expanded.maxZ());
    }
}
