package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;

public class WorkspacePlannerLayout {
    private final MKStackLayoutVertical previewContent;
    private final MKStackLayoutVertical settingsContent;
    private final MKScrollView previewScrollView;
    private final MKScrollView settingsScrollView;

    public WorkspacePlannerLayout(MKStackLayoutVertical previewContent, MKStackLayoutVertical settingsContent,
                                  MKScrollView previewScrollView, MKScrollView settingsScrollView) {
        this.previewContent = previewContent;
        this.settingsContent = settingsContent;
        this.previewScrollView = previewScrollView;
        this.settingsScrollView = settingsScrollView;
    }

    public MKStackLayoutVertical previewContent() {
        return previewContent;
    }

    public MKStackLayoutVertical settingsContent() {
        return settingsContent;
    }

    public int previewWidth() {
        return previewContent.getWidth();
    }

    public int settingsWidth() {
        return settingsContent.getWidth();
    }

    public void finish(MKWorkspaceScreen screen, String stateId) {
        previewContent.manualRecompute();
        settingsContent.manualRecompute();
        previewScrollView.addWidget(previewContent);
        settingsScrollView.addWidget(settingsContent);
        screen.finalizeScrollView(previewScrollView, stateId);
        screen.finalizeScrollView(settingsScrollView, stateId);
    }
}
