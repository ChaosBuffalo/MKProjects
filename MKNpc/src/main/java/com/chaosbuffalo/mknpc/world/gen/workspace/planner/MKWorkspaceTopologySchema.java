package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import java.util.List;

public record MKWorkspaceTopologySchema(
        String profileType,
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
