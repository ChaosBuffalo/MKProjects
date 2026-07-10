package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

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
        Map<String, String> tags,
        MKWorkspacePlannerId plannerId
) {
    public MKPlannedPiece(String roleId,
                          String pieceName,
                          int interiorWidth,
                          int interiorLength,
                          int interiorHeight,
                          List<MKPlannedConnector> connectors,
                          Map<String, String> tags) {
        this(roleId, pieceName, interiorWidth, interiorLength, interiorHeight, connectors, tags,
                MKWorkspacePlannerId.of(roleId).child(pieceName));
    }

    public MKPlannedPiece {
        plannerId = plannerId == null ? MKWorkspacePlannerId.of(roleId).child(pieceName) : plannerId;
    }
}
