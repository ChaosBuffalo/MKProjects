package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorLinkGenerationMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
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
        return session.resolvePlannerScopePalette(stackId);
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return session.plannerScopePaletteOverride(topologyGroupId());
    }

    public void paletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        session.plannerScopePaletteOverride(topologyGroupId(), value);
    }

    public String topologyGroupId() {
        return MKFloorTopologySettings.key(stackId, floorRole);
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

    public MKHallwayLeadInMode hallwayLeadInMode() {
        return settings().hallwayLeadInMode();
    }

    public void hallwayLeadInMode(MKHallwayLeadInMode value) {
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

    public MKFloorLinkGenerationMode linkGenerationMode() {
        return settings().linkGenerationMode();
    }

    public void linkGenerationMode(MKFloorLinkGenerationMode value) {
        replace(settings().withLinkGenerationMode(value));
    }

    public float linkDecay() {
        return settings().linkDecay();
    }

    public void linkDecay(float value) {
        replace(settings().withLinkDecay(value));
    }

    public int endpointIntactRadius() {
        return settings().endpointIntactRadius();
    }

    public void endpointIntactRadius(int value) {
        replace(settings().withEndpointIntactRadius(value));
    }

    public float middleDecayBonus() {
        return settings().middleDecayBonus();
    }

    public void middleDecayBonus(float value) {
        replace(settings().withMiddleDecayBonus(value));
    }

    public boolean linkInsertsEnabled() {
        return settings().insertFamily().isPresent();
    }

    public void linkInsertsEnabled(boolean value) {
        if (value) {
            ensureLinkInsertFamily(settings().insertDepth());
            replace(settings().withInsertFamily(Optional.of(linkInsertFamilyId())));
        } else {
            replace(settings().withInsertFamily(Optional.empty()));
        }
    }

    public Optional<String> insertFamily() {
        return settings().insertFamily();
    }

    public void insertFamily(Optional<String> value) {
        replace(settings().withInsertFamily(value));
    }

    public int insertDepth() {
        return settings().insertDepth();
    }

    public void insertDepth(int value) {
        MKFloorTopologySettings updated = settings().withInsertDepth(value);
        if (updated.insertFamily().isPresent()) {
            ensureLinkInsertFamily(value);
            updated = updated.withInsertFamily(Optional.of(linkInsertFamilyId()));
        }
        replace(updated);
    }

    public int insertSpacing() {
        return settings().insertSpacing();
    }

    public void insertSpacing(int value) {
        replace(withLinkInsertFamilyIfEnabled(settings().withInsertSpacing(value)));
    }

    public float insertProbability() {
        return settings().insertProbability();
    }

    public void insertProbability(float value) {
        replace(withLinkInsertFamilyIfEnabled(settings().withInsertProbability(value)));
    }

    public float insertMaxDecay() {
        return settings().insertMaxDecay();
    }

    public void insertMaxDecay(float value) {
        replace(withLinkInsertFamilyIfEnabled(settings().withInsertMaxDecay(value)));
    }

    private void ensureLinkInsertFamily(int depth) {
        session.ensureFloorLinkInsertFamily(linkInsertFamilyId(), linkInsertWidth(), linkInsertHeight(), depth);
    }

    private MKFloorTopologySettings withLinkInsertFamilyIfEnabled(MKFloorTopologySettings updated) {
        if (updated.insertFamily().isEmpty()) {
            return updated;
        }
        ensureLinkInsertFamily(updated.insertDepth());
        return updated.withInsertFamily(Optional.of(linkInsertFamilyId()));
    }

    private String linkInsertFamilyId() {
        return sanitizeInsertFamilyId(topologyGroupId() + ".link_insert");
    }

    private int linkInsertWidth() {
        return linkOpeningProfile().map(MKHorizontalOpeningProfile::openingWidth).orElse(3) + 2;
    }

    private int linkInsertHeight() {
        return linkOpeningProfile().map(MKHorizontalOpeningProfile::openingHeight).orElse(3) + 2;
    }

    private Optional<MKHorizontalOpeningProfile> linkOpeningProfile() {
        return session.firstCompatibleOpeningProfileId(MKHorizontalExitPathKind.BRANCH)
                .flatMap(session::getOpeningProfile);
    }

    private String sanitizeInsertFamilyId(String value) {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                builder.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append('_');
            }
        }
        return builder.toString().replaceAll("_+", "_").replaceAll("^_|_$", "");
    }

    public long previewSeed() {
        return lockedLayoutSeed()
                .orElseGet(() -> session.viewState.floorTopologyPreviewSeeds.computeIfAbsent(
                        MKFloorTopologySettings.key(stackId, floorRole),
                        key -> (long) key.hashCode()));
    }

    public void rerollPreviewSeed() {
        if (lockedLayoutSeed().isPresent()) {
            return;
        }
        String key = MKFloorTopologySettings.key(stackId, floorRole);
        long current = previewSeed();
        session.viewState.floorTopologyPreviewSeeds.put(key,
                current * 6364136223846793005L + 1442695040888963407L);
    }

    public Optional<Long> lockedLayoutSeed() {
        return settings().lockedLayoutSeed();
    }

    public void lockLayoutSeed() {
        long seed = session.viewState.floorTopologyPreviewSeeds.computeIfAbsent(
                MKFloorTopologySettings.key(stackId, floorRole), key -> (long) key.hashCode());
        replace(settings().withLockedLayoutSeed(Optional.of(seed)));
    }

    public void unlockLayoutSeed() {
        replace(settings().withLockedLayoutSeed(Optional.empty()));
    }

    public int roomWidth(MKFloorRoomKind kind) {
        return roomProfile(kind).width();
    }

    public void roomWidth(MKFloorRoomKind kind, int value) {
        roomWidth(kind, 0, value);
    }

    public void roomWidth(MKFloorRoomKind kind, int index, int value) {
        MKFloorRoomProfile profile = roomProfile(kind, index).withWidth(value);
        replace(settings().withRoomProfile(kind, index, profile));
    }

    public int roomLength(MKFloorRoomKind kind) {
        return roomProfile(kind).length();
    }

    public void roomLength(MKFloorRoomKind kind, int value) {
        roomLength(kind, 0, value);
    }

    public void roomLength(MKFloorRoomKind kind, int index, int value) {
        MKFloorRoomProfile profile = roomProfile(kind, index).withLength(value);
        replace(settings().withRoomProfile(kind, index, profile));
    }

    public int roomHeight(MKFloorRoomKind kind) {
        return roomProfile(kind).height();
    }

    public void roomHeight(MKFloorRoomKind kind, int value) {
        roomHeight(kind, 0, value);
    }

    public void roomHeight(MKFloorRoomKind kind, int index, int value) {
        int clamped = clamp(value, MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, roomHeightMax());
        MKFloorRoomProfile profile = roomProfile(kind, index).withHeight(clamped);
        replace(settings().withRoomProfile(kind, index, profile));
    }

    public int roomHeightMax() {
        MKWorkspaceVerticalStackSettings stackSettings = session.verticalStackSettings(stackId);
        return switch (floorRole) {
            case "main_floor" -> stackSettings.mainHeight();
            case "basement_floor" -> stackSettings.basementHeight();
            case "basement_entry" -> stackSettings.basementEntryHeight();
            case "basement_cap", "basement_cap_approach" -> stackSettings.basementCapHeight();
            case "top_cap", "top_cap_approach" -> stackSettings.mainCapHeight();
            default -> stackSettings.entryHeight();
        };
    }

    public List<MKFloorRoomProfile> roomProfiles(MKFloorRoomKind kind) {
        List<MKFloorRoomProfile> profiles = roomProfilesForKind(settings(), kind);
        if (!profiles.isEmpty()) {
            return profiles;
        }
        return List.of(defaultRoomProfile(kind));
    }

    public void addRoomProfile(MKFloorRoomKind kind) {
        List<MKFloorRoomProfile> profiles = roomProfiles(kind);
        int nextIndex = profiles.size();
        MKFloorRoomProfile source = profiles.getLast();
        MKFloorRoomProfile added = MKFloorRoomProfile.defaults(
                kind,
                source.width(),
                source.length(),
                source.height()
        ).withIdentity(
                kind.getSerializedName() + "_" + nextIndex,
                WorkspaceTopologyUiSupport.formatTopologyLabel(kind.getSerializedName()) + " " + (nextIndex + 1));
        replace(settings().withAddedRoomProfile(kind, added));
    }

    public void removeRoomProfile(MKFloorRoomKind kind, int index) {
        replace(settings().withRemovedRoomProfile(kind, index));
    }

    public void setRoomMainExitDirection(MKFloorRoomKind kind, int index, Direction direction) {
        MKFloorRoomProfile profile = roomProfile(kind, index);
        if (!profile.mainExitDirection(direction)) {
            return;
        }
        ArrayList<MKFamilyHorizontalExitDefinition> exits = new ArrayList<>();
        for (MKFamilyHorizontalExitDefinition exit : profile.horizontalExits()) {
            if (exit.pathKind() == MKHorizontalExitPathKind.MAIN_EXIT ||
                    exit.direction() == direction) {
                continue;
            }
            exits.add(exit);
        }
        exits.add(new MKFamilyHorizontalExitDefinition(
                direction,
                MKHorizontalExitPathKind.MAIN_EXIT,
                MKFloorRoomProfile.INHERITED_MAIN_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        ));
        replace(settings().withRoomProfile(kind, index, profile.withHorizontalExits(exits)));
    }

    public void toggleRoomBranchExit(MKFloorRoomKind kind, int index, Direction direction) {
        MKFloorRoomProfile profile = roomProfile(kind, index);
        if (!profile.optionalBranchExitDirection(direction)) {
            return;
        }
        ArrayList<MKFamilyHorizontalExitDefinition> exits = new ArrayList<>(profile.horizontalExits());
        Optional<MKFamilyHorizontalExitDefinition> existing = exits.stream()
                .filter(exit -> exit.direction() == direction)
                .findFirst();
        if (existing.isPresent()) {
            exits.remove(existing.get());
        } else {
            exits.add(new MKFamilyHorizontalExitDefinition(
                    direction,
                    MKHorizontalExitPathKind.BRANCH,
                    MKFloorRoomProfile.INHERITED_BRANCH_OPENING_PROFILE_ID,
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
        }
        replace(settings().withRoomProfile(kind, index, profile.withHorizontalExits(exits)));
    }

    public void toggleRoomLinkCandidateExit(MKFloorRoomKind kind, int index, Direction direction) {
        MKFloorRoomProfile profile = roomProfile(kind, index);
        if (!profile.linkCandidateExitDirection(direction)) {
            return;
        }
        ArrayList<MKFamilyHorizontalExitDefinition> exits = new ArrayList<>(profile.horizontalExits());
        Optional<MKFamilyHorizontalExitDefinition> existing = exits.stream()
                .filter(exit -> exit.direction() == direction &&
                        exit.pathKind() == MKHorizontalExitPathKind.LINK_CANDIDATE)
                .findFirst();
        if (existing.isPresent()) {
            exits.remove(existing.get());
        } else {
            exits.removeIf(exit -> exit.direction() == direction);
            exits.add(new MKFamilyHorizontalExitDefinition(
                    direction,
                    MKHorizontalExitPathKind.LINK_CANDIDATE,
                    MKFloorRoomProfile.INHERITED_LINK_OPENING_PROFILE_ID,
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
        }
        replace(settings().withRoomProfile(kind, index, profile.withHorizontalExits(exits)));
    }

    public void setRoomRandomizeMainExit(MKFloorRoomKind kind, int index, boolean value) {
        MKFloorRoomProfile profile = roomProfile(kind, index);
        replace(settings().withRoomProfile(kind, index, profile.withRandomizeMainExit(value)));
    }

    private MKFloorTopologySettings settings() {
        return session.floorTopologySettings(stackId, floorRole);
    }

    private void replace(MKFloorTopologySettings settings) {
        session.replaceFloorTopologySettings(settings);
    }

    private MKFloorRoomProfile roomProfile(MKFloorRoomKind kind) {
        return roomProfile(kind, 0);
    }

    private MKFloorRoomProfile roomProfile(MKFloorRoomKind kind, int index) {
        List<MKFloorRoomProfile> profiles = roomProfilesForKind(settings(), kind);
        if (index >= 0 && index < profiles.size()) {
            return profiles.get(index);
        }
        return defaultRoomProfile(kind);
    }

    private MKFloorRoomProfile defaultRoomProfile(MKFloorRoomKind kind) {
        MKWorkspaceVerticalStackSettings stackSettings = session.verticalStackSettings(stackId);
        return MKFloorRoomProfile.defaults(kind, stackSettings.width(), stackSettings.length(),
                roomHeightMax());
    }

    private List<MKFloorRoomProfile> roomProfilesForKind(MKFloorTopologySettings settings,
                                                                  MKFloorRoomKind kind) {
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
