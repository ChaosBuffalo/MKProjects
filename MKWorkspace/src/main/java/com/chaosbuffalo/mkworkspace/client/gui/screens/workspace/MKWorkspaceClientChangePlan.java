package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeEffect;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummary;
import net.minecraft.core.BlockPos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MKWorkspaceClientChangePlan(UUID requestId, UUID planId, BlockPos anchor, Instant expiresAt,
                                          MKWorkspaceChangeSummary summary, int totalEffects,
                                          int receivedEffects, boolean complete) {
    public MKWorkspaceClientChangePlan withEffects(List<MKWorkspaceChangeEffect> effects, boolean effectsComplete) {
        MKWorkspaceChangeSummary updated = new MKWorkspaceChangeSummary(summary.operationId(), summary.title(),
                summary.summary(), summary.safety(), summary.backupRequired(), summary.invalidatedLayers(),
                summary.warnings(), summary.blockers(), summary.fieldChanges(), effects);
        return new MKWorkspaceClientChangePlan(requestId, planId, anchor, expiresAt, updated, totalEffects,
                effects.size(), effectsComplete);
    }
}
