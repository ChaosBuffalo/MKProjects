package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

public final class TowerStackDraftEditor {
    private final WorkspaceDraftSession session;
    private final String stackId;

    TowerStackDraftEditor(WorkspaceDraftSession session, String stackId) {
        this.session = session;
        this.stackId = stackId;
    }

    public WorkspaceDraftSession session() {
        return session;
    }

    public String stackId() {
        return stackId;
    }
}
