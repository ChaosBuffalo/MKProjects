package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

public class CreateWorkspacePacket implements CustomPacketPayload {
    public static final Type<CreateWorkspacePacket> TYPE = new Type<>(MKNpc.id("create_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateWorkspacePacket> STREAM_CODEC = StreamCodec.ofMember(
            CreateWorkspacePacket::toBytes, CreateWorkspacePacket::new
    );

    private final CompoundTag workspaceTag;
    private final boolean generateAfterCreate;

    public CreateWorkspacePacket(MKStructureWorkspace workspace) {
        this(workspace, false);
    }

    public CreateWorkspacePacket(MKStructureWorkspace workspace, boolean generateAfterCreate) {
        this.workspaceTag = workspace.toTag();
        this.generateAfterCreate = generateAfterCreate;
    }

    public CreateWorkspacePacket(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace create packet was missing payload");
        }
        this.workspaceTag = tag;
        this.generateAfterCreate = buffer.readBoolean();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(workspaceTag);
        buffer.writeBoolean(generateAfterCreate);
    }

    public static void handle(CreateWorkspacePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspace workspace = MKStructureWorkspace.fromTag(packet.workspaceTag);
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        List<String> errors = service.validateWorkspace(workspace);
        if (!errors.isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, errors);
            return;
        }
        Optional<MKStructureWorkspace> existingOpt = IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(workspace.anchor());
        if (existingOpt.isPresent()) {
            var preflight = service.preflightWorkspaceUpdate(existingOpt.get(), workspace, System.currentTimeMillis());
            List<MKWorkspaceGeneratedLayer> lockedLayers =
                    service.lockedInvalidatedLayers(existingOpt.get(), preflight.report());
            if (!lockedLayers.isEmpty()) {
                MKWorkspaceValidationMessages.displayFailure(player,
                        "Workspace update blocked by locked layer: " +
                                lockedLayers.getFirst().getSerializedName());
                return;
            }
        }
        boolean nonDestructivePreviewRelayout = service.canApplyPreviewMarginRelayout(player.serverLevel(), workspace);
        boolean nonDestructivePaletteSwap = service.canApplyPaletteSwap(player.serverLevel(), workspace);
        boolean nonDestructiveIdentityRename = service.canApplyIdentityRename(player.serverLevel(), workspace);
        boolean nonDestructiveMarginExpansion = service.canApplyMarginExpansion(player.serverLevel(), workspace);
        boolean nonDestructiveHallwayRegeneration = service.canApplyHallwayRoutingRegeneration(player.serverLevel(), workspace);
        boolean nonDestructiveLinkRenderingRefresh = service.canApplyLinkRenderingRefresh(player.serverLevel(), workspace);
        service.createOrUpdateWorkspace(player.serverLevel(), workspace)
                .ifPresentOrElse(created -> {
                    if (packet.generateAfterCreate && !nonDestructivePreviewRelayout && !nonDestructivePaletteSwap &&
                            !nonDestructiveIdentityRename && !nonDestructiveMarginExpansion &&
                            !nonDestructiveHallwayRegeneration && !nonDestructiveLinkRenderingRefresh &&
                            service.generateWorkspace(player.serverLevel(), created.anchor()).isEmpty()) {
                        MKWorkspaceValidationMessages.displayFailure(player, "Workspace generation failed.");
                        return;
                    }
                    service.openWorkspaceScreen(player, created.anchor());
                }, () -> MKWorkspaceValidationMessages.displayFailure(player, "Workspace creation failed."));
    }
}
