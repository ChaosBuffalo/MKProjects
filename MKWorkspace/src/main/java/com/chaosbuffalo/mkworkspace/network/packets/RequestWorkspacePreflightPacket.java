package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKWorkspacePreflightLogger;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVariantAddition;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public class RequestWorkspacePreflightPacket implements CustomPacketPayload {
    public static final Type<RequestWorkspacePreflightPacket> TYPE = new Type<>(MKWorkspace.id("request_workspace_preflight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorkspacePreflightPacket> STREAM_CODEC =
            StreamCodec.ofMember(RequestWorkspacePreflightPacket::toBytes, RequestWorkspacePreflightPacket::new);
    private static final String ACCEPTED_REMAPS_KEY = "acceptedRemaps";

    private final CompoundTag workspaceTag;
    private final CompoundTag acceptedRemapsTag;
    private final List<MKWorkspaceVariantAddition> addedVariants;
    private final List<UUID> deletedVariantPieceIds;
    private final boolean workspaceSettingsDirty;

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace) {
        this(workspace, List.of());
    }

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace,
                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        this(workspace, acceptedRemaps, List.of());
    }

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace,
                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                           List<UUID> deletedVariantPieceIds) {
        this(workspace, acceptedRemaps, List.of(), deletedVariantPieceIds);
    }

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace,
                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                           List<MKWorkspaceVariantAddition> addedVariants,
                                           List<UUID> deletedVariantPieceIds) {
        this(workspace, acceptedRemaps, addedVariants, deletedVariantPieceIds, true);
    }

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace,
                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps,
                                           List<MKWorkspaceVariantAddition> addedVariants,
                                           List<UUID> deletedVariantPieceIds,
                                           boolean workspaceSettingsDirty) {
        this.workspaceTag = MKWorkspacePacketPayloads.editableWorkspaceTag(workspace);
        this.acceptedRemapsTag = encodeAcceptedRemaps(acceptedRemaps);
        this.addedVariants = List.copyOf(addedVariants);
        this.deletedVariantPieceIds = List.copyOf(deletedVariantPieceIds);
        this.workspaceSettingsDirty = workspaceSettingsDirty;
    }

    public RequestWorkspacePreflightPacket(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace preflight packet was missing payload");
        }
        this.workspaceTag = tag;
        CompoundTag remapsTag = buffer.readNbt();
        if (remapsTag == null) {
            throw new IllegalStateException("workspace preflight packet was missing accepted remaps payload");
        }
        this.acceptedRemapsTag = remapsTag;
        this.addedVariants = readAddedVariants(buffer);
        this.deletedVariantPieceIds = readDeletedVariantPieceIds(buffer);
        this.workspaceSettingsDirty = buffer.readBoolean();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeNbt(workspaceTag);
        buffer.writeNbt(acceptedRemapsTag);
        writeAddedVariants(buffer, addedVariants);
        writeDeletedVariantPieceIds(buffer, deletedVariantPieceIds);
        buffer.writeBoolean(workspaceSettingsDirty);
        MKWorkspacePacketPayloads.warnIfLarge("request_workspace_preflight", buffer.writerIndex() - startIndex);
    }

    List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps() {
        return decodeAcceptedRemaps(acceptedRemapsTag);
    }

    List<UUID> deletedVariantPieceIds() {
        return List.copyOf(deletedVariantPieceIds);
    }

    List<MKWorkspaceVariantAddition> addedVariants() {
        return List.copyOf(addedVariants);
    }

    boolean workspaceSettingsDirty() {
        return workspaceSettingsDirty;
    }

    public static void handle(RequestWorkspacePreflightPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspace requested = MKStructureWorkspace.fromTag(packet.workspaceTag);
        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps = packet.acceptedRemaps();
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        List<String> errors = service.validateWorkspace(requested);
        if (!errors.isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, errors);
            return;
        }
        service.preflightWorkspaceUpdate(player.serverLevel(), requested, acceptedRemaps,
                        packet.addedVariants,
                        packet.deletedVariantPieceIds,
                        packet.workspaceSettingsDirty)
                .ifPresentOrElse(
                        preflight -> {
                            new MKWorkspacePreflightLogger().logConfirmEffects("confirm", player, requested,
                                    preflight, acceptedRemaps);
                            PacketDistributor.sendToPlayer(player,
                                    new WorkspacePreflightReportPacket(requested.anchor(), preflight));
                        },
                        () -> MKWorkspaceValidationMessages.displayFailure(player,
                                "Workspace preflight failed: no workspace found at this anchor.")
                );
    }

    private static CompoundTag encodeAcceptedRemaps(List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        Tag remapList = MKWorkspaceTemplateRemapSuggestion.CODEC.listOf()
                .encodeStart(NbtOps.INSTANCE, acceptedRemaps)
                .resultOrPartial(error -> MKWorkspace.LOGGER.error("Failed to encode accepted workspace template remaps: {}",
                        error))
                .orElseThrow(() -> new IllegalStateException("Failed to encode accepted workspace template remaps"));
        CompoundTag wrapper = new CompoundTag();
        wrapper.put(ACCEPTED_REMAPS_KEY, remapList);
        return wrapper;
    }

    private static List<MKWorkspaceTemplateRemapSuggestion> decodeAcceptedRemaps(CompoundTag tag) {
        Tag remapList = tag.get(ACCEPTED_REMAPS_KEY);
        if (remapList == null) {
            return List.of();
        }
        return MKWorkspaceTemplateRemapSuggestion.CODEC.listOf()
                .parse(NbtOps.INSTANCE, remapList)
                .resultOrPartial(error -> MKWorkspace.LOGGER.error("Failed to parse accepted workspace template remaps: {}",
                        error))
                .orElseThrow(() -> new IllegalStateException("Failed to parse accepted workspace template remaps"));
    }

    private static void writeAddedVariants(FriendlyByteBuf buffer, List<MKWorkspaceVariantAddition> addedVariants) {
        buffer.writeVarInt(addedVariants.size());
        for (MKWorkspaceVariantAddition addition : addedVariants) {
            buffer.writeUtf(addition.basePieceName());
            buffer.writeBoolean(addition.sourcePieceName() != null);
            if (addition.sourcePieceName() != null) {
                buffer.writeUtf(addition.sourcePieceName());
            }
        }
    }

    private static List<MKWorkspaceVariantAddition> readAddedVariants(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        java.util.ArrayList<MKWorkspaceVariantAddition> additions = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String basePieceName = buffer.readUtf();
            String sourcePieceName = buffer.readBoolean() ? buffer.readUtf() : null;
            additions.add(new MKWorkspaceVariantAddition(basePieceName, sourcePieceName));
        }
        return List.copyOf(additions);
    }

    private static void writeDeletedVariantPieceIds(FriendlyByteBuf buffer, List<UUID> pieceIds) {
        buffer.writeVarInt(pieceIds.size());
        for (UUID pieceId : pieceIds) {
            buffer.writeUUID(pieceId);
        }
    }

    private static List<UUID> readDeletedVariantPieceIds(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        java.util.ArrayList<UUID> pieceIds = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pieceIds.add(buffer.readUUID());
        }
        return List.copyOf(pieceIds);
    }
}
