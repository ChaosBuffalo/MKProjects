package com.chaosbuffalo.mknpc.network;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantsForAllPacket;
import com.chaosbuffalo.mknpc.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.network.packets.FinalizeMKSpawnerPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateAllWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.LoadWorkspaceFromManifestPacket;
import com.chaosbuffalo.mknpc.network.packets.NpcDefinitionClientUpdatePacket;
import com.chaosbuffalo.mknpc.network.packets.OpenWorkspaceScreenPacket;
import com.chaosbuffalo.mknpc.network.packets.OpenMKSpawnerPacket;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.DeleteWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.RequestWorkspacePreflightPacket;
import com.chaosbuffalo.mknpc.network.packets.RequestWorkspacePieceChunkPacket;
import com.chaosbuffalo.mknpc.network.packets.RestoreWorkspaceBackupPacket;
import com.chaosbuffalo.mknpc.network.packets.SetWorkspaceLayerLockPacket;
import com.chaosbuffalo.mknpc.network.packets.SetSpawnListPacket;
import com.chaosbuffalo.mknpc.network.packets.SwapWorkspaceBlockPacket;
import com.chaosbuffalo.mknpc.network.packets.TeleportToWorkspacePiecePacket;
import com.chaosbuffalo.mknpc.network.packets.WorkspacePieceChunkPacket;
import com.chaosbuffalo.mknpc.network.packets.WorkspacePreflightReportPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MKNpc.MODID)
public class PacketHandler {

    private static final String VERSION = "1.0";


    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToServer(
                SetSpawnListPacket.TYPE,
                SetSpawnListPacket.STREAM_CODEC,
                SetSpawnListPacket::handle
        );
        registrar.playToClient(
                OpenMKSpawnerPacket.TYPE,
                OpenMKSpawnerPacket.STREAM_CODEC,
                OpenMKSpawnerPacket::handle
        );
        registrar.playToClient(
                NpcDefinitionClientUpdatePacket.TYPE,
                NpcDefinitionClientUpdatePacket.STREAM_CODEC,
                NpcDefinitionClientUpdatePacket::handle
        );
        registrar.playToServer(
                FinalizeMKSpawnerPacket.TYPE,
                FinalizeMKSpawnerPacket.STREAM_CODEC,
                FinalizeMKSpawnerPacket::handle
        );
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
