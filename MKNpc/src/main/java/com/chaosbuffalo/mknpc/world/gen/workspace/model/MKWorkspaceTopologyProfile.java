package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MKWorkspaceTopologyProfile(
        String profileType,
        boolean uniqueCornerTowers,
        boolean uniqueNorthWestCornerTower,
        boolean uniqueNorthEastCornerTower,
        boolean uniqueSouthEastCornerTower,
        boolean uniqueSouthWestCornerTower,
        List<MKWorkspaceTowerStackSettings> towerStackSettings
) {
    public static final String TOWER_PROFILE_TYPE = "tower";
    public static final String WALLED_KEEP_PROFILE_TYPE = "walled_keep";

    public static final Codec<MKWorkspaceTopologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("profile_type", TOWER_PROFILE_TYPE)
                    .forGetter(MKWorkspaceTopologyProfile::profileType),
            Codec.BOOL.optionalFieldOf("unique_corner_towers", false)
                    .forGetter(MKWorkspaceTopologyProfile::uniqueCornerTowers),
            Codec.BOOL.optionalFieldOf("unique_north_west_corner_tower", false)
                    .forGetter(MKWorkspaceTopologyProfile::uniqueNorthWestCornerTower),
            Codec.BOOL.optionalFieldOf("unique_north_east_corner_tower", false)
                    .forGetter(MKWorkspaceTopologyProfile::uniqueNorthEastCornerTower),
            Codec.BOOL.optionalFieldOf("unique_south_east_corner_tower", false)
                    .forGetter(MKWorkspaceTopologyProfile::uniqueSouthEastCornerTower),
            Codec.BOOL.optionalFieldOf("unique_south_west_corner_tower", false)
                    .forGetter(MKWorkspaceTopologyProfile::uniqueSouthWestCornerTower),
            MKWorkspaceTowerStackSettings.CODEC.listOf().optionalFieldOf("tower_stack_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::towerStackSettings)
    ).apply(instance, MKWorkspaceTopologyProfile::new));

    public static MKWorkspaceTopologyProfile tower() {
        return new MKWorkspaceTopologyProfile(TOWER_PROFILE_TYPE, false, false, false, false, false, List.of());
    }

    public static MKWorkspaceTopologyProfile walledKeep(boolean uniqueCornerTowers) {
        return new MKWorkspaceTopologyProfile(WALLED_KEEP_PROFILE_TYPE, uniqueCornerTowers,
                uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers,
                defaultWalledKeepTowerStackSettings(uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers,
                        uniqueCornerTowers));
    }

    public static MKWorkspaceTopologyProfile walledKeep(boolean uniqueNorthWestCornerTower,
                                                        boolean uniqueNorthEastCornerTower,
                                                        boolean uniqueSouthEastCornerTower,
                                                        boolean uniqueSouthWestCornerTower) {
        return new MKWorkspaceTopologyProfile(
                WALLED_KEEP_PROFILE_TYPE,
                uniqueNorthWestCornerTower && uniqueNorthEastCornerTower &&
                        uniqueSouthEastCornerTower && uniqueSouthWestCornerTower,
                uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower,
                uniqueSouthEastCornerTower,
                uniqueSouthWestCornerTower,
                defaultWalledKeepTowerStackSettings(uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                        uniqueSouthEastCornerTower, uniqueSouthWestCornerTower)
        );
    }

    public MKWorkspaceTopologyProfile {
        if (profileType == null || profileType.isBlank()) {
            profileType = TOWER_PROFILE_TYPE;
        }
        if (uniqueCornerTowers) {
            uniqueNorthWestCornerTower = true;
            uniqueNorthEastCornerTower = true;
            uniqueSouthEastCornerTower = true;
            uniqueSouthWestCornerTower = true;
        }
        uniqueCornerTowers = uniqueNorthWestCornerTower && uniqueNorthEastCornerTower &&
                uniqueSouthEastCornerTower && uniqueSouthWestCornerTower;
        towerStackSettings = normalizeTowerStackSettings(profileType, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                towerStackSettings);
    }

    public boolean anySharedCornerTower() {
        return !uniqueNorthWestCornerTower || !uniqueNorthEastCornerTower ||
                !uniqueSouthEastCornerTower || !uniqueSouthWestCornerTower;
    }

    public boolean uniqueCornerTower(String topologySlotId) {
        return switch (topologySlotId) {
            case "keep.corner.north_west" -> uniqueNorthWestCornerTower;
            case "keep.corner.north_east" -> uniqueNorthEastCornerTower;
            case "keep.corner.south_east" -> uniqueSouthEastCornerTower;
            case "keep.corner.south_west" -> uniqueSouthWestCornerTower;
            default -> false;
        };
    }

    public Optional<MKWorkspaceTowerStackSettings> towerStackSettings(String stackId) {
        return towerStackSettings.stream()
                .filter(settings -> settings.stackId().equals(stackId))
                .findFirst();
    }

    public MKWorkspaceTowerStackSettings towerStackSettingsOrDefault(String stackId) {
        return towerStackSettings(stackId)
                .orElseGet(() -> MKWorkspaceTowerStackSettings.defaults(stackId, 7));
    }

    public MKWorkspaceTopologyProfile withTowerStackSettings(MKWorkspaceTowerStackSettings updatedSettings) {
        ArrayList<MKWorkspaceTowerStackSettings> updated = new ArrayList<>();
        boolean replaced = false;
        for (MKWorkspaceTowerStackSettings settings : towerStackSettings) {
            if (settings.stackId().equals(updatedSettings.stackId())) {
                updated.add(updatedSettings);
                replaced = true;
            } else {
                updated.add(settings);
            }
        }
        if (!replaced) {
            updated.add(updatedSettings);
        }
        return new MKWorkspaceTopologyProfile(profileType, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower, updated);
    }

    public static List<MKWorkspaceTowerStackSettings> defaultWalledKeepTowerStackSettings(
            boolean uniqueNorthWestCornerTower,
            boolean uniqueNorthEastCornerTower,
            boolean uniqueSouthEastCornerTower,
            boolean uniqueSouthWestCornerTower) {
        ArrayList<MKWorkspaceTowerStackSettings> settings = new ArrayList<>();
        settings.add(MKWorkspaceTowerStackSettings.defaults("keep.center", 7));
        if (!uniqueNorthWestCornerTower || !uniqueNorthEastCornerTower ||
                !uniqueSouthEastCornerTower || !uniqueSouthWestCornerTower) {
            settings.add(MKWorkspaceTowerStackSettings.defaults("keep.corner.shared", 7));
        }
        if (uniqueNorthWestCornerTower) {
            settings.add(MKWorkspaceTowerStackSettings.defaults("keep.corner.north_west", 7));
        }
        if (uniqueNorthEastCornerTower) {
            settings.add(MKWorkspaceTowerStackSettings.defaults("keep.corner.north_east", 7));
        }
        if (uniqueSouthEastCornerTower) {
            settings.add(MKWorkspaceTowerStackSettings.defaults("keep.corner.south_east", 7));
        }
        if (uniqueSouthWestCornerTower) {
            settings.add(MKWorkspaceTowerStackSettings.defaults("keep.corner.south_west", 7));
        }
        return List.copyOf(settings);
    }

    private static List<MKWorkspaceTowerStackSettings> normalizeTowerStackSettings(
            String profileType,
            boolean uniqueNorthWestCornerTower,
            boolean uniqueNorthEastCornerTower,
            boolean uniqueSouthEastCornerTower,
            boolean uniqueSouthWestCornerTower,
            List<MKWorkspaceTowerStackSettings> currentSettings) {
        if (!WALLED_KEEP_PROFILE_TYPE.equals(profileType)) {
            return List.copyOf(currentSettings == null ? List.of() : currentSettings);
        }
        ArrayList<MKWorkspaceTowerStackSettings> normalized = new ArrayList<>();
        List<MKWorkspaceTowerStackSettings> defaults = defaultWalledKeepTowerStackSettings(uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower);
        for (MKWorkspaceTowerStackSettings defaultSettings : defaults) {
            MKWorkspaceTowerStackSettings settings = currentSettings == null ? null : currentSettings.stream()
                    .filter(existing -> existing.stackId().equals(defaultSettings.stackId()))
                    .findFirst()
                    .orElse(null);
            normalized.add(settings == null ? defaultSettings : settings);
        }
        return List.copyOf(normalized);
    }
}
