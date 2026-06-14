package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

public final class LinearRunDraftEditor {
    private final WorkspaceDraftSession session;
    private final String topologySlotId;

    LinearRunDraftEditor(WorkspaceDraftSession session, String topologySlotId) {
        this.session = session;
        this.topologySlotId = topologySlotId;
    }

    public WorkspaceDraftSession session() {
        return session;
    }

    public String topologySlotId() {
        return topologySlotId;
    }
}
