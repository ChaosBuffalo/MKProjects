package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMutationSafety;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceChangeSummaryFormatterTest {
    @Test
    void formatterIncludesEveryEffectWithoutTruncation() {
        ArrayList<MKWorkspaceChangeEffect> effects = new ArrayList<>();
        for (int index = 0; index < 25; index++) {
            effects.add(new MKWorkspaceChangeEffect(MKWorkspaceChangeEffect.Action.REBUILD,
                    MKWorkspaceChangeEffect.Subject.TEMPLATE, "piece-" + index, "piece-" + index,
                    "base-" + index, 0, true, false, "reason-" + index));
        }
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(MKWorkspace.id("test"), "Test",
                "Test all effects", MKWorkspaceMutationSafety.SAFE_RELAYOUT, true, List.of(), List.of(),
                List.of(), List.of(), effects);

        List<String> lines = MKWorkspaceChangeSummaryFormatter.lines(summary);

        assertEquals(25, lines.stream().filter(line -> line.startsWith("- piece-")).count());
        assertTrue(lines.stream().anyMatch(line -> line.contains("piece-24")));
        assertTrue(lines.stream().noneMatch(line -> line.contains("more")));
    }

    @Test
    void formatterIncludesBlockersFieldsAndBackupPolicy() {
        MKWorkspaceChangeSummary summary = new MKWorkspaceChangeSummary(MKWorkspace.id("test"), "Test",
                "Blocked change", MKWorkspaceMutationSafety.DESTRUCTIVE_REGENERATE, true, List.of(),
                List.of("warning"), List.of("locked"),
                List.of(new MKWorkspaceFieldChange("previewMargin", "2", "4")), List.of());

        String text = MKWorkspaceChangeSummaryFormatter.clipboard(summary);

        assertTrue(text.contains("Backup: required"));
        assertTrue(text.contains("Blocked: locked"));
        assertTrue(text.contains("previewMargin: 2 -> 4"));
        assertTrue(text.contains("Warning: warning"));
    }
}
