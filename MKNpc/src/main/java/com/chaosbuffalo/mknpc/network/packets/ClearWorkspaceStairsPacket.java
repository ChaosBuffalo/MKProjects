package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClearWorkspaceStairsPacket implements CustomPacketPayload {
    public static final Type<ClearWorkspaceStairsPacket> TYPE = new Type<>(MKNpc.id("clear_workspace_stairs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearWorkspaceStairsPacket> STREAM_CODEC = StreamCodec.ofMember(
            ClearWorkspaceStairsPacket::toBytes, ClearWorkspaceStairsPacket::new
    );

    private final BlockPos anchor;
    private final String pieceName;

    public ClearWorkspaceStairsPacket(BlockPos anchor, String pieceName) {
        this.anchor = anchor;
        this.pieceName = pieceName;
    }

    public ClearWorkspaceStairsPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.pieceName = buffer.readUtf();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(pieceName);
    }

    public static void handle(ClearWorkspaceStairsPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        service.clearWorkspaceStairs(player.serverLevel(), packet.anchor, packet.pieceName)
                .ifPresent(updated -> service.openWorkspaceScreen(player, packet.anchor));
    }
}
