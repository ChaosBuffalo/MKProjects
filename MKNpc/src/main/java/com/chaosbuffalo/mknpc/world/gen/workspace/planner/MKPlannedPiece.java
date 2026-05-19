package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import java.util.List;
import java.util.Map;

public record MKPlannedPiece(
        String roleId,
        String pieceName,
        int interiorWidth,
        int interiorLength,
        int interiorHeight,
        List<MKPlannedConnector> connectors,
        Map<String, String> tags
) {
}
