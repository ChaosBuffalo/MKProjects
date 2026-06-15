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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class RequestWorkspacePreflightPacket implements CustomPacketPayload {
    public static final Type<RequestWorkspacePreflightPacket> TYPE = new Type<>(MKNpc.id("request_workspace_preflight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorkspacePreflightPacket> STREAM_CODEC =
            StreamCodec.ofMember(RequestWorkspacePreflightPacket::toBytes, RequestWorkspacePreflightPacket::new);

    private final CompoundTag workspaceTag;

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace) {
        this.workspaceTag = workspace.toTag();
    }

    public RequestWorkspacePreflightPacket(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace preflight packet was missing payload");
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

    public static void handle(RequestWorkspacePreflightPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspace requested = MKStructureWorkspace.fromTag(packet.workspaceTag);
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        List<String> errors = service.validateWorkspace(requested);
        if (!errors.isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, errors);
            return;
        }
        service.preflightWorkspaceUpdate(player.serverLevel(), requested)
                .ifPresentOrElse(
                        preflight -> PacketDistributor.sendToPlayer(player,
                                new WorkspacePreflightReportPacket(requested.anchor(), preflight)),
                        () -> MKWorkspaceValidationMessages.displayFailure(player,
                                "Workspace preflight failed: no workspace found at this anchor.")
                );
    }
}
