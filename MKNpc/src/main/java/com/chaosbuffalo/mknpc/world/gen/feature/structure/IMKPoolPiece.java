package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.OptionalInt;
import java.util.UUID;

public interface IMKPoolPiece {

    void setContainingStructure(UUID instanceId, Structure structure);

    void setLockedFloorPlanSegment(String topologyGroup, int segmentIndex);

    OptionalInt getLockedFloorPlanSegment(String topologyGroup);
}
