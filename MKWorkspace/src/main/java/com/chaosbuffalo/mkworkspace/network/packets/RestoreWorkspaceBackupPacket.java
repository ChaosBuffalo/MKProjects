package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceBackupRestoreService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.Optional;

public class RestoreWorkspaceBackupPacket implements CustomPacketPayload {
    public static final Type<RestoreWorkspaceBackupPacket> TYPE =
            new Type<>(MKWorkspace.id("restore_workspace_backup"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RestoreWorkspaceBackupPacket> STREAM_CODEC =
            StreamCodec.ofMember(RestoreWorkspaceBackupPacket::toBytes, RestoreWorkspaceBackupPacket::new);

    private final BlockPos anchor;
    private final String fileName;

    public RestoreWorkspaceBackupPacket(BlockPos anchor, String fileName) {
        this.anchor = anchor;
        this.fileName = fileName;
    }

    public RestoreWorkspaceBackupPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.fileName = buffer.readUtf();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(fileName);
    }

    public static void handle(RestoreWorkspaceBackupPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        Optional<MKStructureWorkspace> workspaceOpt =
                IMKStructureWorkspaceData.get(player.serverLevel()).getWorkspaceByAnchor(packet.anchor);
        if (workspaceOpt.isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Backup restore failed: no workspace found at this anchor.");
            return;
        }
        try {
            MKWorkspaceBackupRestoreService.RestoreResult result =
                    new MKWorkspaceBackupRestoreService().restoreByFileName(
                            player.serverLevel(), workspaceOpt.get(), packet.fileName);
            if (!result.validationErrors().isEmpty()) {
                MKWorkspaceValidationMessages.displayValidationErrors(player, result.validationErrors());
                return;
            }
            if (result.workspaceOpt().isEmpty()) {
                MKWorkspaceValidationMessages.displayFailure(player,
                        "Backup file not found for this workspace: " + packet.fileName);
                return;
            }
            String blockRestoreSuffix = result.blockRestoreStatsOpt()
                    .map(stats -> " and " + stats.restoredPieceCount() + " saved piece NBT files")
                    .orElse(" metadata only; no piece snapshots were present");
            player.displayClientMessage(Component.literal("Restored workspace backup: " + packet.fileName +
                    blockRestoreSuffix), false);
            new MKStructureWorkspaceService().openWorkspaceScreen(player, packet.anchor);
        } catch (IOException e) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Backup restore failed while restoring backup data: " + e.getMessage());
        }
    }
}
