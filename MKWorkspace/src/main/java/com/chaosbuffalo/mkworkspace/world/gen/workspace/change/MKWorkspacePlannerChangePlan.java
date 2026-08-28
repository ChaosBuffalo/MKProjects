package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/** Exact planner-owned implementation for a definition change the core operation cannot classify. */
public record MKWorkspacePlannerChangePlan(
        ResourceLocation changeId,
        String summary,
        MKWorkspaceMutationSafety safety,
        List<MKWorkspaceGeneratedLayer> invalidatedLayers,
        List<String> warnings,
        List<MKWorkspaceChangeEffect> effects,
        String stateGuard,
        MKWorkspacePreparedMutation mutation
) {
    public MKWorkspacePlannerChangePlan {
        Objects.requireNonNull(changeId, "changeId");
        summary = Objects.requireNonNullElse(summary, "Planner-defined workspace change");
        Objects.requireNonNull(safety, "safety");
        invalidatedLayers = List.copyOf(invalidatedLayers);
        warnings = List.copyOf(warnings);
        effects = List.copyOf(effects);
        stateGuard = Objects.requireNonNullElse(stateGuard, "");
        Objects.requireNonNull(mutation, "mutation");
    }
}
