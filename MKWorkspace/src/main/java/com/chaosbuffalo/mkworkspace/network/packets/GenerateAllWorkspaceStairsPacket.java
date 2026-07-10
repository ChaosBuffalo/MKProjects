package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class GenerateAllWorkspaceStairsPacket implements CustomPacketPayload {
    public static final Type<GenerateAllWorkspaceStairsPacket> TYPE =
            new Type<>(MKWorkspace.id("generate_all_workspace_stairs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateAllWorkspaceStairsPacket> STREAM_CODEC =
            StreamCodec.ofMember(GenerateAllWorkspaceStairsPacket::toBytes, GenerateAllWorkspaceStairsPacket::new);

    private final BlockPos anchor;

    public GenerateAllWorkspaceStairsPacket(BlockPos anchor) {
        this.anchor = anchor;
    }

    public GenerateAllWorkspaceStairsPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
    }

    public static void handle(GenerateAllWorkspaceStairsPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        service.generateAllWorkspaceStairs(player.serverLevel(), packet.anchor)
                .ifPresent(updated -> {
                    List<String> unresolvedPieces = updated.pieces().stream()
                            .filter(piece -> MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()))
                            .filter(piece -> MKWorkspaceStairMode.NONE.getSerializedName()
                                    .equals(piece.tags().get("generated_stair_mode")))
                            .map(piece -> piece.pieceName() + " [" + piece.roleId() + "]")
                            .toList();
                    if (!unresolvedPieces.isEmpty() && updated.stairConfig().mode() != MKWorkspaceStairMode.NONE) {
                        player.displayClientMessage(Component.literal("Workspace stairs: no valid profile for " +
                                String.join(", ", unresolvedPieces)), false);
                    }
                    service.openWorkspaceScreen(player, packet.anchor);
                });
    }
}
