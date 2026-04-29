package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GenerateWorkspaceStairsPacket implements CustomPacketPayload {
    public static final Type<GenerateWorkspaceStairsPacket> TYPE = new Type<>(MKNpc.id("generate_workspace_stairs"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GenerateWorkspaceStairsPacket> STREAM_CODEC = StreamCodec.ofMember(
            GenerateWorkspaceStairsPacket::toBytes, GenerateWorkspaceStairsPacket::new
    );

    private final BlockPos anchor;
    private final String pieceName;
    private final MKWorkspaceStairMode stairMode;
    private final MKWorkspaceStairRiseType stairRiseType;
    private final int stairWidth;
    private final ResourceLocation stairBlock;
    private final ResourceLocation slabBlock;
    private final ResourceLocation ladderBlock;

    public GenerateWorkspaceStairsPacket(BlockPos anchor, String pieceName, MKWorkspaceStairMode stairMode,
                                         MKWorkspaceStairRiseType stairRiseType, int stairWidth,
                                         ResourceLocation stairBlock, ResourceLocation slabBlock,
                                         ResourceLocation ladderBlock) {
        this.anchor = anchor;
        this.pieceName = pieceName;
        this.stairMode = stairMode;
        this.stairRiseType = stairRiseType;
        this.stairWidth = stairWidth;
        this.stairBlock = stairBlock;
        this.slabBlock = slabBlock;
        this.ladderBlock = ladderBlock;
    }

    public GenerateWorkspaceStairsPacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.pieceName = buffer.readUtf();
        this.stairMode = MKWorkspaceStairMode.fromSerializedName(buffer.readUtf());
        this.stairRiseType = MKWorkspaceStairRiseType.fromSerializedName(buffer.readUtf());
        this.stairWidth = buffer.readInt();
        this.stairBlock = ResourceLocation.parse(buffer.readUtf());
        this.slabBlock = ResourceLocation.parse(buffer.readUtf());
        this.ladderBlock = ResourceLocation.parse(buffer.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(anchor);
        buffer.writeUtf(pieceName);
        buffer.writeUtf(stairMode.getSerializedName());
        buffer.writeUtf(stairRiseType.getSerializedName());
        buffer.writeInt(stairWidth);
        buffer.writeUtf(stairBlock.toString());
        buffer.writeUtf(slabBlock.toString());
        buffer.writeUtf(ladderBlock.toString());
    }

    public static void handle(GenerateWorkspaceStairsPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        new MKStructureWorkspaceService().generateTowerWorkspaceStairs(player.serverLevel(), packet.anchor, packet.pieceName,
                new MKWorkspaceStairAuthoringConfig(packet.stairMode, packet.stairRiseType, packet.stairWidth,
                        packet.stairBlock, packet.slabBlock, packet.ladderBlock))
                .ifPresent(updated -> player.connection.send(new OpenWorkspaceScreenPacket(packet.anchor, updated)));
    }
}
