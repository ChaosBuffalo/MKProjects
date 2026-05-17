package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

public record MKWorkspaceLinkSchema(
        String linkId,
        String fromSlot,
        String toSlot,
        String linkKind
) {
}
