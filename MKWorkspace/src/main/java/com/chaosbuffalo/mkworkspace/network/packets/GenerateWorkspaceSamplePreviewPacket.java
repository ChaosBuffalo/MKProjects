package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.preview.MKWorkspaceSamplePreviewService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenerateWorkspaceSamplePreviewPacket implements CustomPacketPayload {
    public static final Type<GenerateWorkspaceSamplePreviewPacket> TYPE =
            new Type<>(MKWorkspace.id("generate_workspace_sample_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateWorkspaceSamplePreviewPacket> STREAM_CODEC =
            StreamCodec.ofMember(GenerateWorkspaceSamplePreviewPacket::toBytes,
                    GenerateWorkspaceSamplePreviewPacket::new);

    private final BlockPos anchor;
    private final boolean lockSeed;

    public GenerateWorkspaceSamplePreviewPacket(BlockPos anchor, boolean lockSeed) {
        this.anchor = anchor;
        this.lockSeed = lockSeed;
    }

    public GenerateWorkspaceSamplePreviewPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.lockSeed = buffer.readBoolean();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeBoolean(lockSeed);
    }

    public static void handle(GenerateWorkspaceSamplePreviewPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        Optional<MKStructureWorkspace> workspaceOpt =
                IMKStructureWorkspaceData.get(player.serverLevel()).getWorkspaceByAnchor(packet.anchor);
        if (workspaceOpt.isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Workspace sample preview failed: no workspace found at this anchor.");
            return;
        }

        MKStructureWorkspaceService workspaceService = new MKStructureWorkspaceService();
        List<String> validationErrors = workspaceService.validateWorkspace(workspaceOpt.get());
        if (!validationErrors.isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, validationErrors);
            return;
        }

        ArrayList<String> errors = new ArrayList<>();
        new MKWorkspaceSamplePreviewService()
                .generate(player.serverLevel(), workspaceOpt.get(), packet.lockSeed, errors)
                .ifPresentOrElse(result -> {
                    player.displayClientMessage(Component.literal("Workspace sample preview generated " +
                            result.placedPieceCount() + " piece(s), seed " + result.seed() +
                            (result.seedLocked() ? " (locked)" : "") +
                            (result.templateFallbackCount() > 0
                                    ? ", " + result.templateFallbackCount() + " template fallback(s)"
                                    : "")), false);
                    for (String warning : result.warnings()) {
                        player.displayClientMessage(Component.literal(" - " + warning), false);
                    }
                    workspaceService.openWorkspaceScreen(player, packet.anchor);
                }, () -> {
                    if (errors.isEmpty()) {
                        MKWorkspaceValidationMessages.displayFailure(player, "Workspace sample preview failed.");
                    } else {
                        MKWorkspaceValidationMessages.displayValidationErrors(player, errors);
                    }
                });
    }
}
