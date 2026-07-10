package com.chaosbuffalo.mkworkspace.network;

import com.chaosbuffalo.mknpc.network.packets.MKWorkspaceServerPackets;
import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;

public final class MKWorkspaceServerPacketHandler implements MKWorkspaceServerPackets.Handler {
    private static final MKWorkspaceServerPacketHandler INSTANCE = new MKWorkspaceServerPacketHandler();

    private MKWorkspaceServerPacketHandler() {
    }

    public static void register() {
        MKWorkspaceServerPackets.setHandler(INSTANCE);
    }

    @Override
    public void openWorkspaceScreen(ServerPlayer player, BlockPos anchor, @Nullable MKStructureWorkspace workspace,
                                    List<String> importManifestIds, List<String> backupManifestFiles) {
        player.connection.send(new OpenWorkspaceScreenPacket(anchor, workspace, importManifestIds, backupManifestFiles));
    }
}
