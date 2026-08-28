package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertOverlaySnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.stream.IntStream;

public class WorkspaceInsertOverlayPacket implements CustomPacketPayload {
    public static final Type<WorkspaceInsertOverlayPacket> TYPE =
            new Type<>(MKWorkspace.id("workspace_insert_overlay"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkspaceInsertOverlayPacket> STREAM_CODEC =
            StreamCodec.ofMember(WorkspaceInsertOverlayPacket::toBytes, WorkspaceInsertOverlayPacket::new);

    private final List<MKWorkspaceInsertOverlaySnapshot.Entry> entries;

    public WorkspaceInsertOverlayPacket(MKWorkspaceInsertOverlaySnapshot snapshot) {
        this(snapshot.entries());
    }

    public WorkspaceInsertOverlayPacket(List<MKWorkspaceInsertOverlaySnapshot.Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public WorkspaceInsertOverlayPacket(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        this.entries = IntStream.range(0, count)
                .mapToObj(ignored -> readEntry(buffer))
                .toList();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeVarInt(entries.size());
        for (MKWorkspaceInsertOverlaySnapshot.Entry entry : entries) {
            writeEntry(buffer, entry);
        }
        MKWorkspacePacketPayloads.warnIfLarge("workspace_insert_overlay", buffer.writerIndex() - startIndex);
    }

    public static void handle(WorkspaceInsertOverlayPacket packet, IPayloadContext context) {
        MKWorkspaceClientPackets.applyInsertOverlay(packet.entries);
    }

    private static MKWorkspaceInsertOverlaySnapshot.Entry readEntry(FriendlyByteBuf buffer) {
        String familyId = buffer.readUtf();
        BlockPos socketWorldPos = buffer.readBlockPos();
        BoundingBox bounds = new BoundingBox(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
        Direction front = buffer.readEnum(Direction.class);
        Direction top = buffer.readEnum(Direction.class);
        return new MKWorkspaceInsertOverlaySnapshot.Entry(familyId, socketWorldPos, bounds, front, top);
    }

    private static void writeEntry(FriendlyByteBuf buffer, MKWorkspaceInsertOverlaySnapshot.Entry entry) {
        BoundingBox bounds = entry.bounds();
        buffer.writeUtf(entry.familyId());
        buffer.writeBlockPos(entry.socketWorldPos());
        buffer.writeVarInt(bounds.minX());
        buffer.writeVarInt(bounds.minY());
        buffer.writeVarInt(bounds.minZ());
        buffer.writeVarInt(bounds.maxX());
        buffer.writeVarInt(bounds.maxY());
        buffer.writeVarInt(bounds.maxZ());
        buffer.writeEnum(entry.front());
        buffer.writeEnum(entry.top());
    }
}
