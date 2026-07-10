package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerSettingsEntry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record MKWalledKeepPlannerSettings(
        boolean uniqueNorthWestCornerTower,
        boolean uniqueNorthEastCornerTower,
        boolean uniqueSouthEastCornerTower,
        boolean uniqueSouthWestCornerTower,
        MKWalledKeepCourtyardSettings courtyardSettings
) {
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "walled_keep");
    public static final String SCOPE_ID = "keep";
    public static final Codec<MKWalledKeepPlannerSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("unique_north_west_corner_tower", false)
                    .forGetter(MKWalledKeepPlannerSettings::uniqueNorthWestCornerTower),
            Codec.BOOL.optionalFieldOf("unique_north_east_corner_tower", false)
                    .forGetter(MKWalledKeepPlannerSettings::uniqueNorthEastCornerTower),
            Codec.BOOL.optionalFieldOf("unique_south_east_corner_tower", false)
                    .forGetter(MKWalledKeepPlannerSettings::uniqueSouthEastCornerTower),
            Codec.BOOL.optionalFieldOf("unique_south_west_corner_tower", false)
                    .forGetter(MKWalledKeepPlannerSettings::uniqueSouthWestCornerTower),
            MKWalledKeepCourtyardSettings.CODEC.optionalFieldOf("courtyard_settings",
                            MKWalledKeepCourtyardSettings.defaults())
                    .forGetter(MKWalledKeepPlannerSettings::courtyardSettings)
    ).apply(instance, MKWalledKeepPlannerSettings::new));

    public MKWalledKeepPlannerSettings {
        courtyardSettings = courtyardSettings == null ? MKWalledKeepCourtyardSettings.defaults() : courtyardSettings;
    }

    public static MKWalledKeepPlannerSettings defaults() {
        return cornerModes(false, false, false, false);
    }

    public static MKWalledKeepPlannerSettings cornerModes(boolean northWest, boolean northEast,
                                                          boolean southEast, boolean southWest) {
        return new MKWalledKeepPlannerSettings(northWest, northEast, southEast, southWest,
                MKWalledKeepCourtyardSettings.defaults());
    }

    public static MKWalledKeepPlannerSettings from(MKWorkspaceTopologyProfile profile) {
        return profile.plannerSettingsEntry(PLANNER_ID, SCOPE_ID)
                .map(entry -> MKWorkspaceCodecs.parseNbt(CODEC, entry.settings(), "walled keep planner settings"))
                .orElseGet(MKWalledKeepPlannerSettings::defaults);
    }

    public boolean uniqueCornerTowers() {
        return uniqueNorthWestCornerTower && uniqueNorthEastCornerTower &&
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

    public MKWalledKeepPlannerSettings withCornerModes(boolean northWest, boolean northEast,
                                                       boolean southEast, boolean southWest) {
        return new MKWalledKeepPlannerSettings(northWest, northEast, southEast, southWest, courtyardSettings);
    }

    public MKWalledKeepPlannerSettings withCornerMode(String topologySlotId, boolean value) {
        return switch (topologySlotId) {
            case "keep.corner.north_west" -> withCornerModes(value, uniqueNorthEastCornerTower,
                    uniqueSouthEastCornerTower, uniqueSouthWestCornerTower);
            case "keep.corner.north_east" -> withCornerModes(uniqueNorthWestCornerTower, value,
                    uniqueSouthEastCornerTower, uniqueSouthWestCornerTower);
            case "keep.corner.south_east" -> withCornerModes(uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                    value, uniqueSouthWestCornerTower);
            case "keep.corner.south_west" -> withCornerModes(uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                    uniqueSouthEastCornerTower, value);
            default -> this;
        };
    }

    public MKWalledKeepPlannerSettings withCourtyardSettings(MKWalledKeepCourtyardSettings updatedSettings) {
        return new MKWalledKeepPlannerSettings(uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                updatedSettings == null ? MKWalledKeepCourtyardSettings.defaults() : updatedSettings);
    }

    public MKWorkspaceTopologyProfile applyTo(MKWorkspaceTopologyProfile profile) {
        return profile.withPlannerSettingsEntry(new MKWorkspacePlannerSettingsEntry(
                PLANNER_ID,
                SCOPE_ID,
                MKWorkspaceCodecs.encodeNbt(CODEC, this, "walled keep planner settings")));
    }
}
