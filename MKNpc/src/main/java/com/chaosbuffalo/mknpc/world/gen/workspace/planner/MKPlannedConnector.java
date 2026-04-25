package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import net.minecraft.core.Direction;

public record MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                                 String targetPoolName, String incomingPoolName) {
    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight) {
        this(role, facing, openingWidth, openingHeight, null, null);
    }

    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                              String targetPoolName) {
        this(role, facing, openingWidth, openingHeight, targetPoolName, null);
    }
}
