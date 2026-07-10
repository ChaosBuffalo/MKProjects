package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

public class GenerateWorkspacePacket implements CustomPacketPayload {
    public static final Type<GenerateWorkspacePacket> TYPE = new Type<>(MKWorkspace.id("generate_workspace"));
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
        Optional<MKStructureWorkspace> workspaceOpt =
                IMKStructureWorkspaceData.get(player.serverLevel()).getWorkspaceByAnchor(packet.anchor);
        if (workspaceOpt.isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Workspace generation failed: no workspace found at this anchor.");
            return;
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        List<String> errors = service.validateWorkspace(workspaceOpt.get());
        if (!errors.isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, errors);
            return;
        }
        if (service.generateWorkspace(player.serverLevel(), packet.anchor).isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player, "Workspace generation failed.");
            return;
        }
        service.openWorkspaceScreen(player, packet.anchor);
    }
}
