package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MKWorkspaceTopologySchema(
        ResourceLocation plannerId,
        List<MKWorkspaceRegionSchema> regions,
        List<MKWorkspaceSlotSchema> slots,
        List<MKWorkspaceLinkSchema> links,
        List<MKWorkspaceRoleSchema> roles
) {
    public MKWorkspaceTopologySchema {
        regions = List.copyOf(regions);
        slots = List.copyOf(slots);
        links = List.copyOf(links);
        roles = List.copyOf(roles);
    }
}
