package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

public final class FloorPlanDraftEditor {
    private final WorkspaceDraftSession session;
    private final String stackId;
    private final String floorRole;

    FloorPlanDraftEditor(WorkspaceDraftSession session, String stackId, String floorRole) {
        this.session = session;
        this.stackId = stackId;
        this.floorRole = floorRole;
    }

    public WorkspaceDraftSession session() {
        return session;
    }

    public String stackId() {
        return stackId;
    }

    public String floorRole() {
        return floorRole;
    }
}
