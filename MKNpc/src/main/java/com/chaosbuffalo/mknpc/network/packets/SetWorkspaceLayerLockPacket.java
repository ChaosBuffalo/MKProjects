package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SetWorkspaceLayerLockPacket implements CustomPacketPayload {
    public static final Type<SetWorkspaceLayerLockPacket> TYPE = new Type<>(MKNpc.id("set_workspace_layer_lock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetWorkspaceLayerLockPacket> STREAM_CODEC =
            StreamCodec.ofMember(SetWorkspaceLayerLockPacket::toBytes, SetWorkspaceLayerLockPacket::new);

    private final BlockPos anchor;
    private final MKWorkspaceGeneratedLayer layer;
    private final boolean locked;

    public SetWorkspaceLayerLockPacket(BlockPos anchor, MKWorkspaceGeneratedLayer layer, boolean locked) {
        this.anchor = anchor;
        this.layer = layer;
        this.locked = locked;
    }

    public SetWorkspaceLayerLockPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.layer = MKWorkspaceGeneratedLayer.fromSerializedName(buffer.readUtf())
                .orElseThrow(() -> new IllegalStateException("unknown workspace layer in lock packet"));
        this.locked = buffer.readBoolean();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(layer.getSerializedName());
        buffer.writeBoolean(locked);
    }

    public static void handle(SetWorkspaceLayerLockPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        service.setLayerLocked(player.serverLevel(), packet.anchor, packet.layer, packet.locked)
                .ifPresentOrElse(
                        workspace -> service.openWorkspaceScreen(player, packet.anchor),
                        () -> MKWorkspaceValidationMessages.displayFailure(player,
                                "Workspace layer lock failed: no workspace found at this anchor.")
                );
    }
}
