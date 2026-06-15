package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import java.util.HashMap;
import java.util.Map;

final class WorkspaceDraftViewState {
    final Map<String, Long> floorTopologyPreviewSeeds = new HashMap<>();
    final Map<String, String> towerStackPreviewSelections = new HashMap<>();
    String walledKeepTowerStackTab = "keep.center";
}
