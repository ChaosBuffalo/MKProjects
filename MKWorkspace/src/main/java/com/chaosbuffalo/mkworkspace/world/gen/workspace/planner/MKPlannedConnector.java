package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;

public record MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                                 int lateralOffset, int verticalOffset,
                                 String targetPoolName, String incomingPoolName,
                                 boolean placesJigsaw,
                                 @Nullable MKWorkspaceHorizontalExtrusionMode horizontalExtrusionModeOverride) {
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
                true, null);
    }

    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                              int lateralOffset, int verticalOffset,
                              String targetPoolName, String incomingPoolName,
                              @Nullable MKWorkspaceHorizontalExtrusionMode horizontalExtrusionModeOverride) {
        this(role, facing, openingWidth, openingHeight, lateralOffset, verticalOffset, targetPoolName, incomingPoolName,
                true, horizontalExtrusionModeOverride);
    }

    public static MKPlannedConnector openingOnly(MKConnectorRole role, Direction facing, int openingWidth,
                                                 int openingHeight, int lateralOffset, int verticalOffset) {
        return new MKPlannedConnector(role, facing, openingWidth, openingHeight, lateralOffset, verticalOffset,
                null, null, false, null);
    }

    public static MKPlannedConnector openingOnly(MKConnectorRole role, Direction facing, int openingWidth,
                                                 int openingHeight, int lateralOffset, int verticalOffset,
                                                 @Nullable MKWorkspaceHorizontalExtrusionMode horizontalExtrusionModeOverride) {
        return new MKPlannedConnector(role, facing, openingWidth, openingHeight, lateralOffset, verticalOffset,
                null, null, false, horizontalExtrusionModeOverride);
    }
}
