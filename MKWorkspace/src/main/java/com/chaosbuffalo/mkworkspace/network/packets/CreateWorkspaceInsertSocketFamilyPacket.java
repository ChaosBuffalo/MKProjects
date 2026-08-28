package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertAuthoringService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class CreateWorkspaceInsertSocketFamilyPacket implements CustomPacketPayload {
    public enum Mode {
        CREATE_NEW,
        PLACE_EXISTING
    }

    public static final Type<CreateWorkspaceInsertSocketFamilyPacket> TYPE =
            new Type<>(MKWorkspace.id("create_workspace_insert_socket_family"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateWorkspaceInsertSocketFamilyPacket> STREAM_CODEC =
            StreamCodec.ofMember(CreateWorkspaceInsertSocketFamilyPacket::toBytes,
                    CreateWorkspaceInsertSocketFamilyPacket::new);

    private final BlockPos anchor;
    private final UUID hostPieceId;
    private final BlockPos socketWorldPos;
    private final Direction socketFacing;
    private final Mode mode;
    private final String familyId;
    private final int width;
    private final int height;
    private final int depth;
    private final int faceUOffset;
    private final int faceVOffset;
    private final String hostFinalState;
    private final String templateJigsawFinalState;

    public CreateWorkspaceInsertSocketFamilyPacket(BlockPos anchor, UUID hostPieceId, BlockPos socketWorldPos,
                                                   Direction socketFacing, String familyId, int width, int height,
                                                   int depth, int faceUOffset, int faceVOffset, String hostFinalState,
                                                   String templateJigsawFinalState) {
        this.anchor = anchor;
        this.hostPieceId = hostPieceId;
        this.socketWorldPos = socketWorldPos;
        this.socketFacing = socketFacing;
        this.mode = Mode.CREATE_NEW;
        this.familyId = familyId;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.faceUOffset = faceUOffset;
        this.faceVOffset = faceVOffset;
        this.hostFinalState = hostFinalState;
        this.templateJigsawFinalState = templateJigsawFinalState;
    }

    public static CreateWorkspaceInsertSocketFamilyPacket placeExisting(BlockPos anchor, UUID hostPieceId,
                                                                        BlockPos socketWorldPos,
                                                                        Direction socketFacing,
                                                                        String familyId,
                                                                        String hostFinalState) {
        return new CreateWorkspaceInsertSocketFamilyPacket(anchor, hostPieceId, socketWorldPos, socketFacing,
                Mode.PLACE_EXISTING, familyId, 1, 1, 1, 0, 0, hostFinalState, "minecraft:air");
    }

    private CreateWorkspaceInsertSocketFamilyPacket(BlockPos anchor, UUID hostPieceId, BlockPos socketWorldPos,
                                                    Direction socketFacing, Mode mode, String familyId, int width,
                                                    int height, int depth, int faceUOffset, int faceVOffset,
                                                    String hostFinalState, String templateJigsawFinalState) {
        this.anchor = anchor;
        this.hostPieceId = hostPieceId;
        this.socketWorldPos = socketWorldPos;
        this.socketFacing = socketFacing;
        this.mode = mode;
        this.familyId = familyId;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.faceUOffset = faceUOffset;
        this.faceVOffset = faceVOffset;
        this.hostFinalState = hostFinalState;
        this.templateJigsawFinalState = templateJigsawFinalState;
    }

    public CreateWorkspaceInsertSocketFamilyPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.hostPieceId = buffer.readUUID();
        this.socketWorldPos = buffer.readBlockPos();
        this.socketFacing = buffer.readEnum(Direction.class);
        this.mode = buffer.readEnum(Mode.class);
        this.familyId = buffer.readUtf();
        this.width = buffer.readVarInt();
        this.height = buffer.readVarInt();
        this.depth = buffer.readVarInt();
        this.faceUOffset = buffer.readVarInt();
        this.faceVOffset = buffer.readVarInt();
        this.hostFinalState = buffer.readUtf();
        this.templateJigsawFinalState = buffer.readUtf();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUUID(hostPieceId);
        buffer.writeBlockPos(socketWorldPos);
        buffer.writeEnum(socketFacing);
        buffer.writeEnum(mode);
        buffer.writeUtf(familyId);
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
        buffer.writeVarInt(depth);
        buffer.writeVarInt(faceUOffset);
        buffer.writeVarInt(faceVOffset);
        buffer.writeUtf(hostFinalState);
        buffer.writeUtf(templateJigsawFinalState);
    }

    public static void handle(CreateWorkspaceInsertSocketFamilyPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKWorkspaceInsertAuthoringService service = new MKWorkspaceInsertAuthoringService();
        MKWorkspaceInsertAuthoringService.Result result = switch (packet.mode) {
            case CREATE_NEW -> service.createSocketFamily(player.serverLevel(),
                    new MKWorkspaceInsertAuthoringService.CreateSocketFamilyRequest(
                            packet.anchor,
                            packet.hostPieceId,
                            packet.socketWorldPos,
                            packet.socketFacing,
                            packet.familyId,
                            packet.width,
                            packet.height,
                            packet.depth,
                            packet.faceUOffset,
                            packet.faceVOffset,
                            packet.hostFinalState,
                            packet.templateJigsawFinalState
                    ));
            case PLACE_EXISTING -> service.placeExistingSocketFamily(player.serverLevel(),
                    new MKWorkspaceInsertAuthoringService.PlaceExistingSocketRequest(
                            packet.anchor,
                            packet.hostPieceId,
                            packet.socketWorldPos,
                            packet.socketFacing,
                            packet.familyId,
                            packet.hostFinalState
                    ));
        };
        if (result.context().isPresent()) {
            player.connection.send(new OpenWorkspaceInsertSocketScreenPacket(result.context().get()));
        } else {
            MKWorkspaceValidationMessages.displayValidationErrors(player, result.errors());
        }
    }
}
