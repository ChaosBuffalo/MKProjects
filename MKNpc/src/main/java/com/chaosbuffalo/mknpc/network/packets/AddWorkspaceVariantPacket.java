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

public class AddWorkspaceVariantPacket implements CustomPacketPayload {
    public static final Type<AddWorkspaceVariantPacket> TYPE = new Type<>(MKNpc.id("add_workspace_variant"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddWorkspaceVariantPacket> STREAM_CODEC = StreamCodec.ofMember(
            AddWorkspaceVariantPacket::toBytes, AddWorkspaceVariantPacket::new
    );

    private final BlockPos anchor;
    private final String basePieceName;

    public AddWorkspaceVariantPacket(BlockPos anchor, String basePieceName) {
        this.anchor = anchor;
        this.basePieceName = basePieceName;
    }

    public AddWorkspaceVariantPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.basePieceName = buffer.readUtf();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(basePieceName);
    }

    public static void handle(AddWorkspaceVariantPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        new MKStructureWorkspaceService().addTowerWorkspaceVariant(player.serverLevel(), packet.anchor, packet.basePieceName)
                .ifPresent(updated -> player.connection.send(new OpenWorkspaceScreenPacket(packet.anchor, updated)));
    }
}
