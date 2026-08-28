package com.chaosbuffalo.mkworkspace.network;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspaceSamplePreviewPacket;
import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceInsertSocketScreenPacket;
import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspacePieceChunkPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspaceInsertOverlayPacket;
import com.chaosbuffalo.mkworkspace.network.packets.SetWorkspaceLayerLockPacket;
import com.chaosbuffalo.mkworkspace.network.packets.TeleportToWorkspacePiecePacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspacePieceChunkPacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspaceInsertOverlayPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspaceChangePacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspaceChangePreflightPacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspaceChangeEffectsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.ConfirmWorkspaceChangePacket;
import com.chaosbuffalo.mkworkspace.network.packets.CancelWorkspaceChangePacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspaceChangeResultPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MKWorkspace.MODID)
public class MKWorkspacePacketHandler {
    private static final String VERSION = "2.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToServer(
                RequestWorkspaceChangePacket.TYPE,
                RequestWorkspaceChangePacket.STREAM_CODEC,
                RequestWorkspaceChangePacket::handle
        );
        registrar.playToClient(
                WorkspaceChangePreflightPacket.TYPE,
                WorkspaceChangePreflightPacket.STREAM_CODEC,
                WorkspaceChangePreflightPacket::handle
        );
        registrar.playToClient(
                WorkspaceChangeEffectsPacket.TYPE,
                WorkspaceChangeEffectsPacket.STREAM_CODEC,
                WorkspaceChangeEffectsPacket::handle
        );
        registrar.playToServer(
                ConfirmWorkspaceChangePacket.TYPE,
                ConfirmWorkspaceChangePacket.STREAM_CODEC,
                ConfirmWorkspaceChangePacket::handle
        );
        registrar.playToServer(
                CancelWorkspaceChangePacket.TYPE,
                CancelWorkspaceChangePacket.STREAM_CODEC,
                CancelWorkspaceChangePacket::handle
        );
        registrar.playToClient(
                WorkspaceChangeResultPacket.TYPE,
                WorkspaceChangeResultPacket.STREAM_CODEC,
                WorkspaceChangeResultPacket::handle
        );
        registrar.playToClient(
                OpenWorkspaceScreenPacket.TYPE,
                OpenWorkspaceScreenPacket.STREAM_CODEC,
                OpenWorkspaceScreenPacket::handle
        );
        registrar.playToClient(
                OpenWorkspaceInsertSocketScreenPacket.TYPE,
                OpenWorkspaceInsertSocketScreenPacket.STREAM_CODEC,
                OpenWorkspaceInsertSocketScreenPacket::handle
        );
        registrar.playToServer(
                RequestWorkspacePieceChunkPacket.TYPE,
                RequestWorkspacePieceChunkPacket.STREAM_CODEC,
                RequestWorkspacePieceChunkPacket::handle
        );
        registrar.playToServer(
                RequestWorkspaceInsertOverlayPacket.TYPE,
                RequestWorkspaceInsertOverlayPacket.STREAM_CODEC,
                RequestWorkspaceInsertOverlayPacket::handle
        );
        registrar.playToServer(
                SetWorkspaceLayerLockPacket.TYPE,
                SetWorkspaceLayerLockPacket.STREAM_CODEC,
                SetWorkspaceLayerLockPacket::handle
        );
        registrar.playToClient(
                WorkspacePieceChunkPacket.TYPE,
                WorkspacePieceChunkPacket.STREAM_CODEC,
                WorkspacePieceChunkPacket::handle
        );
        registrar.playToClient(
                WorkspaceInsertOverlayPacket.TYPE,
                WorkspaceInsertOverlayPacket.STREAM_CODEC,
                WorkspaceInsertOverlayPacket::handle
        );
        registrar.playToServer(
                GenerateWorkspaceSamplePreviewPacket.TYPE,
                GenerateWorkspaceSamplePreviewPacket.STREAM_CODEC,
                GenerateWorkspaceSamplePreviewPacket::handle
        );
        registrar.playToServer(
                ExportWorkspacePiecesPacket.TYPE,
                ExportWorkspacePiecesPacket.STREAM_CODEC,
                ExportWorkspacePiecesPacket::handle
        );
        registrar.playToServer(
                TeleportToWorkspacePiecePacket.TYPE,
                TeleportToWorkspacePiecePacket.STREAM_CODEC,
                TeleportToWorkspacePiecePacket::handle
        );
    }
}
