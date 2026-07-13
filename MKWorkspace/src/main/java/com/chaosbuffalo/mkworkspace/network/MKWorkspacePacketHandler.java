package com.chaosbuffalo.mkworkspace.network;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mkworkspace.network.packets.AddWorkspaceVariantsForAllPacket;
import com.chaosbuffalo.mkworkspace.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mkworkspace.network.packets.DeleteWorkspacePacket;
import com.chaosbuffalo.mkworkspace.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateAllWorkspaceStairsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspacePacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspaceSamplePreviewPacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspaceStairsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.LoadWorkspaceFromManifestPacket;
import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspacePieceChunkPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspacePreflightPacket;
import com.chaosbuffalo.mkworkspace.network.packets.RestoreWorkspaceBackupPacket;
import com.chaosbuffalo.mkworkspace.network.packets.SetWorkspaceLayerLockPacket;
import com.chaosbuffalo.mkworkspace.network.packets.SwapWorkspaceBlockPacket;
import com.chaosbuffalo.mkworkspace.network.packets.TeleportToWorkspacePiecePacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspacePieceChunkPacket;
import com.chaosbuffalo.mkworkspace.network.packets.WorkspacePreflightReportPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MKWorkspace.MODID)
public class MKWorkspacePacketHandler {
    private static final String VERSION = "1.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                OpenWorkspaceScreenPacket.TYPE,
                OpenWorkspaceScreenPacket.STREAM_CODEC,
                OpenWorkspaceScreenPacket::handle
        );
        registrar.playToServer(
                CreateWorkspacePacket.TYPE,
                CreateWorkspacePacket.STREAM_CODEC,
                CreateWorkspacePacket::handle
        );
        registrar.playToServer(
                RequestWorkspacePreflightPacket.TYPE,
                RequestWorkspacePreflightPacket.STREAM_CODEC,
                RequestWorkspacePreflightPacket::handle
        );
        registrar.playToServer(
                RequestWorkspacePieceChunkPacket.TYPE,
                RequestWorkspacePieceChunkPacket.STREAM_CODEC,
                RequestWorkspacePieceChunkPacket::handle
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
                WorkspacePreflightReportPacket.TYPE,
                WorkspacePreflightReportPacket.STREAM_CODEC,
                WorkspacePreflightReportPacket::handle
        );
        registrar.playToServer(
                GenerateWorkspacePacket.TYPE,
                GenerateWorkspacePacket.STREAM_CODEC,
                GenerateWorkspacePacket::handle
        );
        registrar.playToServer(
                GenerateWorkspaceSamplePreviewPacket.TYPE,
                GenerateWorkspaceSamplePreviewPacket.STREAM_CODEC,
                GenerateWorkspaceSamplePreviewPacket::handle
        );
        registrar.playToServer(
                GenerateAllWorkspaceStairsPacket.TYPE,
                GenerateAllWorkspaceStairsPacket.STREAM_CODEC,
                GenerateAllWorkspaceStairsPacket::handle
        );
        registrar.playToServer(
                GenerateWorkspaceStairsPacket.TYPE,
                GenerateWorkspaceStairsPacket.STREAM_CODEC,
                GenerateWorkspaceStairsPacket::handle
        );
        registrar.playToServer(
                ClearWorkspaceStairsPacket.TYPE,
                ClearWorkspaceStairsPacket.STREAM_CODEC,
                ClearWorkspaceStairsPacket::handle
        );
        registrar.playToServer(
                AddWorkspaceVariantPacket.TYPE,
                AddWorkspaceVariantPacket.STREAM_CODEC,
                AddWorkspaceVariantPacket::handle
        );
        registrar.playToServer(
                AddWorkspaceVariantsForAllPacket.TYPE,
                AddWorkspaceVariantsForAllPacket.STREAM_CODEC,
                AddWorkspaceVariantsForAllPacket::handle
        );
        registrar.playToServer(
                ExportWorkspacePiecesPacket.TYPE,
                ExportWorkspacePiecesPacket.STREAM_CODEC,
                ExportWorkspacePiecesPacket::handle
        );
        registrar.playToServer(
                LoadWorkspaceFromManifestPacket.TYPE,
                LoadWorkspaceFromManifestPacket.STREAM_CODEC,
                LoadWorkspaceFromManifestPacket::handle
        );
        registrar.playToServer(
                SwapWorkspaceBlockPacket.TYPE,
                SwapWorkspaceBlockPacket.STREAM_CODEC,
                SwapWorkspaceBlockPacket::handle
        );
        registrar.playToServer(
                RestoreWorkspaceBackupPacket.TYPE,
                RestoreWorkspaceBackupPacket.STREAM_CODEC,
                RestoreWorkspaceBackupPacket::handle
        );
        registrar.playToServer(
                DeleteWorkspacePacket.TYPE,
                DeleteWorkspacePacket.STREAM_CODEC,
                DeleteWorkspacePacket::handle
        );
        registrar.playToServer(
                TeleportToWorkspacePiecePacket.TYPE,
                TeleportToWorkspacePiecePacket.STREAM_CODEC,
                TeleportToWorkspacePiecePacket::handle
        );
    }
}
