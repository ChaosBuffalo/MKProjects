package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DeleteWorkspacePacket implements CustomPacketPayload {
    public static final Type<DeleteWorkspacePacket> TYPE = new Type<>(MKWorkspace.id("delete_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DeleteWorkspacePacket> STREAM_CODEC =
            StreamCodec.ofMember(DeleteWorkspacePacket::toBytes, DeleteWorkspacePacket::new);

    private final BlockPos anchor;

    public DeleteWorkspacePacket(BlockPos anchor) {
        this.anchor = anchor;
    }

    public DeleteWorkspacePacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
    }

    public static void handle(DeleteWorkspacePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        if (!new MKStructureWorkspaceService().deleteWorkspace(player.serverLevel(), packet.anchor)) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Workspace delete failed: no workspace found at this anchor.");
            return;
        }
        player.displayClientMessage(Component.literal("Deleted workspace."), false);
    }
}
