package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceDraftViewState {
    final Map<String, Long> floorTopologyPreviewSeeds = new HashMap<>();
    final Map<String, String> verticalStackPreviewSelections = new HashMap<>();
    private final Map<String, String> plannerSelections = new HashMap<>();

    String plannerSelection(String key, String fallback) {
        return plannerSelections.getOrDefault(key, fallback);
    }

    void setPlannerSelection(String key, String value) {
        if (value == null || value.isBlank()) {
            plannerSelections.remove(key);
            return;
        }
        plannerSelections.put(key, value);
    }
}
