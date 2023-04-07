package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface IMKPoolPiece {

    void setStart(UUID instanceId, ResourceLocation structureName);
}
