package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WorkspacePreflightReportPacket implements CustomPacketPayload {
    public static final Type<WorkspacePreflightReportPacket> TYPE =
            new Type<>(MKWorkspace.id("workspace_preflight_report"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkspacePreflightReportPacket> STREAM_CODEC =
            StreamCodec.ofMember(WorkspacePreflightReportPacket::toBytes, WorkspacePreflightReportPacket::new);

    private final BlockPos anchor;
    private final CompoundTag preflightTag;

    public WorkspacePreflightReportPacket(BlockPos anchor, MKWorkspaceMutationPreflight preflight) {
        this.anchor = anchor;
        this.preflightTag = MKWorkspaceCodecs.encodeNbt(MKWorkspaceMutationPreflight.CODEC,
                MKWorkspacePacketPayloads.compactPreflight(preflight), "workspace mutation preflight");
    }

    public WorkspacePreflightReportPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace preflight report packet was missing payload");
        }
        this.preflightTag = tag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeBlockPos(anchor);
        buffer.writeNbt(preflightTag);
        MKWorkspacePacketPayloads.warnIfLarge("workspace_preflight_report", buffer.writerIndex() - startIndex);
    }

    public static void handle(WorkspacePreflightReportPacket packet, IPayloadContext context) {
        MKWorkspaceMutationPreflight preflight = MKWorkspaceCodecs.parseNbt(
                MKWorkspaceMutationPreflight.CODEC, packet.preflightTag, "workspace mutation preflight");
        MKWorkspaceClientPackets.applyPreflight(packet.anchor, preflight);
    }
}
