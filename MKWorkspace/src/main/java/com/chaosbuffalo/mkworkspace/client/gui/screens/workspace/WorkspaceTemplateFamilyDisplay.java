package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import java.util.List;

record WorkspaceTemplateFamilyDisplay(
        String label,
        String summary,
        List<String> templateBaseNames,
        String linearRunFamilyId
) {
    static WorkspaceTemplateFamilyDisplay forBaseNames(String label, String summary, List<String> templateBaseNames) {
        return new WorkspaceTemplateFamilyDisplay(label, summary, List.copyOf(templateBaseNames), "");
    }

    static WorkspaceTemplateFamilyDisplay forLinearRun(String label, String summary, String linearRunFamilyId) {
        return new WorkspaceTemplateFamilyDisplay(label, summary, List.of(), linearRunFamilyId);
    }

    boolean usesLinearRunFamily() {
        return !linearRunFamilyId.isBlank();
    }
}
