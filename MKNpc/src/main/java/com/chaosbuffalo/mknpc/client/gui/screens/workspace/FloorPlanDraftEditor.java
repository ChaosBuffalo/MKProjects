package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Optional;

public final class FloorPlanDraftEditor {
    private final WorkspaceDraftSession session;
    private final String stackId;
    private final String floorRole;

    FloorPlanDraftEditor(WorkspaceDraftSession session, String stackId, String floorRole) {
        this.session = session;
        this.stackId = stackId;
        this.floorRole = floorRole;
    }

    public String stackId() {
        return stackId;
    }

    public String floorRole() {
        return floorRole;
    }

    public MKWorkspaceMaterialPalette inheritedPalette() {
        return session.resolveTowerStackPalette(stackId);
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return settings().paletteOverride();
    }

    public void paletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        replace(settings().withPaletteOverride(value));
    }

    public int minMainPathPieces() {
        return settings().minMainPathPieces();
    }

    public void minMainPathPieces(int value) {
        replace(settings().withMinMainPathPieces(value));
    }

    public int maxMainPathPieces() {
        return settings().maxMainPathPieces();
    }

    public void maxMainPathPieces(int value) {
        replace(settings().withMaxMainPathPieces(value));
    }

    public int maxBranchPiecesBeforeCap() {
        return settings().maxBranchPiecesBeforeCap();
    }

    public void maxBranchPiecesBeforeCap(int value) {
        replace(settings().withMaxBranchPiecesBeforeCap(value));
    }

    public MKWorkspaceHallwayLeadInMode hallwayLeadInMode() {
        return settings().hallwayLeadInMode();
    }

    public void hallwayLeadInMode(MKWorkspaceHallwayLeadInMode value) {
        replace(settings().withHallwayLeadInMode(value));
    }

    public int manualHallwayLeadInPieces() {
        return settings().manualHallwayLeadInPieces();
    }

    public void manualHallwayLeadInPieces(int value) {
        replace(settings().withManualHallwayLeadInPieces(value));
    }

    public boolean mainHallwaysEnabled() {
        return settings().mainHallwaysEnabled();
    }

    public void mainHallwaysEnabled(boolean value) {
        replace(settings().withMainHallwaysEnabled(value));
    }

    public boolean branchHallwaysEnabled() {
        return settings().branchHallwaysEnabled();
    }

    public void branchHallwaysEnabled(boolean value) {
        replace(settings().withBranchHallwaysEnabled(value));
    }

    public boolean mainCapApproachEnabled() {
        return settings().mainCapApproachEnabled();
    }

    public void mainCapApproachEnabled(boolean value) {
        replace(settings().withMainCapApproachEnabled(value));
    }

    public float sprawl() {
        return settings().sprawl();
    }

    public void sprawl(float value) {
        replace(settings().withSprawl(value));
    }

    public boolean linksEnabled() {
        return settings().linksEnabled();
    }

    public void linksEnabled(boolean value) {
        replace(settings().withLinksEnabled(value));
    }

    public float linkDensity() {
        return settings().linkDensity();
    }

    public void linkDensity(float value) {
        replace(settings().withLinkDensity(value));
    }

    public int maxLinksPerFloor() {
        return settings().maxLinksPerFloor();
    }

    public void maxLinksPerFloor(int value) {
        replace(settings().withMaxLinksPerFloor(value));
    }

    public int maxLinksPerRoom() {
        return settings().maxLinksPerRoom();
    }

    public void maxLinksPerRoom(int value) {
        replace(settings().withMaxLinksPerRoom(value));
    }

    public int maxLinkLength() {
        return settings().maxLinkLength();
    }

    public void maxLinkLength(int value) {
        replace(settings().withMaxLinkLength(value));
    }

    public long previewSeed() {
        return lockedLayoutSeed()
                .orElseGet(() -> session.floorTopologyPreviewSeeds.computeIfAbsent(
                        MKWorkspaceFloorTopologySettings.key(stackId, floorRole),
                        key -> (long) key.hashCode()));
    }

    public void rerollPreviewSeed() {
        if (lockedLayoutSeed().isPresent()) {
            return;
        }
        String key = MKWorkspaceFloorTopologySettings.key(stackId, floorRole);
        long current = previewSeed();
        session.floorTopologyPreviewSeeds.put(key, current * 6364136223846793005L + 1442695040888963407L);
    }

    public Optional<Long> lockedLayoutSeed() {
        return settings().lockedLayoutSeed();
    }

    public void lockLayoutSeed() {
        long seed = session.floorTopologyPreviewSeeds.computeIfAbsent(
                MKWorkspaceFloorTopologySettings.key(stackId, floorRole), key -> (long) key.hashCode());
        replace(settings().withLockedLayoutSeed(Optional.of(seed)));
    }

    public void unlockLayoutSeed() {
        replace(settings().withLockedLayoutSeed(Optional.empty()));
    }

    public int roomWidth(MKWorkspaceFloorRoomKind kind) {
        return session.floorTopologyRoomWidth(stackId, floorRole, kind);
    }

    public void roomWidth(MKWorkspaceFloorRoomKind kind, int value) {
        session.floorTopologyRoomWidth(stackId, floorRole, kind, value);
    }

    public void roomWidth(MKWorkspaceFloorRoomKind kind, int index, int value) {
        session.floorTopologyRoomWidth(stackId, floorRole, kind, index, value);
    }

    public int roomLength(MKWorkspaceFloorRoomKind kind) {
        return session.floorTopologyRoomLength(stackId, floorRole, kind);
    }

    public void roomLength(MKWorkspaceFloorRoomKind kind, int value) {
        session.floorTopologyRoomLength(stackId, floorRole, kind, value);
    }

    public void roomLength(MKWorkspaceFloorRoomKind kind, int index, int value) {
        session.floorTopologyRoomLength(stackId, floorRole, kind, index, value);
    }

    public int roomHeight(MKWorkspaceFloorRoomKind kind) {
        return session.floorTopologyRoomHeight(stackId, floorRole, kind);
    }

    public void roomHeight(MKWorkspaceFloorRoomKind kind, int value) {
        session.floorTopologyRoomHeight(stackId, floorRole, kind, value);
    }

    public void roomHeight(MKWorkspaceFloorRoomKind kind, int index, int value) {
        session.floorTopologyRoomHeight(stackId, floorRole, kind, index, value);
    }

    public int roomHeightMax() {
        return session.floorTopologyRoomHeightMax(stackId, floorRole);
    }

    public List<MKWorkspaceFloorRoomProfile> roomProfiles(MKWorkspaceFloorRoomKind kind) {
        return session.floorTopologyRoomProfiles(stackId, floorRole, kind);
    }

    public void addRoomProfile(MKWorkspaceFloorRoomKind kind) {
        session.floorTopologyAddRoomProfile(stackId, floorRole, kind);
    }

    public void removeRoomProfile(MKWorkspaceFloorRoomKind kind, int index) {
        session.floorTopologyRemoveRoomProfile(stackId, floorRole, kind, index);
    }

    public void setRoomMainExitDirection(MKWorkspaceFloorRoomKind kind, int index, Direction direction) {
        session.floorTopologySetRoomMainExitDirection(stackId, floorRole, kind, index, direction);
    }

    public void toggleRoomBranchExit(MKWorkspaceFloorRoomKind kind, int index, Direction direction) {
        session.floorTopologyToggleRoomBranchExit(stackId, floorRole, kind, index, direction);
    }

    public void toggleRoomLinkCandidateExit(MKWorkspaceFloorRoomKind kind, int index, Direction direction) {
        session.floorTopologyToggleRoomLinkCandidateExit(stackId, floorRole, kind, index, direction);
    }

    public void setRoomRandomizeMainExit(MKWorkspaceFloorRoomKind kind, int index, boolean value) {
        session.floorTopologySetRoomRandomizeMainExit(stackId, floorRole, kind, index, value);
    }

    private MKWorkspaceFloorTopologySettings settings() {
        return session.floorTopologySettings(stackId, floorRole);
    }

    private void replace(MKWorkspaceFloorTopologySettings settings) {
        session.replaceFloorTopologySettings(settings);
    }
}
