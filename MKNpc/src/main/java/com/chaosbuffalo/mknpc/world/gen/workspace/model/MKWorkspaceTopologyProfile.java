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
    public static final ResourceLocation TOWER_PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    public static final ResourceLocation WALLED_KEEP_PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "walled_keep");
    public static final TerrainAdjustment DEFAULT_WALLED_KEEP_TERRAIN_ADJUSTMENT = TerrainAdjustment.BEARD_THIN;
    public static final TerrainAdjustment DEFAULT_TOWER_TERRAIN_ADJUSTMENT = TerrainAdjustment.BEARD_THIN;
    private static final Codec<TerrainAdjustment> TERRAIN_ADJUSTMENT_CODEC =
            StringRepresentable.fromEnum(TerrainAdjustment::values);

    public static final Codec<MKWorkspaceTopologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("planner_id", TOWER_PLANNER_ID)
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
            TERRAIN_ADJUSTMENT_CODEC.optionalFieldOf("terrain_adjustment", DEFAULT_WALLED_KEEP_TERRAIN_ADJUSTMENT)
                    .forGetter(MKWorkspaceTopologyProfile::terrainAdjustment)
    ).apply(instance, MKWorkspaceTopologyProfile::new));

    public static MKWorkspaceTopologyProfile tower() {
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.defaultDimensions();
        return new MKWorkspaceTopologyProfile(TOWER_PLANNER_ID, false, false, false, false, false,
                List.of(),
                List.of(new MKWorkspaceVerticalStackSettings("tower.primary",
                        MKWorkspaceTowerStackFloorCounts.DEFAULT_MAIN_FLOORS,
                        MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_FLOORS,
                        dimensions.roomHeight(), dimensions.roomWidth(), dimensions.roomLength(),
                        MKWorkspaceTowerStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED,
                        MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                        MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED)),
                List.of(),
                MKWorkspaceTopologyPathSettings.defaults(),
                MKWalledKeepCourtyardSettings.defaults(),
                DEFAULT_TOWER_TERRAIN_ADJUSTMENT);
    }

    public static MKWorkspaceTopologyProfile walledKeep(boolean uniqueCornerTowers) {
        return new MKWorkspaceTopologyProfile(WALLED_KEEP_PLANNER_ID, uniqueCornerTowers,
                uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers,
                List.of(),
                defaultWalledKeepVerticalStackSettings(uniqueCornerTowers, uniqueCornerTowers, uniqueCornerTowers,
                        uniqueCornerTowers),
                List.of(),
                MKWorkspaceTopologyPathSettings.defaults(),
                MKWalledKeepCourtyardSettings.defaults(),
                DEFAULT_WALLED_KEEP_TERRAIN_ADJUSTMENT);
    }

    public static MKWorkspaceTopologyProfile walledKeep(boolean uniqueNorthWestCornerTower,
                                                        boolean uniqueNorthEastCornerTower,
                                                        boolean uniqueSouthEastCornerTower,
                                                        boolean uniqueSouthWestCornerTower) {
        return new MKWorkspaceTopologyProfile(
                WALLED_KEEP_PLANNER_ID,
                uniqueNorthWestCornerTower && uniqueNorthEastCornerTower &&
                        uniqueSouthEastCornerTower && uniqueSouthWestCornerTower,
                uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower,
                uniqueSouthEastCornerTower,
                uniqueSouthWestCornerTower,
                List.of(),
                defaultWalledKeepVerticalStackSettings(uniqueNorthWestCornerTower, uniqueNorthEastCornerTower,
                        uniqueSouthEastCornerTower, uniqueSouthWestCornerTower),
                List.of(),
                MKWorkspaceTopologyPathSettings.defaults(),
                MKWalledKeepCourtyardSettings.defaults(),
                DEFAULT_WALLED_KEEP_TERRAIN_ADJUSTMENT
        );
    }

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
            plannerId = TOWER_PLANNER_ID;
        }
        if (uniqueCornerTowers) {
            uniqueNorthWestCornerTower = true;
            uniqueNorthEastCornerTower = true;
            uniqueSouthEastCornerTower = true;
            uniqueSouthWestCornerTower = true;
        }
        uniqueCornerTowers = uniqueNorthWestCornerTower && uniqueNorthEastCornerTower &&
                uniqueSouthEastCornerTower && uniqueSouthWestCornerTower;
        verticalStackSettings = normalizeVerticalStackSettings(plannerId, uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower,
                verticalStackSettings);
        topologyGroupSettings = MKWorkspaceTopologyGroupSettings.normalize(topologyGroupSettings);
        floorTopologySettings = MKWorkspaceFloorTopologySettings.normalize(floorTopologySettings, verticalStackSettings);
        pathSettings = MKWorkspaceTopologyPathSettings.normalize(pathSettings);
        courtyardSettings = courtyardSettings == null ? MKWalledKeepCourtyardSettings.defaults() : courtyardSettings;
        terrainAdjustment = terrainAdjustment == null ? defaultTerrainAdjustment(plannerId) : terrainAdjustment;
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
                updatedTerrainAdjustment == null ? defaultTerrainAdjustment(plannerId) : updatedTerrainAdjustment);
    }

    public static List<MKWorkspaceVerticalStackSettings> defaultWalledKeepVerticalStackSettings(
            boolean uniqueNorthWestCornerTower,
            boolean uniqueNorthEastCornerTower,
            boolean uniqueSouthEastCornerTower,
            boolean uniqueSouthWestCornerTower) {
        ArrayList<MKWorkspaceVerticalStackSettings> settings = new ArrayList<>();
        settings.add(walledKeepCenterStackDefaults());
        if (!uniqueNorthWestCornerTower || !uniqueNorthEastCornerTower ||
                !uniqueSouthEastCornerTower || !uniqueSouthWestCornerTower) {
            settings.add(walledKeepCornerStackDefaults("keep.corner.shared"));
        }
        if (uniqueNorthWestCornerTower) {
            settings.add(walledKeepCornerStackDefaults("keep.corner.north_west"));
        }
        if (uniqueNorthEastCornerTower) {
            settings.add(walledKeepCornerStackDefaults("keep.corner.north_east"));
        }
        if (uniqueSouthEastCornerTower) {
            settings.add(walledKeepCornerStackDefaults("keep.corner.south_east"));
        }
        if (uniqueSouthWestCornerTower) {
            settings.add(walledKeepCornerStackDefaults("keep.corner.south_west"));
        }
        return List.copyOf(settings);
    }

    private static MKWorkspaceVerticalStackSettings walledKeepCenterStackDefaults() {
        return MKWorkspaceVerticalStackSettings.defaults("keep.center", 7)
                .withTopCapApproachEnabled(false)
                .withBasementEntryEnabled(false)
                .withBasementCapApproachEnabled(false);
    }

    private static MKWorkspaceVerticalStackSettings walledKeepCornerStackDefaults(String stackId) {
        return MKWorkspaceVerticalStackSettings.defaults(stackId, 7)
                .withMainFloors(0)
                .withBasementFloors(0)
                .withTopCapApproachEnabled(false)
                .withBasementEntryEnabled(false)
                .withBasementCapApproachEnabled(false);
    }

    private static List<MKWorkspaceVerticalStackSettings> normalizeVerticalStackSettings(
            ResourceLocation plannerId,
            boolean uniqueNorthWestCornerTower,
            boolean uniqueNorthEastCornerTower,
            boolean uniqueSouthEastCornerTower,
            boolean uniqueSouthWestCornerTower,
            List<MKWorkspaceVerticalStackSettings> currentSettings) {
        if (!WALLED_KEEP_PLANNER_ID.equals(plannerId)) {
            return List.copyOf(currentSettings == null ? List.of() : currentSettings);
        }
        ArrayList<MKWorkspaceVerticalStackSettings> normalized = new ArrayList<>();
        List<MKWorkspaceVerticalStackSettings> defaults = defaultWalledKeepVerticalStackSettings(uniqueNorthWestCornerTower,
                uniqueNorthEastCornerTower, uniqueSouthEastCornerTower, uniqueSouthWestCornerTower);
        for (MKWorkspaceVerticalStackSettings defaultSettings : defaults) {
            MKWorkspaceVerticalStackSettings settings = currentSettings == null ? null : currentSettings.stream()
                    .filter(existing -> existing.stackId().equals(defaultSettings.stackId()))
                    .findFirst()
                    .orElse(null);
            normalized.add(settings == null ? defaultSettings : settings);
        }
        return List.copyOf(normalized);
    }

    private static TerrainAdjustment defaultTerrainAdjustment(ResourceLocation plannerId) {
        return WALLED_KEEP_PLANNER_ID.equals(plannerId) ? DEFAULT_WALLED_KEEP_TERRAIN_ADJUSTMENT :
                DEFAULT_TOWER_TERRAIN_ADJUSTMENT;
    }
}
