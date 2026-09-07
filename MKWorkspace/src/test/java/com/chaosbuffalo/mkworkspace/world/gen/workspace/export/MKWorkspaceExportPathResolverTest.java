package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceExportPathResolverTest {
    @Test
    void backupPathUsesDimensionAnchorAndImmutableWorkspaceId() {
        UUID workspaceId = UUID.randomUUID();
        Path path = new MKWorkspaceExportPathResolver().getBackupManifestDirectory(
                Path.of("world"), ResourceLocation.parse("minecraft:the_nether"),
                new BlockPos(-12, 64, 35), workspaceId);

        String normalized = path.toString().replace('\\', '/');
        assertTrue(normalized.endsWith("generated/mkworkspace/backups/minecraft/the_nether/-12_64_35/" +
                workspaceId));
        assertFalse(normalized.contains("structure_name"));
        assertEquals(path, new MKWorkspaceExportPathResolver().getBackupManifestDirectory(
                Path.of("world"), ResourceLocation.parse("minecraft:the_nether"),
                new BlockPos(-12, 64, 35), workspaceId));
    }
}
