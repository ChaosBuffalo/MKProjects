package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.MKStructureWorkspaceService;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceVariantAddition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class MKWorkspaceDefinitionSummaryBuilder {
    private MKWorkspaceDefinitionSummaryBuilder() {
    }

    public static MKWorkspaceChangeSummary build(MKStructureWorkspace existing, MKStructureWorkspace requested,
                                                  MKWorkspaceMutationPreflight preflight,
                                                  MKStructureWorkspaceService.PreparedUpdateStrategy strategy,
                                                  List<MKWorkspaceVariantAddition> additions,
                                                  List<UUID> deletions,
                                                  List<String> blockers) {
        ArrayList<MKWorkspaceFieldChange> fields = new ArrayList<>(
                MKWorkspaceDefinitionFieldDiff.differences(existing, requested));
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        for (MKWorkspaceRelayoutImpact impact : preflight.report().relayoutImpacts()) {
            effects.add(fromRelayoutImpact(impact));
        }
        for (MKWorkspaceVariantAddition addition : additions) {
            effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.CREATE,
                    MKWorkspaceChangeEffect.Subject.VARIANT, addition.basePieceName(), addition.basePieceName(),
                    addition.basePieceName(), 1, true, false,
                    addition.sourcePieceName() == null ? "Create a new authored variant" :
                            "Clone authored blocks from " + addition.sourcePieceName()));
        }
        for (UUID deletedId : deletions) {
            MKWorkspacePieceDefinition piece = existing.pieces().stream()
                    .filter(candidate -> candidate.pieceId().equals(deletedId)).findFirst().orElse(null);
            effects.add(piece == null ? new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.REMOVE,
                    MKWorkspaceChangeEffect.Subject.VARIANT, deletedId.toString(), deletedId.toString(), "", 1,
                    true, false, "Remove authored variant") : pieceEffect(
                    MKWorkspaceChangeEffect.Action.REMOVE, piece, "Remove authored variant"));
        }
        preflight.report().remapSuggestions().forEach(suggestion -> effects.add(new MKWorkspaceChangeEffect(
                MKWorkspaceChangeEffect.Action.MOVE, MKWorkspaceChangeEffect.Subject.TEMPLATE,
                suggestion.orphanedPlannerId().toString(), suggestion.targetPlannerId().toString(),
                suggestion.orphanedPlannerId().toString(), 0, false, false,
                "Template binding remap score " + suggestion.score() + ": " + suggestion.reason())));
        if (strategy == MKStructureWorkspaceService.PreparedUpdateStrategy.FULL_REGENERATE) {
            for (MKWorkspacePieceDefinition piece : existing.pieces()) {
                effects.add(pieceEffect(MKWorkspaceChangeEffect.Action.REBUILD, piece,
                        "Unclaimed definition drift requires full regeneration"));
            }
        } else if (effects.isEmpty() && changesPieces(strategy)) {
            MKWorkspaceChangeEffect.Action action = strategyAction(strategy);
            existing.pieces().forEach(piece -> effects.add(pieceEffect(action, piece,
                    physicalStrategyDetail(strategy))));
        }
        for (MKWorkspaceFieldChange field : fields) {
            effects.add(MKWorkspaceChangeEffect.setting(field.field(),
                    field.beforeValue() + " -> " + field.afterValue()));
        }
        String summary = preflight.report().summary() + " Prepared strategy: " +
                strategy.name().toLowerCase(java.util.Locale.ROOT) + ".";
        MKWorkspaceMutationSafety safety = strategy == MKStructureWorkspaceService.PreparedUpdateStrategy.FULL_REGENERATE ?
                MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE : preflight.report().safety();
        ArrayList<String> warnings = new ArrayList<>(preflight.report().warnings());
        if (strategy == MKStructureWorkspaceService.PreparedUpdateStrategy.FULL_REGENERATE) {
            warnings.add("No registered planner mutation claimed every changed definition field; safe default is full regeneration.");
        }
        return new MKWorkspaceChangeSummary(MKWorkspace.id("update_definition"),
                "Confirm Workspace Definition Change", summary, safety, true,
                invalidatedLayers(preflight, strategy), warnings, blockers, fields, effects);
    }

    public static List<MKWorkspaceGeneratedLayer> invalidatedLayers(
            MKWorkspaceMutationPreflight preflight,
            MKStructureWorkspaceService.PreparedUpdateStrategy strategy) {
        if (strategy == MKStructureWorkspaceService.PreparedUpdateStrategy.FULL_REGENERATE) {
            return List.copyOf(Arrays.asList(MKWorkspaceGeneratedLayer.values()));
        }
        return preflight.report().invalidatedLayers();
    }

    public static MKWorkspaceChangeSummary create(MKStructureWorkspace requested) {
        List<MKWorkspaceChangeEffect> effects = List.of(MKWorkspaceChangeEffect.workspace(
                MKWorkspaceChangeEffect.Action.CREATE, requested.id(),
                requested.namespace() + ":" + requested.structureName(), "Create workspace definition"));
        return new MKWorkspaceChangeSummary(MKWorkspace.id("update_definition"), "Confirm Workspace Creation",
                "Create the workspace definition at " + requested.anchor().toShortString() + ".",
                MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE, false, List.of(), List.of(), List.of(),
                List.of(), effects);
    }

    private static MKWorkspaceChangeEffect fromRelayoutImpact(MKWorkspaceRelayoutImpact impact) {
        MKWorkspaceChangeEffect.Action action = switch (impact.outcome()) {
            case "new" -> MKWorkspaceChangeEffect.Action.CREATE;
            case "rebuild" -> MKWorkspaceChangeEffect.Action.REBUILD;
            case "removed" -> MKWorkspaceChangeEffect.Action.REMOVE;
            case "moved" -> MKWorkspaceChangeEffect.Action.MOVE;
            case "expanded" -> MKWorkspaceChangeEffect.Action.EXPAND;
            case "scaffold_patch" -> MKWorkspaceChangeEffect.Action.PATCH;
            default -> MKWorkspaceChangeEffect.Action.PRESERVE;
        };
        MKWorkspaceChangeEffect.Subject subject = impact.variantIndex() > 0 ?
                MKWorkspaceChangeEffect.Subject.VARIANT : MKWorkspaceChangeEffect.Subject.TEMPLATE;
        return new MKWorkspaceChangeEffect(action, subject, impact.stableSlotKey(), impact.pieceName(),
                impact.baseName(), impact.variantIndex(), action != MKWorkspaceChangeEffect.Action.PRESERVE,
                false, impact.reason());
    }

    private static MKWorkspaceChangeEffect pieceEffect(MKWorkspaceChangeEffect.Action action,
                                                        MKWorkspacePieceDefinition piece, String detail) {
        boolean derived = MKWorkspaceTemplateReuseTags.isDerived(piece.tags());
        MKWorkspaceChangeEffect.Subject subject = piece.variantIndex() > 0 ?
                MKWorkspaceChangeEffect.Subject.VARIANT : MKWorkspaceChangeEffect.Subject.TEMPLATE;
        String baseName = piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
        return new MKWorkspaceChangeEffect(action, subject, piece.pieceId().toString(), piece.pieceName(),
                baseName, piece.variantIndex(), true, derived, detail);
    }

    private static boolean changesPieces(MKStructureWorkspaceService.PreparedUpdateStrategy strategy) {
        return switch (strategy) {
            case PREVIEW_MARGIN_RELAYOUT, PALETTE_SWAP, IDENTITY_RENAME, MARGIN_EXPANSION,
                    HALLWAY_ROUTING_REGENERATION, LINK_RENDERING_REFRESH, RAMPART_ACCESS_PATCH,
                    CATALOG_PRESERVING_RELAYOUT -> true;
            default -> false;
        };
    }

    private static MKWorkspaceChangeEffect.Action strategyAction(
            MKStructureWorkspaceService.PreparedUpdateStrategy strategy) {
        return switch (strategy) {
            case PREVIEW_MARGIN_RELAYOUT -> MKWorkspaceChangeEffect.Action.MOVE;
            case MARGIN_EXPANSION -> MKWorkspaceChangeEffect.Action.EXPAND;
            case HALLWAY_ROUTING_REGENERATION, CATALOG_PRESERVING_RELAYOUT ->
                    MKWorkspaceChangeEffect.Action.REBUILD;
            case RAMPART_ACCESS_PATCH -> MKWorkspaceChangeEffect.Action.PATCH;
            default -> MKWorkspaceChangeEffect.Action.UPDATE;
        };
    }

    private static String physicalStrategyDetail(MKStructureWorkspaceService.PreparedUpdateStrategy strategy) {
        return switch (strategy) {
            case PREVIEW_MARGIN_RELAYOUT -> "Relayout authored pieces for the preview margin";
            case PALETTE_SWAP -> "Replace authored palette blocks throughout the workspace";
            case IDENTITY_RENAME -> "Update every authored template for the new workspace identity";
            case MARGIN_EXPANSION -> "Expand authored scaffold and exterior clearing margins";
            case HALLWAY_ROUTING_REGENERATION -> "Regenerate authored hallway routing";
            case LINK_RENDERING_REFRESH -> "Refresh authored connector and link metadata";
            case RAMPART_ACCESS_PATCH -> "Patch authored rampart access openings";
            case CATALOG_PRESERVING_RELAYOUT -> "Relayout affected authored catalog pieces";
            default -> strategy.name();
        };
    }
}
