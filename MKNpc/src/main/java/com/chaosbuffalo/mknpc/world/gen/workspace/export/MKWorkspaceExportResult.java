package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import java.nio.file.Path;

public record MKWorkspaceExportResult(int savedPieceCount, Path manifestPath, Path pieceMetadataDirectory) {
}
