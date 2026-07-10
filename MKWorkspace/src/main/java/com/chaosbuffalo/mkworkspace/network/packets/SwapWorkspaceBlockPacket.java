package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.mutation.MKStructureWorkspaceMutationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class SwapWorkspaceBlockPacket implements CustomPacketPayload {
    public static final Type<SwapWorkspaceBlockPacket> TYPE = new Type<>(MKWorkspace.id("swap_workspace_block"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwapWorkspaceBlockPacket> STREAM_CODEC =
            StreamCodec.ofMember(SwapWorkspaceBlockPacket::toBytes, SwapWorkspaceBlockPacket::new);

    private final BlockPos anchor;
    private final ResourceLocation source;
    private final ResourceLocation target;

    public SwapWorkspaceBlockPacket(BlockPos anchor, ResourceLocation source, ResourceLocation target) {
        this.anchor = anchor;
        this.source = source;
        this.target = target;
    }

    public SwapWorkspaceBlockPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.source = ResourceLocation.parse(buffer.readUtf());
        this.target = ResourceLocation.parse(buffer.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(source.toString());
        buffer.writeUtf(target.toString());
    }

    public static void handle(SwapWorkspaceBlockPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        if (BuiltInRegistries.BLOCK.getOptional(packet.source).isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player, "Unknown source block: " + packet.source);
            return;
        }
        if (BuiltInRegistries.BLOCK.getOptional(packet.target).isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player, "Unknown target block: " + packet.target);
            return;
        }
        Optional<MKStructureWorkspace> workspaceOpt =
                IMKStructureWorkspaceData.get(player.serverLevel()).getWorkspaceByAnchor(packet.anchor);
        if (workspaceOpt.isEmpty()) {
            MKWorkspaceValidationMessages.displayFailure(player, "Block swap failed: no workspace found at this anchor.");
            return;
        }
        try {
            MKStructureWorkspaceMutationService.WorkspaceBlockSwapResult result =
                    new MKStructureWorkspaceMutationService().swapBlocks(
                            player.serverLevel(), workspaceOpt.get(), Map.of(packet.source, packet.target));
            player.displayClientMessage(Component.literal("Swapped " + result.replacedCount() + " blocks across " +
                    result.pieceCount() + " pieces. Backup manifest: " + result.backupPath()), false);
            new MKStructureWorkspaceService().openWorkspaceScreen(player, packet.anchor);
        } catch (IOException e) {
            MKWorkspaceValidationMessages.displayFailure(player,
                    "Block swap failed while writing backup manifest: " + e.getMessage());
        }
    }
}
