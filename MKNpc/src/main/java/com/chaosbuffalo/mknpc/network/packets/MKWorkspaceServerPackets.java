package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;

public final class MKWorkspaceServerPackets {
    private static Handler handler = Handler.NO_OP;

    private MKWorkspaceServerPackets() {
    }

    public static void setHandler(Handler handler) {
        MKWorkspaceServerPackets.handler = handler;
    }

    public static void openWorkspaceScreen(ServerPlayer player, BlockPos anchor,
                                           @Nullable MKStructureWorkspace workspace,
                                           List<String> importManifestIds,
                                           List<String> backupManifestFiles) {
        handler.openWorkspaceScreen(player, anchor, workspace, importManifestIds, backupManifestFiles);
    }

    public interface Handler {
        Handler NO_OP = (player, anchor, workspace, importManifestIds, backupManifestFiles) -> {
        };

        void openWorkspaceScreen(ServerPlayer player, BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                 List<String> importManifestIds, List<String> backupManifestFiles);
    }
}
