package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import net.minecraft.core.Direction;

public record MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                                 int lateralOffset, int verticalOffset,
                                 String targetPoolName, String incomingPoolName,
                                 boolean placesJigsaw) {
    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight) {
        this(role, facing, openingWidth, openingHeight, 0, 0, null, null);
    }

    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                              String targetPoolName) {
        this(role, facing, openingWidth, openingHeight, 0, 0, targetPoolName, null);
    }

    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                              String targetPoolName, String incomingPoolName) {
        this(role, facing, openingWidth, openingHeight, 0, 0, targetPoolName, incomingPoolName);
    }

    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                              int lateralOffset, int verticalOffset, String targetPoolName) {
        this(role, facing, openingWidth, openingHeight, lateralOffset, verticalOffset, targetPoolName, null);
    }

    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                              int lateralOffset, int verticalOffset,
                              String targetPoolName, String incomingPoolName) {
        this(role, facing, openingWidth, openingHeight, lateralOffset, verticalOffset, targetPoolName, incomingPoolName,
                true);
    }

    public static MKPlannedConnector openingOnly(MKConnectorRole role, Direction facing, int openingWidth,
                                                 int openingHeight, int lateralOffset, int verticalOffset) {
        return new MKPlannedConnector(role, facing, openingWidth, openingHeight, lateralOffset, verticalOffset,
                null, null, false);
    }
}
