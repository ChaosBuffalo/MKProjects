package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceTopologyProfile(
        String profileType,
        boolean uniqueCornerTowers,
        boolean uniqueNorthWestCornerTower,
        boolean uniqueNorthEastCornerTower,
        boolean uniqueSouthEastCornerTower,
        boolean uniqueSouthWestCornerTower
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
                    .forGetter(MKWorkspaceTopologyProfile::uniqueSouthWestCornerTower)
    ).apply(instance, MKWorkspaceTopologyProfile::new));

    public static MKWorkspaceTopologyProfile tower() {
        return new MKWorkspaceTopologyProfile(TOWER_PROFILE_TYPE, false, false, false, false, false);
    }

    public static MKWorkspaceTopologyProfile walledKeep(boolean uniqueCornerTowers) {
        return new MKWorkspaceTopologyProfile(WALLED_KEEP_PROFILE_TYPE, uniqueCornerTowers,
                uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers);
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
                uniqueSouthWestCornerTower
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
}
