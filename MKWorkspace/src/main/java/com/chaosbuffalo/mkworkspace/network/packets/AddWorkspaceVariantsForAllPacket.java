package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AddWorkspaceVariantsForAllPacket implements CustomPacketPayload {
    public enum Mode {
        ALL,
        MISSING_ONLY
    }

    public static final Type<AddWorkspaceVariantsForAllPacket> TYPE =
            new Type<>(MKWorkspace.id("add_workspace_variants_for_all"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddWorkspaceVariantsForAllPacket> STREAM_CODEC =
            StreamCodec.ofMember(AddWorkspaceVariantsForAllPacket::toBytes, AddWorkspaceVariantsForAllPacket::new);

    private final BlockPos anchor;
    private final Mode mode;

    public AddWorkspaceVariantsForAllPacket(BlockPos anchor) {
        this(anchor, Mode.ALL);
    }

    public AddWorkspaceVariantsForAllPacket(BlockPos anchor, Mode mode) {
        this.anchor = anchor;
        this.mode = mode;
    }

    public AddWorkspaceVariantsForAllPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.mode = buffer.readEnum(Mode.class);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeEnum(mode);
    }

    public static void handle(AddWorkspaceVariantsForAllPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        var updated = switch (packet.mode) {
            case ALL -> service.addWorkspaceVariantsForAll(player.serverLevel(), packet.anchor);
            case MISSING_ONLY -> service.addMissingWorkspaceVariantsForAll(player.serverLevel(), packet.anchor);
        };
        updated.ifPresent(workspace -> player.connection.send(new OpenWorkspaceScreenPacket(packet.anchor, workspace)));
    }
}
