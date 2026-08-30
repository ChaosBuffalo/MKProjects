package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeApplyResult;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeCoordinator;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequest;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspacePreparedChange;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertAuthoringService;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class MKWorkspaceInsertSocketChangeOperation
        implements MKWorkspaceChangeOperation<MKWorkspaceInsertSocketChangePayload> {
    public static final ResourceLocation ID = MKWorkspace.id("insert_socket");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<MKWorkspaceInsertSocketChangePayload> codec() {
        return MKWorkspaceInsertSocketChangePayload.CODEC;
    }

    @Override
    public MKWorkspacePreparedChange prepare(ServerPlayer player, BlockPos anchor,
                                             MKWorkspaceChangeRequest request,
                                             MKWorkspaceInsertSocketChangePayload payload) {
        MKStructureWorkspace workspace = IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(anchor)
                .orElseThrow(() -> new IllegalArgumentException("No workspace exists at this anchor."));
        MKWorkspaceInsertAuthoringService service = new MKWorkspaceInsertAuthoringService();
        MKWorkspaceInsertAuthoringService.CreateSocketFamilyRequest createRequest =
                new MKWorkspaceInsertAuthoringService.CreateSocketFamilyRequest(
                        anchor, payload.hostPieceId(), payload.socketWorldPos(), payload.socketFacing(),
                        payload.slotId(), payload.width(), payload.height(), payload.depth(),
                        payload.faceUOffset(), payload.faceVOffset(), payload.hostFinalState(),
                        payload.templateJigsawFinalState());
        MKWorkspaceInsertAuthoringService.PlaceExistingSocketRequest placeRequest =
                new MKWorkspaceInsertAuthoringService.PlaceExistingSocketRequest(
                        anchor, payload.hostPieceId(), payload.socketWorldPos(), payload.socketFacing(),
                        payload.slotId(), payload.hostFinalState());
        ArrayList<String> blockers = new ArrayList<>(payload.createSlot() ?
                service.validateCreateSocketFamily(player.serverLevel(), createRequest) :
                service.validatePlaceExistingSocketFamily(player.serverLevel(), placeRequest));
        MKWorkspacePieceDefinition host = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(payload.hostPieceId())).findFirst().orElse(null);
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        if (host != null) {
            effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.UPDATE,
                    MKWorkspaceChangeEffect.Subject.SOCKET, host.pieceId().toString(), host.pieceName(),
                    host.tags().getOrDefault("workspace_base_name", host.pieceName()), host.variantIndex(),
                    true, false, "Place host jigsaw at " + payload.socketWorldPos().toShortString() +
                            " facing " + payload.socketFacing().getSerializedName()));
        }
        if (payload.createSlot()) {
            effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.CREATE,
                    MKWorkspaceChangeEffect.Subject.SLOT, payload.slotId(), payload.slotId(),
                    "", 0, false, false,
                    "Declare user-owned insert slot " + payload.width() + "x" + payload.height() + "x" + payload.depth()));
            effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.CREATE,
                    MKWorkspaceChangeEffect.Subject.TEMPLATE, payload.slotId() + "/scaffold",
                    payload.slotId() + " slot scaffold", payload.slotId(), 0, true, false,
                    "Create the non-placeable bounds scaffold and its host jigsaw"));
        }
        List<MKWorkspaceGeneratedLayer> invalidated = payload.createSlot() ?
                List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                        MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT, MKWorkspaceGeneratedLayer.RUNTIME_METADATA) :
                List.of(MKWorkspaceGeneratedLayer.SIDECAR_BLOCKS);
        List<MKWorkspaceGeneratedLayer> locked = workspace.layerStates().stream()
                .filter(state -> state.locked() && invalidated.contains(state.layer()))
                .map(state -> state.layer()).toList();
        if (!locked.isEmpty()) {
            blockers.add("Unlock invalidated layer(s): " + locked.stream()
                    .map(MKWorkspaceGeneratedLayer::getSerializedName)
                    .reduce((left, right) -> left + ", " + right).orElse(""));
        }
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(ID,
                "Confirm Insert Socket Change",
                payload.createSlot() ? "Create insert slot " + payload.slotId() + " and place its socket." :
                        "Place a socket targeting existing insert slot " + payload.slotId() + ".",
                MKWorkspaceMutationSafety.SAFE_RELAYOUT, true, invalidated,
                List.of(), blockers, List.of(), effects);
        return new MKWorkspacePreparedChange(request, anchor, workspace.id(),
                MKWorkspaceChangeCoordinator.fingerprint(workspace),
                payload.socketWorldPos().asLong() + ":" + player.serverLevel().getBlockState(payload.socketWorldPos()),
                summary, applyPlayer -> {
            MKWorkspaceInsertAuthoringService.Result result = payload.createSlot() ? service.createSocketFamily(
                    applyPlayer.serverLevel(), createRequest) : service.placeExistingSocketFamily(
                    applyPlayer.serverLevel(), placeRequest);
            return result.context().isPresent() ? MKWorkspaceChangeApplyResult.success(anchor,
                    payload.createSlot() ? "Insert slot and scaffold created." : "Insert socket placed.") :
                    MKWorkspaceChangeApplyResult.failure(anchor, String.join("; ", result.errors()));
        });
    }
}
