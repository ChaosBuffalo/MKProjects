package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
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
        return session.floorTopologyInheritedPalette(stackId, floorRole);
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return session.floorTopologyPaletteOverrideOpt(stackId, floorRole);
    }

    public void paletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        session.floorTopologyPaletteOverride(stackId, floorRole, value);
    }

    public int minMainPathPieces() {
        return session.floorTopologyMinMainPathPieces(stackId, floorRole);
    }

    public void minMainPathPieces(int value) {
        session.floorTopologyMinMainPathPieces(stackId, floorRole, value);
    }

    public int maxMainPathPieces() {
        return session.floorTopologyMaxMainPathPieces(stackId, floorRole);
    }

    public void maxMainPathPieces(int value) {
        session.floorTopologyMaxMainPathPieces(stackId, floorRole, value);
    }

    public int maxBranchPiecesBeforeCap() {
        return session.floorTopologyMaxBranchPiecesBeforeCap(stackId, floorRole);
    }

    public void maxBranchPiecesBeforeCap(int value) {
        session.floorTopologyMaxBranchPiecesBeforeCap(stackId, floorRole, value);
    }

    public MKWorkspaceHallwayLeadInMode hallwayLeadInMode() {
        return session.floorTopologyHallwayLeadInMode(stackId, floorRole);
    }

    public void hallwayLeadInMode(MKWorkspaceHallwayLeadInMode value) {
        session.floorTopologyHallwayLeadInMode(stackId, floorRole, value);
    }

    public int manualHallwayLeadInPieces() {
        return session.floorTopologyManualHallwayLeadInPieces(stackId, floorRole);
    }

    public void manualHallwayLeadInPieces(int value) {
        session.floorTopologyManualHallwayLeadInPieces(stackId, floorRole, value);
    }

    public boolean mainHallwaysEnabled() {
        return session.floorTopologyMainHallwaysEnabled(stackId, floorRole);
    }

    public void mainHallwaysEnabled(boolean value) {
        session.floorTopologyMainHallwaysEnabled(stackId, floorRole, value);
    }

    public boolean branchHallwaysEnabled() {
        return session.floorTopologyBranchHallwaysEnabled(stackId, floorRole);
    }

    public void branchHallwaysEnabled(boolean value) {
        session.floorTopologyBranchHallwaysEnabled(stackId, floorRole, value);
    }

    public boolean mainCapApproachEnabled() {
        return session.floorTopologyMainCapApproachEnabled(stackId, floorRole);
    }

    public void mainCapApproachEnabled(boolean value) {
        session.floorTopologyMainCapApproachEnabled(stackId, floorRole, value);
    }

    public float sprawl() {
        return session.floorTopologySprawl(stackId, floorRole);
    }

    public void sprawl(float value) {
        session.floorTopologySprawl(stackId, floorRole, value);
    }

    public boolean linksEnabled() {
        return session.floorTopologyLinksEnabled(stackId, floorRole);
    }

    public void linksEnabled(boolean value) {
        session.floorTopologyLinksEnabled(stackId, floorRole, value);
    }

    public float linkDensity() {
        return session.floorTopologyLinkDensity(stackId, floorRole);
    }

    public void linkDensity(float value) {
        session.floorTopologyLinkDensity(stackId, floorRole, value);
    }

    public int maxLinksPerFloor() {
        return session.floorTopologyMaxLinksPerFloor(stackId, floorRole);
    }

    public void maxLinksPerFloor(int value) {
        session.floorTopologyMaxLinksPerFloor(stackId, floorRole, value);
    }

    public int maxLinksPerRoom() {
        return session.floorTopologyMaxLinksPerRoom(stackId, floorRole);
    }

    public void maxLinksPerRoom(int value) {
        session.floorTopologyMaxLinksPerRoom(stackId, floorRole, value);
    }

    public int maxLinkLength() {
        return session.floorTopologyMaxLinkLength(stackId, floorRole);
    }

    public void maxLinkLength(int value) {
        session.floorTopologyMaxLinkLength(stackId, floorRole, value);
    }

    public long previewSeed() {
        return session.floorTopologyPreviewSeed(stackId, floorRole);
    }

    public void rerollPreviewSeed() {
        session.rerollFloorTopologyPreviewSeed(stackId, floorRole);
    }

    public Optional<Long> lockedLayoutSeed() {
        return session.floorTopologyLockedLayoutSeed(stackId, floorRole);
    }

    public void lockLayoutSeed() {
        session.lockFloorTopologyLayoutSeed(stackId, floorRole);
    }

    public void unlockLayoutSeed() {
        session.unlockFloorTopologyLayoutSeed(stackId, floorRole);
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
}
