package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKWorkspacePreflightLogger;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

public class CreateWorkspacePacket implements CustomPacketPayload {
    public static final Type<CreateWorkspacePacket> TYPE = new Type<>(MKWorkspace.id("create_workspace"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreateWorkspacePacket> STREAM_CODEC = StreamCodec.ofMember(
            CreateWorkspacePacket::toBytes, CreateWorkspacePacket::new
    );

    private final CompoundTag workspaceTag;
    private final boolean generateAfterCreate;
    private final boolean fullRegenerate;
    private final List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps;

    public CreateWorkspacePacket(MKStructureWorkspace workspace) {
        this(workspace, false);
    }

    public CreateWorkspacePacket(MKStructureWorkspace workspace, boolean generateAfterCreate) {
        this(workspace, generateAfterCreate, List.of());
    }

    public CreateWorkspacePacket(MKStructureWorkspace workspace, boolean generateAfterCreate,
                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        this(workspace, generateAfterCreate, false, acceptedRemaps);
    }

    public CreateWorkspacePacket(MKStructureWorkspace workspace, boolean generateAfterCreate,
                                 boolean fullRegenerate,
                                 List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        this.workspaceTag = MKWorkspacePacketPayloads.editableWorkspaceTag(workspace);
        this.generateAfterCreate = generateAfterCreate;
        this.fullRegenerate = fullRegenerate;
        this.acceptedRemaps = List.copyOf(acceptedRemaps);
    }

    public CreateWorkspacePacket(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new IllegalStateException("workspace create packet was missing payload");
        }
        this.workspaceTag = tag;
        this.generateAfterCreate = buffer.readBoolean();
        this.fullRegenerate = buffer.readBoolean();
        this.acceptedRemaps = readAcceptedRemaps(buffer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        int startIndex = buffer.writerIndex();
        buffer.writeNbt(workspaceTag);
        buffer.writeBoolean(generateAfterCreate);
        buffer.writeBoolean(fullRegenerate);
        writeAcceptedRemaps(buffer, acceptedRemaps);
        MKWorkspacePacketPayloads.warnIfLarge("create_workspace", buffer.writerIndex() - startIndex);
    }

    public static void handle(CreateWorkspacePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) {
            return;
        }
        MKStructureWorkspace workspace = MKStructureWorkspace.fromTag(packet.workspaceTag);
        MKStructureWorkspaceService service = new MKStructureWorkspaceService();
        List<String> errors = service.validateWorkspace(workspace);
        if (!errors.isEmpty()) {
            MKWorkspaceValidationMessages.displayValidationErrors(player, errors);
            return;
        }
        Optional<MKStructureWorkspace> existingOpt = IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(workspace.anchor());
        if (existingOpt.isPresent() && !packet.fullRegenerate) {
            var preflight = service.preflightWorkspaceUpdate(existingOpt.get(), workspace, System.currentTimeMillis(),
                    packet.acceptedRemaps);
            new MKWorkspacePreflightLogger().logConfirmEffects("apply", player, workspace, preflight,
                    packet.acceptedRemaps);
            List<MKWorkspaceGeneratedLayer> lockedLayers =
                    service.lockedInvalidatedLayers(existingOpt.get(), preflight.report());
            if (!lockedLayers.isEmpty()) {
                MKWorkspaceValidationMessages.displayFailure(player,
                        "Workspace update blocked by locked layer: " +
                                lockedLayers.getFirst().getSerializedName());
                return;
            }
        }
        if (packet.fullRegenerate) {
            service.fullRegenerateWorkspace(player.serverLevel(), workspace)
                    .ifPresentOrElse(created -> service.openWorkspaceScreen(player, created.anchor()),
                            () -> MKWorkspaceValidationMessages.displayFailure(player,
                                    "Workspace regeneration failed."));
            return;
        }
        service.createOrUpdateWorkspace(player.serverLevel(), workspace, packet.acceptedRemaps)
                .ifPresentOrElse(created -> {
                    if (packet.generateAfterCreate &&
                            service.generateWorkspace(player.serverLevel(), created.anchor()).isEmpty()) {
                        MKWorkspaceValidationMessages.displayFailure(player, "Workspace generation failed.");
                        return;
                    }
                    service.openWorkspaceScreen(player, created.anchor());
                }, () -> MKWorkspaceValidationMessages.displayFailure(player, "Workspace creation failed."));
    }

    private static void writeAcceptedRemaps(FriendlyByteBuf buffer,
                                            List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        CompoundTag tag = new CompoundTag();
        ListTag remapsTag = new ListTag();
        for (MKWorkspaceTemplateRemapSuggestion remap : acceptedRemaps) {
            remapsTag.add(MKWorkspaceCodecs.encodeNbt(MKWorkspaceTemplateRemapSuggestion.CODEC, remap,
                    "workspace template remap suggestion"));
        }
        tag.put("acceptedRemaps", remapsTag);
        buffer.writeNbt(tag);
    }

    private static List<MKWorkspaceTemplateRemapSuggestion> readAcceptedRemaps(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            return List.of();
        }
        ListTag remapsTag = tag.getList("acceptedRemaps", Tag.TAG_COMPOUND);
        java.util.ArrayList<MKWorkspaceTemplateRemapSuggestion> remaps = new java.util.ArrayList<>();
        for (int i = 0; i < remapsTag.size(); i++) {
            remaps.add(MKWorkspaceCodecs.parseNbt(MKWorkspaceTemplateRemapSuggestion.CODEC,
                    remapsTag.getCompound(i), "workspace template remap suggestion"));
        }
        return List.copyOf(remaps);
    }
}
