package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceExportResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ExportWorkspacePiecesPacket implements CustomPacketPayload {
    public static final Type<ExportWorkspacePiecesPacket> TYPE =
            new Type<>(MKWorkspace.id("export_workspace_pieces"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExportWorkspacePiecesPacket> STREAM_CODEC =
            StreamCodec.ofMember(ExportWorkspacePiecesPacket::toBytes, ExportWorkspacePiecesPacket::new);

    private final BlockPos anchor;

    public ExportWorkspacePiecesPacket(BlockPos anchor) {
        this.anchor = anchor;
    }

    public ExportWorkspacePiecesPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
    }

    public static void handle(ExportWorkspacePiecesPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKWorkspaceExportResult result = new MKStructureWorkspaceService()
                .exportWorkspacePieces(player.serverLevel(), packet.anchor)
                .orElse(null);
        if (result == null) {
            player.displayClientMessage(Component.translatable("mknpc.workspace.message.export_failed"), false);
            return;
        }
        player.displayClientMessage(Component.translatable("mknpc.workspace.message.exported_pieces",
                result.savedPieceCount(), result.metadataCount(), result.archivePath().toString()), false);
    }
}
