package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

public record MKWorkspaceSlotSchema(
        String slotId,
        String regionId,
        String slotKind,
        String roleId,
        Repeat repeat
) {
    public enum Repeat {
        FIXED,
        OPTIONAL,
        RANGE,
        DERIVED
    }
}
