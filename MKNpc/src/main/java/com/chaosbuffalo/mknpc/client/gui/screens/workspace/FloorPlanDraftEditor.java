package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTowerStackSettings;
import net.minecraft.core.Direction;

import java.util.ArrayList;
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
        return session.towerStackSettings(stackId).paletteOverrideOpt()
                .map(override -> override.resolve(session.draftBasePalette()))
                .orElse(session.draftBasePalette());
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
        return roomProfile(kind).width();
    }

    public void roomWidth(MKWorkspaceFloorRoomKind kind, int value) {
        roomWidth(kind, 0, value);
    }

    public void roomWidth(MKWorkspaceFloorRoomKind kind, int index, int value) {
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index).withWidth(value);
        replace(settings().withRoomProfile(kind, index, profile));
    }

    public int roomLength(MKWorkspaceFloorRoomKind kind) {
        return roomProfile(kind).length();
    }

    public void roomLength(MKWorkspaceFloorRoomKind kind, int value) {
        roomLength(kind, 0, value);
    }

    public void roomLength(MKWorkspaceFloorRoomKind kind, int index, int value) {
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index).withLength(value);
        replace(settings().withRoomProfile(kind, index, profile));
    }

    public int roomHeight(MKWorkspaceFloorRoomKind kind) {
        return roomProfile(kind).height();
    }

    public void roomHeight(MKWorkspaceFloorRoomKind kind, int value) {
        roomHeight(kind, 0, value);
    }

    public void roomHeight(MKWorkspaceFloorRoomKind kind, int index, int value) {
        int clamped = clamp(value, MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, roomHeightMax());
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index).withHeight(clamped);
        replace(settings().withRoomProfile(kind, index, profile));
    }

    public int roomHeightMax() {
        MKWorkspaceTowerStackSettings stackSettings = session.towerStackSettings(stackId);
        return switch (floorRole) {
            case "main_floor" -> stackSettings.mainHeight();
            case "basement_floor" -> stackSettings.basementHeight();
            case "basement_entry" -> stackSettings.basementEntryHeight();
            case "basement_cap", "basement_cap_approach" -> stackSettings.basementCapHeight();
            case "top_cap", "top_cap_approach" -> stackSettings.mainCapHeight();
            default -> stackSettings.entryHeight();
        };
    }

    public List<MKWorkspaceFloorRoomProfile> roomProfiles(MKWorkspaceFloorRoomKind kind) {
        List<MKWorkspaceFloorRoomProfile> profiles = roomProfilesForKind(settings(), kind);
        if (!profiles.isEmpty()) {
            return profiles;
        }
        return List.of(defaultRoomProfile(kind));
    }

    public void addRoomProfile(MKWorkspaceFloorRoomKind kind) {
        List<MKWorkspaceFloorRoomProfile> profiles = roomProfiles(kind);
        int nextIndex = profiles.size();
        MKWorkspaceFloorRoomProfile source = profiles.getLast();
        MKWorkspaceFloorRoomProfile added = MKWorkspaceFloorRoomProfile.defaults(
                kind,
                source.width(),
                source.length(),
                source.height()
        ).withIdentity(
                kind.getSerializedName() + "_" + nextIndex,
                WorkspaceTopologyUiSupport.formatTopologyLabel(kind.getSerializedName()) + " " + (nextIndex + 1));
        replace(settings().withAddedRoomProfile(kind, added));
    }

    public void removeRoomProfile(MKWorkspaceFloorRoomKind kind, int index) {
        replace(settings().withRemovedRoomProfile(kind, index));
    }

    public void setRoomMainExitDirection(MKWorkspaceFloorRoomKind kind, int index, Direction direction) {
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index);
        if (!profile.mainExitDirection(direction)) {
            return;
        }
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new ArrayList<>();
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : profile.horizontalExits()) {
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT ||
                    exit.direction() == direction) {
                continue;
            }
            exits.add(exit);
        }
        exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                MKWorkspaceFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        ));
        replace(settings().withRoomProfile(kind, index, profile.withHorizontalExits(exits)));
    }

    public void toggleRoomBranchExit(MKWorkspaceFloorRoomKind kind, int index, Direction direction) {
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index);
        if (!profile.optionalBranchExitDirection(direction)) {
            return;
        }
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new ArrayList<>(profile.horizontalExits());
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> existing = exits.stream()
                .filter(exit -> exit.direction() == direction)
                .findFirst();
        if (existing.isPresent()) {
            exits.remove(existing.get());
        } else {
            exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                    direction,
                    MKWorkspaceHorizontalExitPathKind.BRANCH,
                    MKWorkspaceFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID,
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
        }
        replace(settings().withRoomProfile(kind, index, profile.withHorizontalExits(exits)));
    }

    public void toggleRoomLinkCandidateExit(MKWorkspaceFloorRoomKind kind, int index, Direction direction) {
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index);
        if (!profile.linkCandidateExitDirection(direction)) {
            return;
        }
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new ArrayList<>(profile.horizontalExits());
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> existing = exits.stream()
                .filter(exit -> exit.direction() == direction &&
                        exit.pathKind() == MKWorkspaceHorizontalExitPathKind.LINK_CANDIDATE)
                .findFirst();
        if (existing.isPresent()) {
            exits.remove(existing.get());
        } else {
            exits.removeIf(exit -> exit.direction() == direction);
            exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                    direction,
                    MKWorkspaceHorizontalExitPathKind.LINK_CANDIDATE,
                    MKWorkspaceFloorRoomProfile.INHERITED_LINK_OPENING_PROFILE_ID,
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
        }
        replace(settings().withRoomProfile(kind, index, profile.withHorizontalExits(exits)));
    }

    public void setRoomRandomizeMainExit(MKWorkspaceFloorRoomKind kind, int index, boolean value) {
        MKWorkspaceFloorRoomProfile profile = roomProfile(kind, index);
        replace(settings().withRoomProfile(kind, index, profile.withRandomizeMainExit(value)));
    }

    private MKWorkspaceFloorTopologySettings settings() {
        return session.floorTopologySettings(stackId, floorRole);
    }

    private void replace(MKWorkspaceFloorTopologySettings settings) {
        session.replaceFloorTopologySettings(settings);
    }

    private MKWorkspaceFloorRoomProfile roomProfile(MKWorkspaceFloorRoomKind kind) {
        return roomProfile(kind, 0);
    }

    private MKWorkspaceFloorRoomProfile roomProfile(MKWorkspaceFloorRoomKind kind, int index) {
        List<MKWorkspaceFloorRoomProfile> profiles = roomProfilesForKind(settings(), kind);
        if (index >= 0 && index < profiles.size()) {
            return profiles.get(index);
        }
        return defaultRoomProfile(kind);
    }

    private MKWorkspaceFloorRoomProfile defaultRoomProfile(MKWorkspaceFloorRoomKind kind) {
        MKWorkspaceTowerStackSettings stackSettings = session.towerStackSettings(stackId);
        return MKWorkspaceFloorRoomProfile.defaults(kind, stackSettings.width(), stackSettings.length(),
                roomHeightMax());
    }

    private List<MKWorkspaceFloorRoomProfile> roomProfilesForKind(MKWorkspaceFloorTopologySettings settings,
                                                                  MKWorkspaceFloorRoomKind kind) {
        return switch (kind) {
            case MAIN_ROOM -> settings.mainRoomProfiles();
            case BRANCH_ROOM -> settings.branchRoomProfiles();
            case BRANCH_CAP -> settings.branchCapProfiles();
            case MAIN_CAP_APPROACH -> settings.mainCapApproachProfiles();
            case MAIN_CAP -> settings.mainCapProfiles();
        };
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
