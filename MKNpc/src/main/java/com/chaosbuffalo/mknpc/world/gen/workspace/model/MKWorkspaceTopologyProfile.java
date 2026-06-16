package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public record MKWorkspaceTopologyProfile(
        ResourceLocation plannerId,
        List<MKWorkspaceTopologyGroupSettings> topologyGroupSettings,
        List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
        List<MKWorkspaceFloorTopologySettings> floorTopologySettings,
        List<MKWorkspaceTopologyPathSettings> pathSettings,
        List<MKWorkspacePlannerSettingsEntry> plannerSettings,
        TerrainAdjustment terrainAdjustment
) {
    private static final ResourceLocation DEFAULT_PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    private static final Codec<TerrainAdjustment> TERRAIN_ADJUSTMENT_CODEC =
            StringRepresentable.fromEnum(TerrainAdjustment::values);

    public static final Codec<MKWorkspaceTopologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("planner_id", DEFAULT_PLANNER_ID)
                    .forGetter(MKWorkspaceTopologyProfile::plannerId),
            MKWorkspaceTopologyGroupSettings.CODEC.listOf().optionalFieldOf("topology_group_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::topologyGroupSettings),
            MKWorkspaceVerticalStackSettings.CODEC.listOf().optionalFieldOf("vertical_stack_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::verticalStackSettings),
            MKWorkspaceFloorTopologySettings.CODEC.listOf().optionalFieldOf("floor_topology_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::floorTopologySettings),
            MKWorkspaceTopologyPathSettings.CODEC.listOf().optionalFieldOf("path_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::pathSettings),
            MKWorkspacePlannerSettingsEntry.CODEC.listOf().optionalFieldOf("planner_settings", List.of())
                    .forGetter(MKWorkspaceTopologyProfile::plannerSettings),
            TERRAIN_ADJUSTMENT_CODEC.optionalFieldOf("terrain_adjustment", TerrainAdjustment.BEARD_THIN)
                    .forGetter(MKWorkspaceTopologyProfile::terrainAdjustment)
    ).apply(instance, MKWorkspaceTopologyProfile::new));

    public MKWorkspaceTopologyProfile(ResourceLocation plannerId,
                                      List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
                                      List<MKWorkspaceFloorTopologySettings> floorTopologySettings,
                                      List<MKWorkspaceTopologyPathSettings> pathSettings,
                                      List<MKWorkspacePlannerSettingsEntry> plannerSettings,
                                      TerrainAdjustment terrainAdjustment) {
        this(plannerId, List.of(), verticalStackSettings, floorTopologySettings, pathSettings, plannerSettings,
                terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile {
        if (plannerId == null) {
            plannerId = DEFAULT_PLANNER_ID;
        }
        verticalStackSettings = List.copyOf(verticalStackSettings == null ? List.of() : verticalStackSettings);
        topologyGroupSettings = MKWorkspaceTopologyGroupSettings.normalize(topologyGroupSettings);
        floorTopologySettings = MKWorkspaceFloorTopologySettings.normalize(floorTopologySettings, verticalStackSettings);
        pathSettings = MKWorkspaceTopologyPathSettings.normalize(pathSettings);
        plannerSettings = normalizePlannerSettings(plannerSettings);
        terrainAdjustment = terrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : terrainAdjustment;
    }

    private static List<MKWorkspacePlannerSettingsEntry> normalizePlannerSettings(
            List<MKWorkspacePlannerSettingsEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<ResourceLocation, MKWorkspacePlannerSettingsEntry> byPlanner = new LinkedHashMap<>();
        for (MKWorkspacePlannerSettingsEntry entry : entries) {
            if (entry != null) {
                byPlanner.put(entry.plannerId(), entry);
            }
        }
        return List.copyOf(byPlanner.values());
    }

    public Optional<MKWorkspacePlannerSettingsEntry> plannerSettingsEntry(ResourceLocation settingsPlannerId) {
        return plannerSettings.stream()
                .filter(entry -> entry.plannerId().equals(settingsPlannerId))
                .findFirst();
    }

    public MKWorkspaceTopologyProfile withPlannerSettingsEntry(MKWorkspacePlannerSettingsEntry updatedSettings) {
        ArrayList<MKWorkspacePlannerSettingsEntry> updated = new ArrayList<>();
        boolean replaced = false;
        for (MKWorkspacePlannerSettingsEntry settings : plannerSettings) {
            if (settings.plannerId().equals(updatedSettings.plannerId())) {
                updated.add(updatedSettings);
                replaced = true;
            } else {
                updated.add(settings);
            }
        }
        if (!replaced) {
            updated.add(updatedSettings);
        }
        return new MKWorkspaceTopologyProfile(plannerId, topologyGroupSettings, verticalStackSettings,
                floorTopologySettings, pathSettings, updated, terrainAdjustment);
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
        return new MKWorkspaceTopologyProfile(plannerId, updated, verticalStackSettings, floorTopologySettings,
                pathSettings, plannerSettings, terrainAdjustment);
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
        return new MKWorkspaceTopologyProfile(plannerId, topologyGroupSettings, updated, floorTopologySettings,
                pathSettings, plannerSettings, terrainAdjustment);
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
        return new MKWorkspaceTopologyProfile(plannerId, topologyGroupSettings, verticalStackSettings, updated,
                pathSettings, plannerSettings, terrainAdjustment);
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
        return new MKWorkspaceTopologyProfile(plannerId, topologyGroupSettings, verticalStackSettings,
                floorTopologySettings, updated, plannerSettings, terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile withTerrainAdjustment(TerrainAdjustment updatedTerrainAdjustment) {
        return new MKWorkspaceTopologyProfile(plannerId, topologyGroupSettings, verticalStackSettings,
                floorTopologySettings, pathSettings, plannerSettings,
                updatedTerrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : updatedTerrainAdjustment);
    }
}
