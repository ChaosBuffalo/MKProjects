package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKConnectorRole;
import net.minecraft.core.Direction;

public record MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight,
                                 String targetBaseName) {
    public MKPlannedConnector(MKConnectorRole role, Direction facing, int openingWidth, int openingHeight) {
        this(role, facing, openingWidth, openingHeight, null);
    }
}
