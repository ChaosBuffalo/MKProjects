package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceDraftViewState {
    final Map<String, Long> floorTopologyPreviewSeeds = new HashMap<>();
    final Map<String, String> verticalStackPreviewSelections = new HashMap<>();
    final Map<String, Boolean> floorRootVariantDrawers = new HashMap<>();
    final Map<String, Boolean> floorHallVariantDrawers = new HashMap<>();
    final Map<String, Boolean> floorInsertVariantDrawers = new HashMap<>();
    final Map<String, Boolean> floorRoomVariantDrawers = new HashMap<>();
    private final Map<String, String> plannerSelections = new HashMap<>();

    void copyFrom(WorkspaceDraftViewState source) {
        floorTopologyPreviewSeeds.clear();
        floorTopologyPreviewSeeds.putAll(source.floorTopologyPreviewSeeds);
        verticalStackPreviewSelections.clear();
        verticalStackPreviewSelections.putAll(source.verticalStackPreviewSelections);
        floorRootVariantDrawers.clear();
        floorRootVariantDrawers.putAll(source.floorRootVariantDrawers);
        floorHallVariantDrawers.clear();
        floorHallVariantDrawers.putAll(source.floorHallVariantDrawers);
        floorInsertVariantDrawers.clear();
        floorInsertVariantDrawers.putAll(source.floorInsertVariantDrawers);
        floorRoomVariantDrawers.clear();
        floorRoomVariantDrawers.putAll(source.floorRoomVariantDrawers);
        plannerSelections.clear();
        plannerSelections.putAll(source.plannerSelections);
    }

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
