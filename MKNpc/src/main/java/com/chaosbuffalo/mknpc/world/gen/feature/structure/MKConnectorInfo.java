package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public record MKConnectorInfo(
        ResourceLocation name,
        ResourceLocation target,
        ResourceKey<StructureTemplatePool> targetPool,
        MKConnectorRole role
) {
}
