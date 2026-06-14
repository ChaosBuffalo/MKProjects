package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWalledKeepCourtyardSettings;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

public final class WalledKeepDraftEditor {
    private final WorkspaceDraftSession session;

    WalledKeepDraftEditor(WorkspaceDraftSession session) {
        this.session = session;
    }

    public TerrainAdjustment terrainAdjustment() {
        return session.draft().topologyProfile.terrainAdjustment();
    }

    public void terrainAdjustment(TerrainAdjustment value) {
        session.draft().topologyProfile = session.draft().topologyProfile.withTerrainAdjustment(value);
    }

    public MKWalledKeepCourtyardSettings courtyardSettings() {
        return session.draft().topologyProfile.courtyardSettings();
    }

    public int courtyardContentTemplateSize() {
        return courtyardSettings().courtyardContentTemplateSize();
    }

    public int courtyardPathInnerMargin() {
        return courtyardSettings().courtyardPathInnerMargin();
    }

    public void courtyardPathInnerMargin(int value) {
        MKWalledKeepCourtyardSettings current = courtyardSettings();
        session.draft().topologyProfile = session.draft().topologyProfile.withCourtyardSettings(
                new MKWalledKeepCourtyardSettings(
                        current.courtyardContentEnabled(),
                        current.courtyardSocketGenerationEnabled(),
                        current.courtyardContentTemplateHeight(),
                        current.courtyardSocketClearance(),
                        current.courtyardWalkwayContinuationLength(),
                        value,
                        current.courtyardContentTemplateSize()
                ));
    }

    public void courtyardContentTemplateSize(int value) {
        MKWalledKeepCourtyardSettings current = courtyardSettings();
        session.draft().topologyProfile = session.draft().topologyProfile.withCourtyardSettings(
                new MKWalledKeepCourtyardSettings(
                        current.courtyardContentEnabled(),
                        current.courtyardSocketGenerationEnabled(),
                        current.courtyardContentTemplateHeight(),
                        current.courtyardSocketClearance(),
                        current.courtyardWalkwayContinuationLength(),
                        current.courtyardPathInnerMargin(),
                        value
                ));
    }
}
