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

public class AddMissingWorkspaceVariantsPacket implements CustomPacketPayload {
    public static final Type<AddMissingWorkspaceVariantsPacket> TYPE =
            new Type<>(MKNpc.id("add_missing_workspace_variants"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddMissingWorkspaceVariantsPacket> STREAM_CODEC =
            StreamCodec.ofMember(AddMissingWorkspaceVariantsPacket::toBytes, AddMissingWorkspaceVariantsPacket::new);

    private final BlockPos anchor;

    public AddMissingWorkspaceVariantsPacket(BlockPos anchor) {
        this.anchor = anchor;
    }

    public AddMissingWorkspaceVariantsPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
    }

    public static void handle(AddMissingWorkspaceVariantsPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        new MKStructureWorkspaceService().addMissingWorkspaceVariantsForAll(player.serverLevel(), packet.anchor)
                .ifPresent(updated -> player.connection.send(new OpenWorkspaceScreenPacket(packet.anchor, updated)));
    }
}
