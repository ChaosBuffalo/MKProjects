package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenWorkspaceScreenPacket implements CustomPacketPayload {
    public static final Type<OpenWorkspaceScreenPacket> TYPE = new Type<>(MKNpc.id("open_workspace_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWorkspaceScreenPacket> STREAM_CODEC = StreamCodec.ofMember(
            OpenWorkspaceScreenPacket::toBytes, OpenWorkspaceScreenPacket::new
    );

    private final BlockPos anchor;
    private final CompoundTag workspaceTag;
    private final java.util.List<String> importManifestIds;
    private final java.util.List<String> backupManifestFiles;

    public OpenWorkspaceScreenPacket(BlockPos anchor, MKStructureWorkspace workspace) {
        this(anchor, workspace, java.util.List.of(), java.util.List.of());
    }

    public OpenWorkspaceScreenPacket(BlockPos anchor, MKStructureWorkspace workspace, java.util.List<String> importManifestIds) {
        this(anchor, workspace, importManifestIds, java.util.List.of());
    }

    public OpenWorkspaceScreenPacket(BlockPos anchor, MKStructureWorkspace workspace, java.util.List<String> importManifestIds,
                                     java.util.List<String> backupManifestFiles) {
        this.anchor = anchor;
        this.workspaceTag = workspace != null ? workspace.toTag() : null;
        this.importManifestIds = java.util.List.copyOf(importManifestIds);
        this.backupManifestFiles = java.util.List.copyOf(backupManifestFiles);
    }

    public OpenWorkspaceScreenPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.workspaceTag = buffer.readBoolean() ? buffer.readNbt() : null;
        this.importManifestIds = buffer.readList(FriendlyByteBuf::readUtf);
        this.backupManifestFiles = buffer.readList(FriendlyByteBuf::readUtf);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeBoolean(workspaceTag != null);
        if (workspaceTag != null) {
            buffer.writeNbt(workspaceTag);
        }
        buffer.writeCollection(importManifestIds, FriendlyByteBuf::writeUtf);
        buffer.writeCollection(backupManifestFiles, FriendlyByteBuf::writeUtf);
    }

    public static void handle(OpenWorkspaceScreenPacket packet, IPayloadContext context) {
        MKStructureWorkspace workspace = packet.workspaceTag != null ? MKStructureWorkspace.fromTag(packet.workspaceTag) : null;
        if (Minecraft.getInstance().screen instanceof MKWorkspaceScreen current) {
            Minecraft.getInstance().setScreen(current.copyWithWorkspace(workspace, packet.importManifestIds,
                    packet.backupManifestFiles));
        } else {
            Minecraft.getInstance().setScreen(new MKWorkspaceScreen(packet.anchor, workspace, packet.importManifestIds,
                    packet.backupManifestFiles));
        }
    }
}
