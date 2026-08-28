package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MKWorkspacePreparedMutation {
    MKWorkspaceChangeApplyResult apply(ServerPlayer player) throws Exception;
}
