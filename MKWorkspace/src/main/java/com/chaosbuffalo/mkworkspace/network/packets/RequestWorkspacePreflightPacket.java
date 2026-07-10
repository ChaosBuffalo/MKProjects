package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKWorkspacePreflightLogger;
import com.chaosbuffalo.mknpc.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
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

public class RequestWorkspacePreflightPacket implements CustomPacketPayload {
    public static final Type<RequestWorkspacePreflightPacket> TYPE = new Type<>(MKWorkspace.id("request_workspace_preflight"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestWorkspacePreflightPacket> STREAM_CODEC =
            StreamCodec.ofMember(RequestWorkspacePreflightPacket::toBytes, RequestWorkspacePreflightPacket::new);
    private static final String ACCEPTED_REMAPS_KEY = "acceptedRemaps";

    private final CompoundTag workspaceTag;
    private final CompoundTag acceptedRemapsTag;

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace) {
        this(workspace, List.of());
    }

    public RequestWorkspacePreflightPacket(MKStructureWorkspace workspace,
                                           List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        this.workspaceTag = MKWorkspacePacketPayloads.editableWorkspaceTag(workspace);
        this.acceptedRemapsTag = encodeAcceptedRemaps(acceptedRemaps);
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
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeNbt(workspaceTag);
        buffer.writeNbt(acceptedRemapsTag);
        MKWorkspacePacketPayloads.warnIfLarge("request_workspace_preflight", buffer.writerIndex() - startIndex);
    }

    List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps() {
        return decodeAcceptedRemaps(acceptedRemapsTag);
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
        service.preflightWorkspaceUpdate(player.serverLevel(), requested, acceptedRemaps)
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
}
