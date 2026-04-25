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

public class GenerateWorkspacePacket implements CustomPacketPayload {
    public static final Type<GenerateWorkspacePacket> TYPE = new Type<>(MKNpc.id("generate_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateWorkspacePacket> STREAM_CODEC = StreamCodec.ofMember(
            GenerateWorkspacePacket::toBytes, GenerateWorkspacePacket::new
    );

    private final BlockPos anchor;

    public GenerateWorkspacePacket(BlockPos anchor) {
        this.anchor = anchor;
    }

    public GenerateWorkspacePacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
    }

    public static void handle(GenerateWorkspacePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        new MKStructureWorkspaceService().generateTowerWorkspace(player.serverLevel(), packet.anchor);
    }
}
