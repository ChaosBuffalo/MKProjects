package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;

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
    public MKWorkspacePlannerId plannerId() {
        return MKWorkspacePlannerId.of(roleId).child(pieceName);
    }
}
