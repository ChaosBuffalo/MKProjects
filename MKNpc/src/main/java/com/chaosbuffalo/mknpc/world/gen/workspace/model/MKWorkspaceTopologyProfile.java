package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceTopologyProfile(
        String profileType,
        boolean uniqueCornerTowers
) {
    public static final String TOWER_PROFILE_TYPE = "tower";
    public static final String WALLED_KEEP_PROFILE_TYPE = "walled_keep";

    public static final Codec<MKWorkspaceTopologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("profile_type", TOWER_PROFILE_TYPE)
                    .forGetter(MKWorkspaceTopologyProfile::profileType),
            Codec.BOOL.optionalFieldOf("unique_corner_towers", false)
                    .forGetter(MKWorkspaceTopologyProfile::uniqueCornerTowers)
    ).apply(instance, MKWorkspaceTopologyProfile::new));

    public static MKWorkspaceTopologyProfile tower() {
        return new MKWorkspaceTopologyProfile(TOWER_PROFILE_TYPE, false);
    }

    public static MKWorkspaceTopologyProfile walledKeep(boolean uniqueCornerTowers) {
        return new MKWorkspaceTopologyProfile(WALLED_KEEP_PROFILE_TYPE, uniqueCornerTowers);
    }

    public MKWorkspaceTopologyProfile {
        if (profileType == null || profileType.isBlank()) {
            profileType = TOWER_PROFILE_TYPE;
        }
    }
}
