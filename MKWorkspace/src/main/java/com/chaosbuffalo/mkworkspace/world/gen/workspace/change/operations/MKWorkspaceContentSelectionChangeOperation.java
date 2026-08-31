package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.*;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspaceContentSelectionMutationService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation.MKWorkspacePieceRelayoutService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** The single preflight operation for author-owned slot/family/variant selection metadata. */
public final class MKWorkspaceContentSelectionChangeOperation
        implements MKWorkspaceChangeOperation<MKWorkspaceContentSelectionChangePayload> {
    public static final ResourceLocation ID = MKWorkspace.id("content_selection");

    @Override public ResourceLocation id() { return ID; }
    @Override public Codec<MKWorkspaceContentSelectionChangePayload> codec() {
        return MKWorkspaceContentSelectionChangePayload.CODEC;
    }

    @Override
    public MKWorkspacePreparedChange prepare(ServerPlayer player, BlockPos anchor, MKWorkspaceChangeRequest request,
                                             MKWorkspaceContentSelectionChangePayload payload) {
        MKStructureWorkspace workspace = IMKStructureWorkspaceData.get(player.serverLevel())
                .getWorkspaceByAnchor(anchor).orElseThrow(() ->
                        new IllegalArgumentException("No workspace exists at this anchor."));
        MKWorkspacePieceDefinition selected = workspace.pieces().stream()
                .filter(piece -> piece.pieceId().equals(payload.pieceId())).findFirst().orElse(null);
        MKWorkspaceContentSelectionMutationService service = new MKWorkspaceContentSelectionMutationService();
        List<String> validationErrors = service.validate(workspace, payload);
        ArrayList<String> blockers = new ArrayList<>(validationErrors);
        List<MKWorkspaceGeneratedLayer> invalidated =
                MKWorkspaceContentSelectionMutationService.invalidatedLayers(payload.kind());
        List<MKWorkspaceGeneratedLayer> locked = workspace.layerStates().stream()
                .filter(state -> state.locked() && invalidated.contains(state.layer()))
                .map(MKWorkspaceGeneratedLayerState::layer).toList();
        if (!locked.isEmpty()) blockers.add("Unlock invalidated layer(s): " + locked.stream()
                .map(MKWorkspaceGeneratedLayer::getSerializedName).reduce((a, b) -> a + ", " + b).orElse(""));

        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        ArrayList<MKWorkspaceFieldChange> fields = new ArrayList<>();
        ArrayList<String> warnings = new ArrayList<>();
        if (selected != null) describe(workspace, selected, payload, effects, fields, warnings);
        if (selected != null && validationErrors.isEmpty() && requiresCatalogRelayout(payload.kind())) {
            MKStructureWorkspace projected = service.project(workspace, payload);
            var catalogRelayout = new MKWorkspacePieceRelayoutService()
                    .summarizeWorkspaceCatalogRelayout(workspace, projected);
            if (catalogRelayout.isEmpty()) {
                blockers.add("The physical catalog layout could not be prepared safely.");
            } else {
                var relayout = catalogRelayout.get();
                warnings.addAll(relayout.warnings());
                if (relayout.newCount() > 0 || relayout.removedCount() > 0 ||
                        relayout.rebuildRequiredCount() > 0) {
                    blockers.add("Family row changes must preserve every physical template, but this layout would " +
                            "create, remove, or rebuild catalog entries.");
                }
                relayout.impacts().stream()
                        .filter(impact -> !"preserved".equals(impact.outcome()))
                        .filter(impact -> !selected.pieceName().equals(impact.pieceName()))
                        .map(this::relayoutEffect)
                        .forEach(effects::add);
            }
        }
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(ID, title(payload.kind()),
                summary(selected, payload), MKWorkspaceMutationSafety.SAFE_RELAYOUT, true, invalidated,
                warnings, blockers, fields, effects);
        return new MKWorkspacePreparedChange(request, anchor, workspace.id(),
                MKWorkspaceChangeCoordinator.fingerprint(workspace), payload.toString(), summary, applyPlayer -> {
            MKStructureWorkspace current = IMKStructureWorkspaceData.get(applyPlayer.serverLevel())
                    .getWorkspaceByAnchor(anchor).orElseThrow();
            MKStructureWorkspace updated = service.apply(current, payload);
            if (requiresCatalogRelayout(payload.kind())) {
                new MKWorkspacePieceRelayoutService().relayoutWorkspaceCatalog(
                        applyPlayer.serverLevel(), current, updated).orElseThrow(() ->
                        new IllegalStateException("The confirmed physical catalog relayout produced no plan."));
            } else {
                IMKStructureWorkspaceData.get(applyPlayer.serverLevel()).updateWorkspace(updated);
            }
            return MKWorkspaceChangeApplyResult.success(anchor, appliedMessage(payload.kind()));
        });
    }

    private void describe(MKStructureWorkspace workspace, MKWorkspacePieceDefinition selected,
                          MKWorkspaceContentSelectionChangePayload payload,
                          List<MKWorkspaceChangeEffect> effects, List<MKWorkspaceFieldChange> fields,
                          List<String> warnings) {
        String oldFamily = MKWorkspaceContentSelectionTags.familyId(selected);
        String slot = MKWorkspaceContentSelectionTags.topologySlotId(selected);
        effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.UPDATE,
                MKWorkspaceChangeEffect.Subject.SLOT, slot, slot, "", 0, false, false,
                "Recompile this topology slot's resolved candidate pool"));
        switch (payload.kind()) {
            case PROMOTE_VARIANT -> {
                fields.add(new MKWorkspaceFieldChange("family", oldFamily, payload.targetFamilyId()));
                fields.add(new MKWorkspaceFieldChange("template purpose",
                        MKWorkspaceContentSelectionTags.purpose(selected).serializedName(),
                        MKWorkspaceTemplatePurpose.FAMILY_CANONICAL.serializedName()));
                effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.CREATE,
                        MKWorkspaceChangeEffect.Subject.FAMILY, payload.targetFamilyId(), payload.targetFamilyId(),
                        selected.pieceName(), 0, false, false,
                        "Bind family to topology slot " + slot + " with weight " + payload.weight()));
                effects.add(pieceEffect(selected, MKWorkspaceChangeEffect.Action.MOVE,
                        "Preserve authored blocks and UUID; move from variant row of " + oldFamily +
                                " to the canonical row for " + payload.targetFamilyId()));
                warnings.add("The promoted template becomes canonical fallback for the new family until it has " +
                        "an enabled variant.");
                warnings.add("If the source family has no other enabled variants, it will return to its own " +
                        "canonical fallback.");
            }
            case MOVE_VARIANT -> {
                fields.add(new MKWorkspaceFieldChange("family", oldFamily, payload.targetFamilyId()));
                effects.add(pieceEffect(selected, MKWorkspaceChangeEffect.Action.MOVE,
                        "Preserve authored blocks and move variant ownership from " + oldFamily + " to " +
                                payload.targetFamilyId()));
            }
            case SET_FAMILY_WEIGHT, SET_FAMILY_ENABLED -> {
                String oldValue = payload.kind() == MKWorkspaceContentSelectionChangePayload.Kind.SET_FAMILY_WEIGHT ?
                        Integer.toString(MKWorkspaceContentSelectionTags.familyWeight(selected.tags())) :
                        Boolean.toString(MKWorkspaceContentSelectionTags.familyEnabled(selected.tags()));
                String newValue = payload.kind() == MKWorkspaceContentSelectionChangePayload.Kind.SET_FAMILY_WEIGHT ?
                        Integer.toString(payload.weight()) : Boolean.toString(payload.enabled());
                fields.add(new MKWorkspaceFieldChange(payload.kind() ==
                        MKWorkspaceContentSelectionChangePayload.Kind.SET_FAMILY_WEIGHT ? "family weight" :
                        "family enabled", oldValue, newValue));
                effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.UPDATE,
                        MKWorkspaceChangeEffect.Subject.FAMILY, oldFamily, oldFamily, selected.pieceName(), 0,
                        false, false, oldValue + " -> " + newValue));
                workspace.pieces().stream().filter(piece -> oldFamily.equals(
                                MKWorkspaceContentSelectionTags.familyId(piece)) && slot.equals(
                                MKWorkspaceContentSelectionTags.topologySlotId(piece)))
                        .forEach(piece -> effects.add(pieceEffect(piece, MKWorkspaceChangeEffect.Action.UPDATE,
                                "Persist family selection metadata " + oldValue + " -> " + newValue)));
            }
            case SET_VARIANT_WEIGHT, SET_VARIANT_ENABLED -> {
                String oldValue = payload.kind() == MKWorkspaceContentSelectionChangePayload.Kind.SET_VARIANT_WEIGHT ?
                        Integer.toString(MKWorkspaceContentSelectionTags.variantWeight(selected.tags())) :
                        Boolean.toString(MKWorkspaceContentSelectionTags.variantEnabled(selected.tags()));
                String newValue = payload.kind() == MKWorkspaceContentSelectionChangePayload.Kind.SET_VARIANT_WEIGHT ?
                        Integer.toString(payload.weight()) : Boolean.toString(payload.enabled());
                fields.add(new MKWorkspaceFieldChange(payload.kind() ==
                        MKWorkspaceContentSelectionChangePayload.Kind.SET_VARIANT_WEIGHT ? "variant weight" :
                        "variant enabled", oldValue, newValue));
                effects.add(pieceEffect(selected, MKWorkspaceChangeEffect.Action.UPDATE,
                        "Variant selection metadata " + oldValue + " -> " + newValue));
            }
            case SET_TEMPLATE_PURPOSE -> {
                fields.add(new MKWorkspaceFieldChange("template purpose",
                        MKWorkspaceContentSelectionTags.purpose(selected).serializedName(),
                        payload.purpose().serializedName()));
                effects.add(pieceEffect(selected, MKWorkspaceChangeEffect.Action.UPDATE,
                        "Template purpose changes to " + payload.purpose().serializedName()));
                if (!payload.purpose().placeable()) warnings.add("This template will be excluded from preview and " +
                        "runtime candidate pools.");
            }
        }
    }

    private MKWorkspaceChangeEffect pieceEffect(MKWorkspacePieceDefinition piece,
                                                 MKWorkspaceChangeEffect.Action action, String detail) {
        MKWorkspaceTemplatePurpose purpose = MKWorkspaceContentSelectionTags.purpose(piece);
        return new MKWorkspaceChangeEffect(action, purpose.variant() ? MKWorkspaceChangeEffect.Subject.VARIANT :
                MKWorkspaceChangeEffect.Subject.TEMPLATE, piece.pieceId().toString(), piece.pieceName(),
                piece.tags().getOrDefault("workspace_base_name", piece.pieceName()), piece.variantIndex(), true,
                !purpose.placeable(), detail);
    }

    private MKWorkspaceChangeEffect relayoutEffect(MKWorkspaceRelayoutImpact impact) {
        MKWorkspaceChangeEffect.Action action = switch (impact.outcome()) {
            case "new" -> MKWorkspaceChangeEffect.Action.CREATE;
            case "rebuild" -> MKWorkspaceChangeEffect.Action.REBUILD;
            case "removed" -> MKWorkspaceChangeEffect.Action.REMOVE;
            case "expanded" -> MKWorkspaceChangeEffect.Action.EXPAND;
            default -> MKWorkspaceChangeEffect.Action.MOVE;
        };
        return new MKWorkspaceChangeEffect(action,
                impact.variantIndex() > 0 ? MKWorkspaceChangeEffect.Subject.VARIANT :
                        MKWorkspaceChangeEffect.Subject.TEMPLATE,
                impact.stableSlotKey(), impact.pieceName(), impact.baseName(), impact.variantIndex(), true,
                false, impact.reason());
    }

    private boolean requiresCatalogRelayout(MKWorkspaceContentSelectionChangePayload.Kind kind) {
        return kind == MKWorkspaceContentSelectionChangePayload.Kind.PROMOTE_VARIANT ||
                kind == MKWorkspaceContentSelectionChangePayload.Kind.MOVE_VARIANT;
    }

    private String summary(MKWorkspacePieceDefinition piece, MKWorkspaceContentSelectionChangePayload payload) {
        String name = piece == null ? payload.pieceId().toString() : piece.pieceName();
        return switch (payload.kind()) {
            case PROMOTE_VARIANT -> "Promote " + name + " to family " + payload.targetFamilyId() + ".";
            case MOVE_VARIANT -> "Move " + name + " to family " + payload.targetFamilyId() + ".";
            case SET_FAMILY_WEIGHT -> "Set the family weight containing " + name + " to " + payload.weight() + ".";
            case SET_FAMILY_ENABLED -> (payload.enabled() ? "Enable" : "Disable") + " the family containing " + name + ".";
            case SET_VARIANT_WEIGHT -> "Set " + name + " variant weight to " + payload.weight() + ".";
            case SET_VARIANT_ENABLED -> (payload.enabled() ? "Enable " : "Disable ") + name + ".";
            case SET_TEMPLATE_PURPOSE -> "Change " + name + " purpose to " + payload.purpose().serializedName() + ".";
        };
    }

    private String title(MKWorkspaceContentSelectionChangePayload.Kind kind) {
        return switch (kind) {
            case PROMOTE_VARIANT -> "Confirm Variant Promotion";
            case MOVE_VARIANT -> "Confirm Variant Family Move";
            case SET_FAMILY_WEIGHT, SET_FAMILY_ENABLED -> "Confirm Family Selection Change";
            case SET_VARIANT_WEIGHT, SET_VARIANT_ENABLED -> "Confirm Variant Selection Change";
            case SET_TEMPLATE_PURPOSE -> "Confirm Template Purpose Change";
        };
    }

    private String appliedMessage(MKWorkspaceContentSelectionChangePayload.Kind kind) {
        return switch (kind) {
            case PROMOTE_VARIANT -> "Variant promoted to a new family.";
            case MOVE_VARIANT -> "Variant moved between families.";
            case SET_FAMILY_WEIGHT, SET_FAMILY_ENABLED -> "Family selection settings updated.";
            case SET_VARIANT_WEIGHT, SET_VARIANT_ENABLED -> "Variant selection settings updated.";
            case SET_TEMPLATE_PURPOSE -> "Template purpose updated.";
        };
    }
}
