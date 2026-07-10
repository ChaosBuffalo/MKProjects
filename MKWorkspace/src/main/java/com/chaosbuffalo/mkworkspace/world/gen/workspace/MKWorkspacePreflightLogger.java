package com.chaosbuffalo.mkworkspace.world.gen.workspace;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class MKWorkspacePreflightLogger {
    public void logConfirmEffects(String phase,
                                  ServerPlayer player,
                                  MKStructureWorkspace requested,
                                  MKWorkspaceMutationPreflight preflight,
                                  List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        for (String line : describeConfirmEffects(phase, player, requested, preflight, acceptedRemaps)) {
            MKWorkspace.LOGGER.info(line);
        }
    }

    List<String> describeConfirmEffects(String phase,
                                        ServerPlayer player,
                                        MKStructureWorkspace requested,
                                        MKWorkspaceMutationPreflight preflight,
                                        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        return describeConfirmEffects(phase, player.getGameProfile().getName(), requested, preflight, acceptedRemaps);
    }

    List<String> describeConfirmEffects(String phase,
                                        String playerName,
                                        MKStructureWorkspace requested,
                                        MKWorkspaceMutationPreflight preflight,
                                        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps) {
        MKWorkspaceInvalidationReport report = preflight.report();
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Workspace update effects phase=" + phase +
                " player=" + playerName +
                " anchor=" + requested.anchor().toShortString() +
                " workspace=" + requested.namespace() + ":" + requested.structureName() +
                " operation=" + report.recommendedOperation() +
                " safety=" + report.safety().getSerializedName());
        lines.add("Workspace update summary: " + report.summary());
        lines.add("Workspace update invalidates: " + generatedLayerNames(report.invalidatedLayers()));
        if (!report.affectedPlannerIds().isEmpty()) {
            lines.add("Workspace update affected planner ids: " + plannerIds(report.affectedPlannerIds()));
        }
        if (!report.preservedTemplateBindings().isEmpty()) {
            lines.add("Workspace update preserved bindings: " + plannerIds(report.preservedTemplateBindings()));
        }
        if (!report.orphanedTemplateBindings().isEmpty()) {
            lines.add("Workspace update orphaned bindings: " + plannerIds(report.orphanedTemplateBindings()));
        }
        for (String warning : report.warnings()) {
            lines.add("Workspace update warning: " + warning);
        }
        appendRemapLines(lines, "Workspace update accepted remap", acceptedRemaps);
        appendRemapLines(lines, "Workspace update suggested remap", report.remapSuggestions());
        appendImpactLines(lines, report.relayoutImpacts());
        return List.copyOf(lines);
    }

    private void appendRemapLines(ArrayList<String> lines,
                                  String prefix,
                                  List<MKWorkspaceTemplateRemapSuggestion> remaps) {
        if (remaps.isEmpty()) {
            return;
        }
        lines.add(prefix + " count=" + remaps.size());
        for (MKWorkspaceTemplateRemapSuggestion remap : remaps) {
            lines.add(prefix + ": orphaned=" + remap.orphanedPlannerId() +
                    " target=" + remap.targetPlannerId() +
                    " score=" + remap.score() +
                    " reason=" + remap.reason());
        }
    }

    private void appendImpactLines(ArrayList<String> lines, List<MKWorkspaceRelayoutImpact> impacts) {
        if (impacts.isEmpty()) {
            lines.add("Workspace update template impact: none reported");
            return;
        }
        long preserved = countImpacts(impacts, "preserved") + countImpacts(impacts, "moved") +
                countImpacts(impacts, "expanded");
        long moved = countImpacts(impacts, "moved");
        long expanded = countImpacts(impacts, "expanded");
        long created = countImpacts(impacts, "new");
        long rebuilt = countImpacts(impacts, "rebuild");
        long removed = countImpacts(impacts, "removed");
        lines.add("Workspace update template impact: total=" + impacts.size() +
                " preserved=" + preserved +
                " moved=" + moved +
                " expanded=" + expanded +
                " new=" + created +
                " rebuilt=" + rebuilt +
                " removed=" + removed);
        for (MKWorkspaceRelayoutImpact impact : impacts) {
            lines.add("Workspace update template impact detail: outcome=" + impact.outcome() +
                    " base=" + impact.baseName() +
                    " piece=" + impact.pieceName() +
                    " variant=" + impact.variantIndex() +
                    " plannerId=" + impact.plannerId() +
                    " slot=" + (impact.stableSlotKey().isBlank() ? impact.plannerId() : impact.stableSlotKey()) +
                    " reason=" + impact.reason());
        }
    }

    private long countImpacts(List<MKWorkspaceRelayoutImpact> impacts, String outcome) {
        return impacts.stream()
                .filter(impact -> outcome.equals(impact.outcome()))
                .count();
    }

    private String generatedLayerNames(List<MKWorkspaceGeneratedLayer> layers) {
        if (layers.isEmpty()) {
            return "none";
        }
        return String.join(", ", layers.stream()
                .map(MKWorkspaceGeneratedLayer::getSerializedName)
                .toList());
    }

    private String plannerIds(List<MKWorkspacePlannerId> plannerIds) {
        return String.join(", ", plannerIds.stream()
                .map(MKWorkspacePlannerId::toString)
                .toList());
    }
}
