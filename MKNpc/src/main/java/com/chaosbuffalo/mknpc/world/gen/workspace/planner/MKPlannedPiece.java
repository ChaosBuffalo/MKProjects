package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;

import java.util.List;
import java.util.Map;

public record MKPlannedPiece(
        MKWorkspacePieceRole role,
        String pieceName,
        int interiorWidth,
        int interiorLength,
        int interiorHeight,
        List<MKPlannedConnector> connectors,
        Map<String, String> tags
) {
}
