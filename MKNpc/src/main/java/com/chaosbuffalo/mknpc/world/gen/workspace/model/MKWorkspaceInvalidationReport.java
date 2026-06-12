package com.chaosbuffalo.mknpc.world.gen.workspace.model;

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
        List<String> warnings
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
                    Codec.STRING.listOf().fieldOf("warnings").forGetter(MKWorkspaceInvalidationReport::warnings)
            ).apply(instance, MKWorkspaceInvalidationReport::new)
    );

    public MKWorkspaceInvalidationReport {
        invalidatedLayers = List.copyOf(invalidatedLayers);
        affectedPlannerIds = List.copyOf(affectedPlannerIds);
        preservedTemplateBindings = List.copyOf(preservedTemplateBindings);
        orphanedTemplateBindings = List.copyOf(orphanedTemplateBindings);
        warnings = List.copyOf(warnings);
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
                List.of()
        );
    }

    public boolean hasInvalidatedLayer(MKWorkspaceGeneratedLayer layer) {
        return invalidatedLayers.contains(layer);
    }

    public boolean hasOrphanedBindings() {
        return !orphanedTemplateBindings.isEmpty();
    }
}
