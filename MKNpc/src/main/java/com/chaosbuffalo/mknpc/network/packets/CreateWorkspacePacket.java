package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CreateWorkspacePacket implements CustomPacketPayload {
    public static final Type<CreateWorkspacePacket> TYPE = new Type<>(MKNpc.id("create_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateWorkspacePacket> STREAM_CODEC = StreamCodec.ofMember(
            CreateWorkspacePacket::toBytes, CreateWorkspacePacket::new
    );

    private final CompoundTag workspaceTag;

    public CreateWorkspacePacket(MKStructureWorkspace workspace) {
        this.workspaceTag = workspace.toTag();
    }

    public CreateWorkspacePacket(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace create packet was missing payload");
        }
        this.workspaceTag = tag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(workspaceTag);
    }

    public static void handle(CreateWorkspacePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspace workspace = MKStructureWorkspace.fromTag(packet.workspaceTag);
        new MKStructureWorkspaceService().createOrUpdateTowerWorkspace(player.serverLevel(), workspace);
    }
}
