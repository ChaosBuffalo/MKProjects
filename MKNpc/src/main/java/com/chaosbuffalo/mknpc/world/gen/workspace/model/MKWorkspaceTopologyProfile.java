package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MKWorkspaceTopologyProfile(
        ResourceLocation plannerId,
        boolean uniqueCornerTowers,
        boolean uniqueNorthWestCornerTower,
        boolean uniqueNorthEastCornerTower,
        boolean uniqueSouthEastCornerTower,
        boolean uniqueSouthWestCornerTower,
        List<MKWorkspaceTopologyGroupSettings> topologyGroupSettings,
        List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
        List<MKWorkspaceFloorTopologySettings> floorTopologySettings,
        List<MKWorkspaceTopologyPathSettings> pathSettings,
        MKWalledKeepCourtyardSettings courtyardSettings,
        TerrainAdjustment terrainAdjustment
) {
    private static final ResourceLocation DEFAULT_PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    private static final Codec<TerrainAdjustment> TERRAIN_ADJUSTMENT_CODEC =
            StringRepresentable.fromEnum(TerrainAdjustment::values);

    public static final Codec<MKWorkspaceTopologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("planner_id", DEFAULT_PLANNER_ID)
                    .forGetter(MKWorkspaceTopologyProfile::plannerId),
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
            MKWorkspaceTopologyGroupSettings.CODEC.listOf().optionalFieldOf("topology_group_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::topologyGroupSettings),
            MKWorkspaceVerticalStackSettings.CODEC.listOf().optionalFieldOf("vertical_stack_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::verticalStackSettings),
            MKWorkspaceFloorTopologySettings.CODEC.listOf().optionalFieldOf("floor_topology_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::floorTopologySettings),
            MKWorkspaceTopologyPathSettings.CODEC.listOf().optionalFieldOf("path_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::pathSettings),
            MKWalledKeepCourtyardSettings.CODEC.optionalFieldOf("courtyard_settings",
                            MKWalledKeepCourtyardSettings.defaults())
                    .forGetter(MKWorkspaceTopologyProfile::courtyardSettings),
            TERRAIN_ADJUSTMENT_CODEC.optionalFieldOf("terrain_adjustment", TerrainAdjustment.BEARD_THIN)
                    .forGetter(MKWorkspaceTopologyProfile::terrainAdjustment)
    ).apply(instance, MKWorkspaceTopologyProfile::new));

    public MKWorkspaceTopologyProfile(ResourceLocation plannerId,
                                      boolean uniqueCornerTowers,
                                      boolean uniqueNorthWestCornerTower,
                                      boolean uniqueNorthEastCornerTower,
                                      boolean uniqueSouthEastCornerTower,
                                      boolean uniqueSouthWestCornerTower,
                                      List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
                                      List<MKWorkspaceFloorTopologySettings> floorTopologySettings,
                                      List<MKWorkspaceTopologyPathSettings> pathSettings,
                                      MKWalledKeepCourtyardSettings courtyardSettings,
                                      TerrainAdjustment terrainAdjustment) {
        this(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                uniqueSouthEastCornerTower, uniqueSouthWestCornerTower, List.of(), verticalStackSettings,
                floorTopologySettings, pathSettings, courtyardSettings, terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile {
        if (plannerId == null) {
            plannerId = DEFAULT_PLANNER_ID;
        }
        if (uniqueCornerTowers) {
            uniqueNorthWestCornerTower = true;
            uniqueNorthEastCornerTower = true;
            uniqueSouthEastCornerTower = true;
            uniqueSouthWestCornerTower = true;
        }
        uniqueCornerTowers = uniqueNorthWestCornerTower && uniqueNorthEastCornerTower &&
                uniqueSouthEastCornerTower && uniqueSouthWestCornerTower;
        verticalStackSettings = List.copyOf(verticalStackSettings == null ? List.of() : verticalStackSettings);
        topologyGroupSettings = MKWorkspaceTopologyGroupSettings.normalize(topologyGroupSettings);
        floorTopologySettings = MKWorkspaceFloorTopologySettings.normalize(floorTopologySettings, verticalStackSettings);
        pathSettings = MKWorkspaceTopologyPathSettings.normalize(pathSettings);
        courtyardSettings = courtyardSettings == null ? MKWalledKeepCourtyardSettings.defaults() : courtyardSettings;
        terrainAdjustment = terrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : terrainAdjustment;
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

    public Optional<MKWorkspaceVerticalStackSettings> verticalStackSettings(String stackId) {
        return verticalStackSettings.stream()
                .filter(settings -> settings.stackId().equals(stackId))
                .findFirst();
    }

    public Optional<MKWorkspaceTopologyGroupSettings> topologyGroupSettings(String topologyGroupId) {
        return MKWorkspaceTopologyGroupSettings.find(topologyGroupSettings, topologyGroupId);
    }

    public Optional<MKWorkspacePaletteOverride> topologyGroupPaletteOverride(String topologyGroupId) {
        return topologyGroupSettings(topologyGroupId)
                .flatMap(MKWorkspaceTopologyGroupSettings::paletteOverride);
    }

    public MKWorkspaceTopologyProfile withTopologyGroupSettings(MKWorkspaceTopologyGroupSettings updatedSettings) {
        ArrayList<MKWorkspaceTopologyGroupSettings> updated = new ArrayList<>();
        boolean replaced = false;
        for (MKWorkspaceTopologyGroupSettings settings : topologyGroupSettings) {
            if (settings.topologyGroupId().equals(updatedSettings.topologyGroupId())) {
                if (updatedSettings.paletteOverride().isPresent()) {
                    updated.add(updatedSettings);
                }
                replaced = true;
            } else {
                updated.add(settings);
            }
        }
        if (!replaced && updatedSettings.paletteOverride().isPresent()) {
            updated.add(updatedSettings);
        }
        return new MKWorkspaceTopologyProfile(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                updated, verticalStackSettings, floorTopologySettings, pathSettings, courtyardSettings,
                terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile withTopologyGroupPaletteOverride(
            String topologyGroupId, Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return withTopologyGroupSettings(MKWorkspaceTopologyGroupSettings.palette(topologyGroupId, paletteOverride));
    }

    public MKWorkspaceVerticalStackSettings verticalStackSettingsOrDefault(String stackId) {
        return verticalStackSettings(stackId)
                .orElseGet(() -> MKWorkspaceVerticalStackSettings.defaults(stackId, 7));
    }

    public MKWorkspaceTopologyProfile withVerticalStackSettings(MKWorkspaceVerticalStackSettings updatedSettings) {
        ArrayList<MKWorkspaceVerticalStackSettings> updated = new ArrayList<>();
        boolean replaced = false;
        for (MKWorkspaceVerticalStackSettings settings : verticalStackSettings) {
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
        return new MKWorkspaceTopologyProfile(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                topologyGroupSettings, updated, floorTopologySettings, pathSettings, courtyardSettings,
                terrainAdjustment);
    }

    public Optional<MKWorkspaceFloorTopologySettings> floorTopologySettings(String stackId, String floorRole) {
        return MKWorkspaceFloorTopologySettings.find(floorTopologySettings, stackId, floorRole);
    }

    public MKWorkspaceFloorTopologySettings floorTopologySettingsOrDefault(String stackId, String floorRole) {
        return floorTopologySettings(stackId, floorRole)
                .orElseGet(() -> MKWorkspaceFloorTopologySettings.defaults(
                        verticalStackSettingsOrDefault(stackId), floorRole));
    }

    public MKWorkspaceTopologyProfile withFloorTopologySettings(MKWorkspaceFloorTopologySettings updatedSettings) {
        ArrayList<MKWorkspaceFloorTopologySettings> updated = new ArrayList<>();
        boolean replaced = false;
        for (MKWorkspaceFloorTopologySettings settings : floorTopologySettings) {
            if (settings.key().equals(updatedSettings.key())) {
                updated.add(updatedSettings);
                replaced = true;
            } else {
                updated.add(settings);
            }
        }
        if (!replaced) {
            updated.add(updatedSettings);
        }
        return new MKWorkspaceTopologyProfile(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                topologyGroupSettings, verticalStackSettings, updated, pathSettings, courtyardSettings,
                terrainAdjustment);
    }

    public Optional<MKWorkspaceTopologyPathSettings> pathSettings(String topologyGroupId) {
        return MKWorkspaceTopologyPathSettings.find(pathSettings, topologyGroupId);
    }

    public MKWorkspaceTopologyPathSettings pathSettingsOrDefault(String topologyGroupId) {
        return pathSettings(topologyGroupId)
                .orElseGet(() -> MKWorkspaceTopologyPathSettings.defaultForTopologyGroup(topologyGroupId));
    }

    public MKWorkspaceTopologyProfile withPathSettings(MKWorkspaceTopologyPathSettings updatedSettings) {
        ArrayList<MKWorkspaceTopologyPathSettings> updated = new ArrayList<>();
        boolean replaced = false;
        for (MKWorkspaceTopologyPathSettings settings : pathSettings) {
            if (settings.topologyGroupId().equals(updatedSettings.topologyGroupId())) {
                updated.add(updatedSettings);
                replaced = true;
            } else {
                updated.add(settings);
            }
        }
        if (!replaced) {
            updated.add(updatedSettings);
        }
        return new MKWorkspaceTopologyProfile(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                topologyGroupSettings, verticalStackSettings, floorTopologySettings, updated, courtyardSettings,
                terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile withCourtyardSettings(MKWalledKeepCourtyardSettings updatedSettings) {
        return new MKWorkspaceTopologyProfile(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                topologyGroupSettings, verticalStackSettings, floorTopologySettings, pathSettings,
                updatedSettings == null ? MKWalledKeepCourtyardSettings.defaults() : updatedSettings,
                terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile withTerrainAdjustment(TerrainAdjustment updatedTerrainAdjustment) {
        return new MKWorkspaceTopologyProfile(plannerId, uniqueCornerTowers, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                topologyGroupSettings, verticalStackSettings, floorTopologySettings, pathSettings, courtyardSettings,
                updatedTerrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : updatedTerrainAdjustment);
    }
}
