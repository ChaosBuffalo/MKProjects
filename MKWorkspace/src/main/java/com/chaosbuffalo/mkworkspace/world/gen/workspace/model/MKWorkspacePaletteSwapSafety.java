package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MKWorkspacePaletteSwapSafety {
    private MKWorkspacePaletteSwapSafety() {
    }

    public static boolean canRepresentAsBlockReplacement(MKWorkspaceMaterialPalette source,
                                                         MKWorkspaceMaterialPalette target) {
        Map<ResourceLocation, ResourceLocation> targetBySource = new LinkedHashMap<>();
        return addRoleMapping(targetBySource, source.floorBlock(), target.floorBlock()) &&
                addRoleMapping(targetBySource, source.wallBlock(), target.wallBlock()) &&
                addRoleMapping(targetBySource, source.ceilingBlock(), target.ceilingBlock()) &&
                addRoleMapping(targetBySource, source.stairBlock(), target.stairBlock()) &&
                addRoleMapping(targetBySource, source.slabBlock(), target.slabBlock()) &&
                addRoleMapping(targetBySource, source.ladderBlock(), target.ladderBlock());
    }

    private static boolean addRoleMapping(Map<ResourceLocation, ResourceLocation> targetBySource,
                                          ResourceLocation sourceBlock, ResourceLocation targetBlock) {
        ResourceLocation existingTarget = targetBySource.putIfAbsent(sourceBlock, targetBlock);
        return existingTarget == null || existingTarget.equals(targetBlock);
    }
}
