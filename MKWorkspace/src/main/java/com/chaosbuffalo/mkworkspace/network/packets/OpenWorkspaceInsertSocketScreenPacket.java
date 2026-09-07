package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertPlacementContext;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public class OpenWorkspaceInsertSocketScreenPacket implements CustomPacketPayload {
    public static final Type<OpenWorkspaceInsertSocketScreenPacket> TYPE =
            new Type<>(MKWorkspace.id("open_workspace_insert_socket_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWorkspaceInsertSocketScreenPacket> STREAM_CODEC =
            StreamCodec.ofMember(OpenWorkspaceInsertSocketScreenPacket::toBytes,
                    OpenWorkspaceInsertSocketScreenPacket::new);

    private final InsertSocketContext socketContext;

    public OpenWorkspaceInsertSocketScreenPacket(MKWorkspaceInsertPlacementContext context) {
        this.socketContext = InsertSocketContext.from(context);
    }

    public OpenWorkspaceInsertSocketScreenPacket(FriendlyByteBuf buffer) {
        this.socketContext = InsertSocketContext.read(buffer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        socketContext.write(buffer);
    }

    public static void handle(OpenWorkspaceInsertSocketScreenPacket packet, IPayloadContext context) {
        MKWorkspaceClientPackets.openWorkspaceInsertSocketScreen(packet.socketContext);
    }

    public record InsertSocketContext(
            BlockPos anchor,
            CompoundTag workspaceTag,
            UUID pieceId,
            String pieceName,
            BlockPos socketWorldPos,
            BlockPos socketLocalPos,
            Direction socketFacing,
            String finalState,
            int pieceWidth,
            int pieceHeight,
            int pieceDepth,
            List<String> compatibleInsertFamilyIds,
            List<OccupiedInsertFootprint> occupiedFootprints
    ) {
        private static InsertSocketContext from(MKWorkspaceInsertPlacementContext context) {
            BoundingBox bounds = context.piece().exportBounds();
            return new InsertSocketContext(
                    context.workspace().anchor(),
                    MKWorkspacePacketPayloads.editableWorkspaceTag(context.workspace()),
                    context.piece().pieceId(),
                    context.piece().pieceName(),
                    context.socketWorldPos(),
                    context.socketLocalPos(),
                    context.socketFacing(),
                    context.finalState(),
                    bounds.getXSpan(),
                    bounds.getYSpan(),
                    bounds.getZSpan(),
                    context.compatibleInsertFamilyIds(),
                    context.occupiedFootprints().stream()
                            .map(OccupiedInsertFootprint::from)
                            .toList()
            );
        }

        private static InsertSocketContext read(FriendlyByteBuf buffer) {
            return new InsertSocketContext(
                    buffer.readBlockPos(),
                    buffer.readNbt(),
                    buffer.readUUID(),
                    buffer.readUtf(),
                    buffer.readBlockPos(),
                    buffer.readBlockPos(),
                    buffer.readEnum(Direction.class),
                    buffer.readUtf(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    readStrings(buffer),
                    readFootprints(buffer)
            );
        }

        private void write(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(anchor);
            buffer.writeNbt(workspaceTag);
            buffer.writeUUID(pieceId);
            buffer.writeUtf(pieceName);
            buffer.writeBlockPos(socketWorldPos);
            buffer.writeBlockPos(socketLocalPos);
            buffer.writeEnum(socketFacing);
            buffer.writeUtf(finalState);
            buffer.writeVarInt(pieceWidth);
            buffer.writeVarInt(pieceHeight);
            buffer.writeVarInt(pieceDepth);
            buffer.writeVarInt(compatibleInsertFamilyIds.size());
            for (String familyId : compatibleInsertFamilyIds) {
                buffer.writeUtf(familyId);
            }
            buffer.writeVarInt(occupiedFootprints.size());
            for (OccupiedInsertFootprint footprint : occupiedFootprints) {
                footprint.write(buffer);
            }
        }

        public MKStructureWorkspace workspace() {
            return MKStructureWorkspace.fromTag(workspaceTag);
        }

        private static List<OccupiedInsertFootprint> readFootprints(FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(ignored -> OccupiedInsertFootprint.read(buffer))
                    .toList();
        }

        private static List<String> readStrings(FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(ignored -> buffer.readUtf())
                    .toList();
        }
    }

    public record OccupiedInsertFootprint(String familyId, BlockPos socketLocalPos, BoundingBox bounds) {
        private static OccupiedInsertFootprint from(
                com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertFootprintScanner.OccupiedInsertFootprint footprint) {
            return new OccupiedInsertFootprint(footprint.familyId(), footprint.socketLocalPos(),
                    footprint.bounds());
        }

        private static OccupiedInsertFootprint read(FriendlyByteBuf buffer) {
            return new OccupiedInsertFootprint(
                    buffer.readUtf(),
                    buffer.readBlockPos(),
                    new BoundingBox(
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt(),
                            buffer.readVarInt()
                    )
            );
        }

        private void write(FriendlyByteBuf buffer) {
            buffer.writeUtf(familyId);
            buffer.writeBlockPos(socketLocalPos);
            buffer.writeVarInt(bounds.minX());
            buffer.writeVarInt(bounds.minY());
            buffer.writeVarInt(bounds.minZ());
            buffer.writeVarInt(bounds.maxX());
            buffer.writeVarInt(bounds.maxY());
            buffer.writeVarInt(bounds.maxZ());
        }
    }
}
