package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerStairPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
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

import java.util.List;
import java.util.UUID;

public class CreateWorkspacePacket implements CustomPacketPayload {
    public static final Type<CreateWorkspacePacket> TYPE = new Type<>(MKNpc.id("create_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateWorkspacePacket> STREAM_CODEC = StreamCodec.ofMember(
            CreateWorkspacePacket::toBytes, CreateWorkspacePacket::new
    );

    private final BlockPos anchor;
    private final String namespace;
    private final String structureName;
    private final int roomWidth;
    private final int roomLength;
    private final int entranceHeight;
    private final int roomHeight;
    private final int basementHeight;
    private final int hallwayWidth;
    private final int doorwayWidth;
    private final int doorwayHeight;
    private final MKTowerStairPlacement towerStairPlacement;
    private final int shellMargin;
    private final int exteriorAirMargin;
    private final int previewMargin;
    private final MKWorkspaceStairMode stairMode;
    private final MKWorkspaceStairRiseType stairRiseType;
    private final int stairFlatRunLength;
    private final int stairWidth;
    private final ResourceLocation floorBlock;
    private final ResourceLocation wallBlock;
    private final ResourceLocation ceilingBlock;
    private final ResourceLocation stairBlock;
    private final ResourceLocation slabBlock;
    private final ResourceLocation ladderBlock;

    public CreateWorkspacePacket(BlockPos anchor, String namespace, String structureName, int roomWidth, int roomLength,
                                 int entranceHeight, int roomHeight, int basementHeight, int hallwayWidth, int doorwayWidth,
                                 int doorwayHeight, MKTowerStairPlacement towerStairPlacement, int shellMargin,
                                 int exteriorAirMargin, int previewMargin, MKWorkspaceStairMode stairMode,
                                 MKWorkspaceStairRiseType stairRiseType, int stairFlatRunLength, int stairWidth,
                                 ResourceLocation floorBlock,
                                 ResourceLocation wallBlock, ResourceLocation ceilingBlock, ResourceLocation stairBlock,
                                 ResourceLocation slabBlock, ResourceLocation ladderBlock) {
        this.anchor = anchor;
        this.namespace = namespace;
        this.structureName = structureName;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.entranceHeight = entranceHeight;
        this.roomHeight = roomHeight;
        this.basementHeight = basementHeight;
        this.hallwayWidth = hallwayWidth;
        this.doorwayWidth = doorwayWidth;
        this.doorwayHeight = doorwayHeight;
        this.towerStairPlacement = towerStairPlacement;
        this.shellMargin = shellMargin;
        this.exteriorAirMargin = exteriorAirMargin;
        this.previewMargin = previewMargin;
        this.stairMode = stairMode;
        this.stairRiseType = stairRiseType;
        this.stairFlatRunLength = stairFlatRunLength;
        this.stairWidth = stairWidth;
        this.floorBlock = floorBlock;
        this.wallBlock = wallBlock;
        this.ceilingBlock = ceilingBlock;
        this.stairBlock = stairBlock;
        this.slabBlock = slabBlock;
        this.ladderBlock = ladderBlock;
    }

    public CreateWorkspacePacket(FriendlyByteBuf buffer) {
        this.anchor = buffer.readBlockPos();
        this.namespace = buffer.readUtf();
        this.structureName = buffer.readUtf();
        this.roomWidth = buffer.readInt();
        this.roomLength = buffer.readInt();
        this.entranceHeight = buffer.readInt();
        this.roomHeight = buffer.readInt();
        this.basementHeight = buffer.readInt();
        this.hallwayWidth = buffer.readInt();
        this.doorwayWidth = buffer.readInt();
        this.doorwayHeight = buffer.readInt();
        this.towerStairPlacement = MKTowerStairPlacement.fromSerializedName(buffer.readUtf());
        this.shellMargin = buffer.readInt();
        this.exteriorAirMargin = buffer.readInt();
        this.previewMargin = buffer.readInt();
        this.stairMode = MKWorkspaceStairMode.fromSerializedName(buffer.readUtf());
        this.stairRiseType = MKWorkspaceStairRiseType.fromSerializedName(buffer.readUtf());
        this.stairFlatRunLength = buffer.readInt();
        this.stairWidth = buffer.readInt();
        this.floorBlock = ResourceLocation.parse(buffer.readUtf());
        this.wallBlock = ResourceLocation.parse(buffer.readUtf());
        this.ceilingBlock = ResourceLocation.parse(buffer.readUtf());
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
        buffer.writeUtf(namespace);
        buffer.writeUtf(structureName);
        buffer.writeInt(roomWidth);
        buffer.writeInt(roomLength);
        buffer.writeInt(entranceHeight);
        buffer.writeInt(roomHeight);
        buffer.writeInt(basementHeight);
        buffer.writeInt(hallwayWidth);
        buffer.writeInt(doorwayWidth);
        buffer.writeInt(doorwayHeight);
        buffer.writeUtf(towerStairPlacement.getSerializedName());
        buffer.writeInt(shellMargin);
        buffer.writeInt(exteriorAirMargin);
        buffer.writeInt(previewMargin);
        buffer.writeUtf(stairMode.getSerializedName());
        buffer.writeUtf(stairRiseType.getSerializedName());
        buffer.writeInt(stairFlatRunLength);
        buffer.writeInt(stairWidth);
        buffer.writeUtf(floorBlock.toString());
        buffer.writeUtf(wallBlock.toString());
        buffer.writeUtf(ceilingBlock.toString());
        buffer.writeUtf(stairBlock.toString());
        buffer.writeUtf(slabBlock.toString());
        buffer.writeUtf(ladderBlock.toString());
    }

    public static void handle(CreateWorkspacePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspace workspace = new MKStructureWorkspace(
                UUID.randomUUID(),
                packet.anchor,
                packet.namespace,
                packet.structureName,
                MKStructureFamilyType.TOWER,
                new MKWorkspaceDimensions(packet.roomWidth, packet.roomLength, packet.entranceHeight, packet.roomHeight,
                        packet.basementHeight, packet.hallwayWidth, packet.doorwayWidth,
                        packet.doorwayHeight),
                new MKWorkspaceMaterialPalette(packet.floorBlock, packet.wallBlock, packet.ceilingBlock),
                new MKWorkspaceStairAuthoringConfig(packet.stairMode, packet.stairRiseType, packet.stairFlatRunLength,
                        packet.stairWidth, packet.stairBlock, packet.slabBlock, packet.ladderBlock),
                packet.towerStairPlacement,
                packet.shellMargin,
                packet.exteriorAirMargin,
                packet.previewMargin,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                List.of()
        );
        new MKStructureWorkspaceService().createOrUpdateTowerWorkspace(player.serverLevel(), workspace);
    }
}
