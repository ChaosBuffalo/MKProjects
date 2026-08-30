package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceContentSelectionChangePayload;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplatePurpose;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Applies metadata-only slot/family/variant changes without moving or replacing authored blocks. */
public final class MKWorkspaceContentSelectionMutationService {
    public List<MKWorkspacePieceDefinition> piecesNeedingInsertSlotNormalization(MKStructureWorkspace workspace) {
        return workspace.pieces().stream()
                .filter(piece -> !piece.tags().equals(normalizedInsertSlotTags(piece)))
                .toList();
    }

    public MKStructureWorkspace normalizeInsertSlotIdentities(MKStructureWorkspace workspace) {
        MKWorkspaceBackupManifestWriter.requireTransaction("normalize-insert-slot-identities");
        List<MKWorkspacePieceDefinition> pieces = workspace.pieces().stream().map(piece -> {
            var tags = normalizedInsertSlotTags(piece);
            return tags.equals(piece.tags()) ? piece : piece.withTags(tags);
        }).toList();
        MKStructureWorkspace updated = workspace.withPieces(pieces);
        for (MKWorkspaceGeneratedLayer layer : List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT, MKWorkspaceGeneratedLayer.RUNTIME_METADATA)) {
            var state = updated.layerState(layer).orElse(null);
            if (state != null) updated = updated.withLayerState(state.markDirty());
        }
        return updated;
    }

    private java.util.Map<String, String> normalizedInsertSlotTags(MKWorkspacePieceDefinition piece) {
        var tags = MKWorkspaceContentSelectionTags.normalizeInsertSlotIdentity(piece.tags());
        return MKWorkspaceContentSelectionTags.purpose(piece) == MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD ?
                MKWorkspaceContentSelectionTags.clearFamily(tags) : tags;
    }

    public List<String> validate(MKStructureWorkspace workspace, MKWorkspaceContentSelectionChangePayload change) {
        ArrayList<String> errors = new ArrayList<>();
        MKWorkspacePieceDefinition piece = find(workspace, change);
        if (piece == null) {
            return List.of("The selected authored piece no longer exists.");
        }
        String familyId = MKWorkspaceContentSelectionTags.familyId(piece);
        String slotId = MKWorkspaceContentSelectionTags.topologySlotId(piece);
        MKWorkspaceTemplatePurpose purpose = MKWorkspaceContentSelectionTags.purpose(piece);
        switch (change.kind()) {
            case PROMOTE_VARIANT -> {
                if (purpose != MKWorkspaceTemplatePurpose.FAMILY_VARIANT) {
                    errors.add("Only a family variant can be promoted to a new family canonical.");
                }
                validateNewFamilyId(workspace, change.targetFamilyId(), errors);
            }
            case MOVE_VARIANT -> {
                if (purpose != MKWorkspaceTemplatePurpose.FAMILY_VARIANT) {
                    errors.add("Only a family variant can move between families.");
                }
                if (change.targetFamilyId().isBlank() || change.targetFamilyId().equals(familyId)) {
                    errors.add("Choose a different target family.");
                } else {
                    boolean compatibleTarget = workspace.pieces().stream().anyMatch(candidate ->
                            change.targetFamilyId().equals(MKWorkspaceContentSelectionTags.familyId(candidate)) &&
                                    slotId.equals(MKWorkspaceContentSelectionTags.topologySlotId(candidate)) &&
                                    MKWorkspaceContentSelectionTags.purpose(candidate) ==
                                            MKWorkspaceTemplatePurpose.FAMILY_CANONICAL);
                    if (!compatibleTarget) {
                        errors.add("Target family must have a canonical bound to topology slot " + slotId + ".");
                    }
                }
            }
            case SET_FAMILY_WEIGHT, SET_VARIANT_WEIGHT -> {
                if (change.weight() < 1) errors.add("Selection weights must be positive.");
            }
            case SET_VARIANT_ENABLED -> {
                if (purpose != MKWorkspaceTemplatePurpose.FAMILY_VARIANT) {
                    errors.add("Variant enablement can only be changed on a family variant.");
                }
            }
            case SET_TEMPLATE_PURPOSE -> validatePurposeChange(workspace, piece, change.purpose(), errors);
            case SET_FAMILY_ENABLED -> { }
        }
        return List.copyOf(errors);
    }

    public MKStructureWorkspace apply(MKStructureWorkspace workspace, MKWorkspaceContentSelectionChangePayload change) {
        List<String> errors = validate(workspace, change);
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        MKWorkspaceBackupManifestWriter.requireTransaction("change-workspace-content-selection");
        MKWorkspacePieceDefinition selected = find(workspace, change);
        String sourceFamily = MKWorkspaceContentSelectionTags.familyId(selected);
        String slot = MKWorkspaceContentSelectionTags.topologySlotId(selected);
        ArrayList<MKWorkspacePieceDefinition> pieces = new ArrayList<>(workspace.pieces().size());
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            boolean selectedPiece = piece.pieceId().equals(change.pieceId());
            boolean selectedFamily = sourceFamily.equals(MKWorkspaceContentSelectionTags.familyId(piece)) &&
                    slot.equals(MKWorkspaceContentSelectionTags.topologySlotId(piece));
            LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
            switch (change.kind()) {
                case PROMOTE_VARIANT -> {
                    if (selectedPiece) {
                        tags = new LinkedHashMap<>(MKWorkspaceContentSelectionTags.applyFamily(tags, slot,
                                change.targetFamilyId(), change.weight(), change.enabled()));
                        tags = new LinkedHashMap<>(MKWorkspaceContentSelectionTags.applyTemplate(tags,
                                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, "", 1, true));
                    }
                }
                case MOVE_VARIANT -> {
                    if (selectedPiece) {
                        MKWorkspacePieceDefinition targetCanonical = workspace.pieces().stream().filter(candidate ->
                                change.targetFamilyId().equals(MKWorkspaceContentSelectionTags.familyId(candidate)) &&
                                        MKWorkspaceContentSelectionTags.purpose(candidate) ==
                                                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL).findFirst().orElseThrow();
                        tags = new LinkedHashMap<>(MKWorkspaceContentSelectionTags.applyFamily(tags, slot,
                                change.targetFamilyId(), MKWorkspaceContentSelectionTags.familyWeight(
                                        targetCanonical.tags()), MKWorkspaceContentSelectionTags.familyEnabled(
                                        targetCanonical.tags())));
                    }
                }
                case SET_FAMILY_WEIGHT -> {
                    if (selectedFamily) tags.put(MKWorkspaceContentSelectionTags.FAMILY_WEIGHT,
                            Integer.toString(change.weight()));
                }
                case SET_FAMILY_ENABLED -> {
                    if (selectedFamily) tags.put(MKWorkspaceContentSelectionTags.FAMILY_ENABLED,
                            Boolean.toString(change.enabled()));
                }
                case SET_VARIANT_WEIGHT -> {
                    if (selectedPiece) tags.put(MKWorkspaceContentSelectionTags.VARIANT_WEIGHT,
                            Integer.toString(change.weight()));
                }
                case SET_VARIANT_ENABLED -> {
                    if (selectedPiece) tags.put(MKWorkspaceContentSelectionTags.VARIANT_ENABLED,
                            Boolean.toString(change.enabled()));
                }
                case SET_TEMPLATE_PURPOSE -> {
                    if (selectedPiece) tags = new LinkedHashMap<>(MKWorkspaceContentSelectionTags.applyTemplate(tags,
                            change.purpose(), change.purpose().variant() ?
                                    MKWorkspaceContentSelectionTags.variantId(piece) : "",
                            MKWorkspaceContentSelectionTags.variantWeight(tags),
                            MKWorkspaceContentSelectionTags.variantEnabled(tags)));
                }
            }
            pieces.add(tags.equals(piece.tags()) ? piece : piece.withTags(tags));
        }
        MKStructureWorkspace updated = workspace.withPieces(pieces);
        for (MKWorkspaceGeneratedLayer layer : invalidatedLayers(change.kind())) {
            var state = updated.layerState(layer).orElse(null);
            if (state != null) updated = updated.withLayerState(state.markDirty());
        }
        return updated;
    }

    public static List<MKWorkspaceGeneratedLayer> invalidatedLayers(
            MKWorkspaceContentSelectionChangePayload.Kind kind) {
        return switch (kind) {
            case PROMOTE_VARIANT, MOVE_VARIANT, SET_TEMPLATE_PURPOSE -> List.of(
                    MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS, MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT,
                    MKWorkspaceGeneratedLayer.RUNTIME_METADATA);
            case SET_FAMILY_WEIGHT, SET_FAMILY_ENABLED, SET_VARIANT_WEIGHT, SET_VARIANT_ENABLED -> List.of(
                    MKWorkspaceGeneratedLayer.PREVIEW_LAYOUT, MKWorkspaceGeneratedLayer.RUNTIME_METADATA);
        };
    }

    private MKWorkspacePieceDefinition find(MKStructureWorkspace workspace,
                                             MKWorkspaceContentSelectionChangePayload change) {
        return workspace.pieces().stream().filter(piece -> piece.pieceId().equals(change.pieceId()))
                .findFirst().orElse(null);
    }

    private void validateNewFamilyId(MKStructureWorkspace workspace, String familyId, List<String> errors) {
        if (!familyId.matches("[a-z0-9_.-]+")) {
            errors.add("Family ID must use lowercase letters, numbers, '.', '_' or '-'.");
        } else if (workspace.pieces().stream().anyMatch(piece ->
                familyId.equals(MKWorkspaceContentSelectionTags.familyId(piece)))) {
            errors.add("Family ID already exists: " + familyId + ".");
        }
    }

    private void validatePurposeChange(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece,
                                       MKWorkspaceTemplatePurpose target, List<String> errors) {
        MKWorkspaceTemplatePurpose source = MKWorkspaceContentSelectionTags.purpose(piece);
        if (source == target) errors.add("The template already has purpose " + target.serializedName() + ".");
        if (source == MKWorkspaceTemplatePurpose.FAMILY_CANONICAL && target != source) {
            errors.add("A family canonical cannot change purpose without first assigning another canonical.");
        }
        if (target == MKWorkspaceTemplatePurpose.FAMILY_CANONICAL) {
            String family = MKWorkspaceContentSelectionTags.familyId(piece);
            boolean hasOtherCanonical = workspace.pieces().stream().anyMatch(candidate ->
                    !candidate.pieceId().equals(piece.pieceId()) &&
                            family.equals(MKWorkspaceContentSelectionTags.familyId(candidate)) &&
                            MKWorkspaceContentSelectionTags.purpose(candidate) ==
                                    MKWorkspaceTemplatePurpose.FAMILY_CANONICAL);
            if (hasOtherCanonical) errors.add("That family already has a canonical template.");
        }
    }
}
