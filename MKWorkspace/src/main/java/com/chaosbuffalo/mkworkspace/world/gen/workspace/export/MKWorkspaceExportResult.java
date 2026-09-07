package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import java.nio.file.Path;

public record MKWorkspaceExportResult(int savedPieceCount, int metadataCount, Path archivePath) {
}
