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

public class AddWorkspaceVariantsForAllPacket implements CustomPacketPayload {
    public static final Type<AddWorkspaceVariantsForAllPacket> TYPE =
            new Type<>(MKNpc.id("add_workspace_variants_for_all"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddWorkspaceVariantsForAllPacket> STREAM_CODEC =
            StreamCodec.ofMember(AddWorkspaceVariantsForAllPacket::toBytes, AddWorkspaceVariantsForAllPacket::new);

    private final BlockPos anchor;

    public AddWorkspaceVariantsForAllPacket(BlockPos anchor) {
        this.anchor = anchor;
    }

    public AddWorkspaceVariantsForAllPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
    }

    public static void handle(AddWorkspaceVariantsForAllPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        new MKStructureWorkspaceService().addTowerWorkspaceVariantsForAll(player.serverLevel(), packet.anchor)
                .ifPresent(updated -> player.connection.send(new OpenWorkspaceScreenPacket(packet.anchor, updated)));
    }
}
