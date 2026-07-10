package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class LoadWorkspaceFromManifestPacket implements CustomPacketPayload {
    public static final Type<LoadWorkspaceFromManifestPacket> TYPE = new Type<>(MKWorkspace.id("load_workspace_from_manifest"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LoadWorkspaceFromManifestPacket> STREAM_CODEC = StreamCodec.ofMember(
            LoadWorkspaceFromManifestPacket::toBytes, LoadWorkspaceFromManifestPacket::new
    );

    private final BlockPos anchor;
    private final ResourceLocation manifestId;

    public LoadWorkspaceFromManifestPacket(BlockPos anchor, ResourceLocation manifestId) {
        this.anchor = anchor;
        this.manifestId = manifestId;
    }

    public LoadWorkspaceFromManifestPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.manifestId = ResourceLocation.parse(buffer.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(manifestId.toString());
    }

    public static void handle(LoadWorkspaceFromManifestPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        MKStructureWorkspaceService.MKWorkspaceImportResponse response =
                service.importWorkspaceFromManifestWithValidation(player.serverLevel(), packet.anchor, packet.manifestId);
        if (!response.validationErrors().isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, response.validationErrors());
            return;
        }
        if (response.workspaceOpt().isPresent()) {
            service.openWorkspaceScreen(player, packet.anchor);
        } else {
            MKWorkspaceValidationMessages.displayFailure(player, "Workspace import failed.");
        }
    }
}
