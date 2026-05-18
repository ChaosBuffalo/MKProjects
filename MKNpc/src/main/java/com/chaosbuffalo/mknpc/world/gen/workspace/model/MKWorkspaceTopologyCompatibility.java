package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.List;
import java.util.Optional;

public final class MKWorkspaceTopologyCompatibility {
    private MKWorkspaceTopologyCompatibility() {
    }

    public static List<MKTowerWorkspaceCategoryProfile> categoryProfilesForWorkspace(
            MKStructureWorkspace workspace) {
        return categoryProfiles(
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.categoryProfiles()
        );
    }

    public static List<MKTowerWorkspaceCategoryProfile> categoryProfiles(
            MKWorkspaceTopologyProfile topologyProfile,
            MKWorkspaceDimensions dimensions,
            List<MKTowerWorkspaceCategoryProfile> legacyProfiles) {
        if (topologyProfile == null || topologyProfile.towerStackSettings().isEmpty()) {
            return withPathSettings(topologyProfile, legacyOrDefault(dimensions, legacyProfiles));
        }
        MKWorkspaceTowerStackSettings settings = primaryStackSettings(topologyProfile)
                .orElseGet(() -> topologyProfile.towerStackSettings().getFirst());
        return categoryProfilesForStack(topologyProfile, settings, legacyProfiles);
    }

    public static List<MKTowerWorkspaceCategoryProfile> categoryProfilesForStack(
            MKWorkspaceTopologyProfile topologyProfile,
            MKWorkspaceTowerStackSettings settings,
            List<MKTowerWorkspaceCategoryProfile> legacyProfiles) {
        return List.of(
                categoryProfileForStack(topologyProfile, settings, MKTowerWorkspaceCategory.ENTRY, legacyProfiles),
                categoryProfileForStack(topologyProfile, settings, MKTowerWorkspaceCategory.MAIN, legacyProfiles),
                categoryProfileForStack(topologyProfile, settings, MKTowerWorkspaceCategory.BASEMENT, legacyProfiles),
                categoryProfileForStack(topologyProfile, settings, MKTowerWorkspaceCategory.TOP_CAP, legacyProfiles),
                categoryProfileForStack(topologyProfile, settings, MKTowerWorkspaceCategory.BASEMENT_CAP, legacyProfiles)
        );
    }

    public static MKTowerWorkspaceCategoryProfile categoryProfileForStack(
            MKWorkspaceTopologyProfile topologyProfile,
            MKWorkspaceTowerStackSettings settings,
            MKTowerWorkspaceCategory category,
            List<MKTowerWorkspaceCategoryProfile> legacyProfiles) {
        MKWorkspaceTopologyPathSettings pathSettings = pathSettings(topologyProfile, category);
        Optional<MKWorkspacePaletteOverride> paletteOverride = legacyProfiles == null ? Optional.empty() :
                legacyProfiles.stream()
                        .filter(profile -> profile.category() == category)
                        .findFirst()
                        .flatMap(MKTowerWorkspaceCategoryProfile::paletteOverrideOpt);
        return new MKTowerWorkspaceCategoryProfile(
                category,
                settings.width(),
                settings.length(),
                settings.height(),
                pathSettings.minMainPathPieces(),
                pathSettings.maxMainPathPieces(),
                pathSettings.maxBranchPiecesBeforeCap(),
                paletteOverride.orElse(null)
        );
    }

    public static Optional<MKWorkspaceTowerStackSettings> primaryStackSettings(
            MKWorkspaceTopologyProfile topologyProfile) {
        if (topologyProfile == null) {
            return Optional.empty();
        }
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfile.profileType())) {
            Optional<MKWorkspaceTowerStackSettings> center = topologyProfile.towerStackSettings("keep.center");
            if (center.isPresent()) {
                return center;
            }
        }
        Optional<MKWorkspaceTowerStackSettings> primary = topologyProfile.towerStackSettings("tower.primary");
        if (primary.isPresent()) {
            return primary;
        }
        return topologyProfile.towerStackSettings().stream().findFirst();
    }

    private static List<MKTowerWorkspaceCategoryProfile> legacyOrDefault(
            MKWorkspaceDimensions dimensions,
            List<MKTowerWorkspaceCategoryProfile> legacyProfiles) {
        if (legacyProfiles != null && !legacyProfiles.isEmpty()) {
            return List.copyOf(legacyProfiles);
        }
        return MKTowerWorkspaceCategoryProfile.createDefaults(dimensions);
    }

    private static List<MKTowerWorkspaceCategoryProfile> withPathSettings(
            MKWorkspaceTopologyProfile topologyProfile,
            List<MKTowerWorkspaceCategoryProfile> profiles) {
        return profiles.stream()
                .map(profile -> {
                    MKWorkspaceTopologyPathSettings pathSettings = pathSettings(topologyProfile, profile.category());
                    return new MKTowerWorkspaceCategoryProfile(
                            profile.category(),
                            profile.roomWidth(),
                            profile.roomLength(),
                            profile.fullHeight(),
                            pathSettings.minMainPathPieces(),
                            pathSettings.maxMainPathPieces(),
                            pathSettings.maxBranchPiecesBeforeCap(),
                            profile.paletteOverride()
                    );
                })
                .toList();
    }

    private static MKWorkspaceTopologyPathSettings pathSettings(MKWorkspaceTopologyProfile topologyProfile,
                                                                MKTowerWorkspaceCategory category) {
        if (topologyProfile == null) {
            return MKWorkspaceTopologyPathSettings.defaultForCategory(category.getSerializedName());
        }
        return topologyProfile.pathSettingsOrDefault(category.getSerializedName());
    }
}
