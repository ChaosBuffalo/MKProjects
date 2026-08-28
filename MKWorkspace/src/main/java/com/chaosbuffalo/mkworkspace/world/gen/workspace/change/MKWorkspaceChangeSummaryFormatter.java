package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Shared formatter used by the confirmation UI, clipboard, and server logs. */
public final class MKWorkspaceChangeSummaryFormatter {
    private MKWorkspaceChangeSummaryFormatter() {
    }

    public static List<String> lines(MKWorkspaceChangeSummary summary) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Summary: " + summary.summary());
        lines.add("Safety: " + summary.safety().getSerializedName());
        lines.add("Backup: " + (summary.backupRequired() ?
                "required immediately before apply" : "not required (no existing workspace is changed)"));
        if (!summary.invalidatedLayers().isEmpty()) {
            lines.add("Invalidates: " + summary.invalidatedLayers().stream()
                    .map(layer -> layer.getSerializedName())
                    .reduce((left, right) -> left + ", " + right).orElse(""));
        }
        for (String blocker : summary.blockers()) {
            lines.add("Blocked: " + blocker);
        }
        for (String warning : summary.warnings()) {
            lines.add("Warning: " + warning);
        }
        if (!summary.fieldChanges().isEmpty()) {
            lines.add("Settings (" + summary.fieldChanges().size() + ")");
            for (MKWorkspaceFieldChange field : summary.fieldChanges()) {
                lines.add("- " + field.field() + ": " + field.beforeValue() + " -> " + field.afterValue());
            }
        }

        Map<MKWorkspaceChangeEffect.Action, List<MKWorkspaceChangeEffect>> byAction =
                new EnumMap<>(MKWorkspaceChangeEffect.Action.class);
        for (MKWorkspaceChangeEffect effect : summary.effects()) {
            byAction.computeIfAbsent(effect.action(), ignored -> new ArrayList<>()).add(effect);
        }
        lines.add("Effects: " + summary.effects().size() + " total, " + summary.materialEffectCount() + " physical");
        for (MKWorkspaceChangeEffect.Action action : MKWorkspaceChangeEffect.Action.values()) {
            List<MKWorkspaceChangeEffect> effects = byAction.getOrDefault(action, List.of());
            if (effects.isEmpty()) {
                continue;
            }
            lines.add(title(action) + " (" + effects.size() + ")");
            for (MKWorkspaceChangeEffect effect : effects) {
                lines.add("- " + format(effect));
            }
        }
        return List.copyOf(lines);
    }

    public static String clipboard(MKWorkspaceChangeSummary summary) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(summary.title());
        lines.add("Operation: " + summary.operationId());
        lines.add("");
        lines.addAll(lines(summary));
        return String.join("\n", lines);
    }

    private static String format(MKWorkspaceChangeEffect effect) {
        StringBuilder label = new StringBuilder(effect.displayName());
        if (!effect.baseName().isBlank() && !effect.baseName().equals(effect.displayName())) {
            label.append(" / ").append(effect.baseName());
        }
        if (effect.subject() == MKWorkspaceChangeEffect.Subject.VARIANT || effect.variantIndex() > 0) {
            label.append(" (variant ").append(effect.variantIndex()).append(')');
        } else if (effect.subject() == MKWorkspaceChangeEffect.Subject.TEMPLATE) {
            label.append(" (template)");
        }
        label.append(" [").append(effect.subject().name().toLowerCase(java.util.Locale.ROOT));
        if (effect.physical()) {
            label.append(", physical");
        }
        if (effect.derived()) {
            label.append(", derived");
        }
        label.append(']');
        if (!effect.detail().isBlank()) {
            label.append(" - ").append(effect.detail());
        }
        return label.toString();
    }

    private static String title(MKWorkspaceChangeEffect.Action action) {
        String lower = action.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
