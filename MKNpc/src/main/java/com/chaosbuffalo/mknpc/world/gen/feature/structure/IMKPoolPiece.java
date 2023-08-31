package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.UUID;

public interface IMKPoolPiece {

    void setContainingStructure(UUID instanceId, Structure structure);
}
