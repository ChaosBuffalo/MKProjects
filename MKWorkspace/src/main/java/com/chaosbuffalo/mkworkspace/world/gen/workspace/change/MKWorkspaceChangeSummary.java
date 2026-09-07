package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/** The complete, operation-neutral report displayed before a persistent workspace mutation. */
public record MKWorkspaceChangeSummary(
        ResourceLocation operationId,
        String title,
        String summary,
        MKWorkspaceMutationSafety safety,
        boolean backupRequired,
        List<MKWorkspaceGeneratedLayer> invalidatedLayers,
        List<String> warnings,
        List<String> blockers,
        List<MKWorkspaceFieldChange> fieldChanges,
        List<MKWorkspaceChangeEffect> effects
) {
    public MKWorkspaceChangeSummary {
        Objects.requireNonNull(operationId, "operationId");
        title = Objects.requireNonNullElse(title, "Confirm Workspace Change");
        summary = Objects.requireNonNullElse(summary, "");
        Objects.requireNonNull(safety, "safety");
        invalidatedLayers = List.copyOf(invalidatedLayers);
        warnings = List.copyOf(warnings);
        blockers = List.copyOf(blockers);
        fieldChanges = List.copyOf(fieldChanges);
        effects = List.copyOf(effects);
    }

    public boolean canConfirm() {
        return blockers.isEmpty();
    }

    public int materialEffectCount() {
        return (int) effects.stream().filter(MKWorkspaceChangeEffect::physical).count();
    }
}
