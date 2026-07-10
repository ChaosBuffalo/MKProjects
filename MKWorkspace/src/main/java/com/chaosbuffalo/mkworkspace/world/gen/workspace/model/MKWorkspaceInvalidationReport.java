package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MKWorkspaceInvalidationReport(
        List<MKWorkspaceGeneratedLayer> invalidatedLayers,
        List<MKWorkspacePlannerId> affectedPlannerIds,
        List<MKWorkspacePlannerId> preservedTemplateBindings,
        List<MKWorkspacePlannerId> orphanedTemplateBindings,
        MKWorkspaceMutationSafety safety,
        String summary,
        String recommendedOperation,
        List<String> warnings,
        List<MKWorkspaceTemplateRemapSuggestion> remapSuggestions,
        List<MKWorkspaceRelayoutImpact> relayoutImpacts
) {
    public static final Codec<MKWorkspaceInvalidationReport> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MKWorkspaceGeneratedLayer.CODEC.listOf().fieldOf("invalidatedLayers")
                            .forGetter(MKWorkspaceInvalidationReport::invalidatedLayers),
                    MKWorkspacePlannerId.CODEC.listOf().fieldOf("affectedPlannerIds")
                            .forGetter(MKWorkspaceInvalidationReport::affectedPlannerIds),
                    MKWorkspacePlannerId.CODEC.listOf().fieldOf("preservedTemplateBindings")
                            .forGetter(MKWorkspaceInvalidationReport::preservedTemplateBindings),
                    MKWorkspacePlannerId.CODEC.listOf().fieldOf("orphanedTemplateBindings")
                            .forGetter(MKWorkspaceInvalidationReport::orphanedTemplateBindings),
                    MKWorkspaceMutationSafety.CODEC.fieldOf("safety")
                            .forGetter(MKWorkspaceInvalidationReport::safety),
                    Codec.STRING.fieldOf("summary").forGetter(MKWorkspaceInvalidationReport::summary),
                    Codec.STRING.fieldOf("recommendedOperation")
                            .forGetter(MKWorkspaceInvalidationReport::recommendedOperation),
                    Codec.STRING.listOf().fieldOf("warnings").forGetter(MKWorkspaceInvalidationReport::warnings),
                    MKWorkspaceTemplateRemapSuggestion.CODEC.listOf().optionalFieldOf("remapSuggestions", List.of())
                            .forGetter(MKWorkspaceInvalidationReport::remapSuggestions),
                    MKWorkspaceRelayoutImpact.CODEC.listOf().optionalFieldOf("relayoutImpacts", List.of())
                            .forGetter(MKWorkspaceInvalidationReport::relayoutImpacts)
            ).apply(instance, MKWorkspaceInvalidationReport::new)
    );

    public MKWorkspaceInvalidationReport(List<MKWorkspaceGeneratedLayer> invalidatedLayers,
                                         List<MKWorkspacePlannerId> affectedPlannerIds,
                                         List<MKWorkspacePlannerId> preservedTemplateBindings,
                                         List<MKWorkspacePlannerId> orphanedTemplateBindings,
                                         MKWorkspaceMutationSafety safety,
                                         String summary,
                                         String recommendedOperation,
                                         List<String> warnings) {
        this(invalidatedLayers, affectedPlannerIds, preservedTemplateBindings, orphanedTemplateBindings, safety,
                summary, recommendedOperation, warnings, List.of(), List.of());
    }

    public MKWorkspaceInvalidationReport(List<MKWorkspaceGeneratedLayer> invalidatedLayers,
                                         List<MKWorkspacePlannerId> affectedPlannerIds,
                                         List<MKWorkspacePlannerId> preservedTemplateBindings,
                                         List<MKWorkspacePlannerId> orphanedTemplateBindings,
                                         MKWorkspaceMutationSafety safety,
                                         String summary,
                                         String recommendedOperation,
                                         List<String> warnings,
                                         List<MKWorkspaceTemplateRemapSuggestion> remapSuggestions) {
        this(invalidatedLayers, affectedPlannerIds, preservedTemplateBindings, orphanedTemplateBindings, safety,
                summary, recommendedOperation, warnings, remapSuggestions, List.of());
    }

    public MKWorkspaceInvalidationReport {
        invalidatedLayers = List.copyOf(invalidatedLayers);
        affectedPlannerIds = List.copyOf(affectedPlannerIds);
        preservedTemplateBindings = List.copyOf(preservedTemplateBindings);
        orphanedTemplateBindings = List.copyOf(orphanedTemplateBindings);
        warnings = List.copyOf(warnings);
        remapSuggestions = List.copyOf(remapSuggestions);
        relayoutImpacts = List.copyOf(relayoutImpacts);
    }

    public static MKWorkspaceInvalidationReport noChanges(String summary) {
        return new MKWorkspaceInvalidationReport(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.SAFE_METADATA_UPDATE,
                summary,
                "none",
                List.of(),
                List.of(),
                List.of()
        );
    }

    public boolean hasInvalidatedLayer(MKWorkspaceGeneratedLayer layer) {
        return invalidatedLayers.contains(layer);
    }

    public boolean hasOrphanedBindings() {
        return !orphanedTemplateBindings.isEmpty();
    }

    public MKWorkspaceInvalidationReport withTemplateBindings(List<MKWorkspacePlannerId> preservedBindings,
                                                              List<MKWorkspacePlannerId> orphanedBindings) {
        return new MKWorkspaceInvalidationReport(
                invalidatedLayers,
                affectedPlannerIds,
                preservedBindings,
                orphanedBindings,
                safety,
                summary,
                recommendedOperation,
                warnings,
                remapSuggestions,
                relayoutImpacts
        );
    }

    public MKWorkspaceInvalidationReport withRemapSuggestions(
            List<MKWorkspaceTemplateRemapSuggestion> newRemapSuggestions) {
        return new MKWorkspaceInvalidationReport(
                invalidatedLayers,
                affectedPlannerIds,
                preservedTemplateBindings,
                orphanedTemplateBindings,
                safety,
                summary,
                recommendedOperation,
                warnings,
                newRemapSuggestions,
                relayoutImpacts
        );
    }

    public MKWorkspaceInvalidationReport withRelayoutImpacts(List<MKWorkspaceRelayoutImpact> newRelayoutImpacts) {
        return new MKWorkspaceInvalidationReport(
                invalidatedLayers,
                affectedPlannerIds,
                preservedTemplateBindings,
                orphanedTemplateBindings,
                safety,
                summary,
                recommendedOperation,
                warnings,
                remapSuggestions,
                newRelayoutImpacts
        );
    }
}
