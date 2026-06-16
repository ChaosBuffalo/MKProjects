package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public record MKWorkspaceTopologyProfile(
        ResourceLocation plannerId,
        List<MKWorkspacePlannerSettingsEntry> plannerSettings,
        TerrainAdjustment terrainAdjustment
) {
    private static final ResourceLocation DEFAULT_PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "tower");
    private static final Codec<TerrainAdjustment> TERRAIN_ADJUSTMENT_CODEC =
            StringRepresentable.fromEnum(TerrainAdjustment::values);

    public static final Codec<MKWorkspaceTopologyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("planner_id", DEFAULT_PLANNER_ID)
                    .forGetter(MKWorkspaceTopologyProfile::plannerId),
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

    public MKWorkspaceTopologyProfile(ResourceLocation plannerId,
                                      List<MKWorkspaceTopologyGroupSettings> topologyGroupSettings,
                                      List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
                                      List<MKWorkspaceFloorTopologySettings> floorTopologySettings,
                                      List<MKWorkspaceTopologyPathSettings> pathSettings,
                                      List<MKWorkspacePlannerSettingsEntry> plannerSettings,
                                      TerrainAdjustment terrainAdjustment) {
        this(plannerId, entriesFrom(plannerId, topologyGroupSettings, verticalStackSettings, floorTopologySettings,
                pathSettings, plannerSettings), terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile {
        if (plannerId == null) {
            plannerId = DEFAULT_PLANNER_ID;
        }
        plannerSettings = normalizePlannerSettings(plannerSettings);
        terrainAdjustment = terrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : terrainAdjustment;
    }

    private static List<MKWorkspacePlannerSettingsEntry> entriesFrom(
            ResourceLocation ownerPlannerId,
            List<MKWorkspaceTopologyGroupSettings> topologyGroupSettings,
            List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
            List<MKWorkspaceFloorTopologySettings> floorTopologySettings,
            List<MKWorkspaceTopologyPathSettings> pathSettings,
            List<MKWorkspacePlannerSettingsEntry> plannerSettings) {
        LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope = new LinkedHashMap<>();
        addEntries(byScope, plannerSettings);
        addTopologyGroupPaletteEntries(byScope, ownerPlannerId == null ? DEFAULT_PLANNER_ID : ownerPlannerId,
                topologyGroupSettings);

        List<MKWorkspaceVerticalStackSettings> normalizedStackSettings =
                List.copyOf(verticalStackSettings == null ? List.of() : verticalStackSettings);
        for (MKWorkspaceVerticalStackSettings settings : normalizedStackSettings) {
            putEntry(byScope, settings.plannerSettingsEntry());
        }

        List<MKWorkspaceFloorTopologySettings> normalizedFloorSettings =
                MKWorkspaceFloorTopologySettings.normalize(floorTopologySettings, normalizedStackSettings);
        for (MKWorkspaceFloorTopologySettings settings : normalizedFloorSettings) {
            putEntry(byScope, settings.plannerSettingsEntry());
        }

        List<MKWorkspaceTopologyPathSettings> normalizedPathSettings =
                MKWorkspaceTopologyPathSettings.normalize(pathSettings);
        for (MKWorkspaceTopologyPathSettings settings : normalizedPathSettings) {
            putEntry(byScope, settings.plannerSettingsEntry());
        }
        return List.copyOf(byScope.values());
    }

    private static void addEntries(LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope,
                                   List<MKWorkspacePlannerSettingsEntry> entries) {
        if (entries == null) {
            return;
        }
        for (MKWorkspacePlannerSettingsEntry entry : entries) {
            putEntry(byScope, entry);
        }
    }

    private static void addTopologyGroupPaletteEntries(
            LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope,
            ResourceLocation ownerPlannerId,
            List<MKWorkspaceTopologyGroupSettings> topologyGroupSettings) {
        for (MKWorkspaceTopologyGroupSettings settings : MKWorkspaceTopologyGroupSettings.normalize(topologyGroupSettings)) {
            MKWorkspacePlannerSettingsEntry current = byScope.get(settings.topologyGroupId());
            if (current == null) {
                putEntry(byScope, new MKWorkspacePlannerSettingsEntry(ownerPlannerId, settings.topologyGroupId(),
                        settings.paletteOverride(), new net.minecraft.nbt.CompoundTag()));
            } else {
                putEntry(byScope, current.withPaletteOverride(settings.paletteOverride()));
            }
        }
    }

    private static List<MKWorkspacePlannerSettingsEntry> normalizePlannerSettings(
            List<MKWorkspacePlannerSettingsEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope = new LinkedHashMap<>();
        addEntries(byScope, entries);
        return List.copyOf(byScope.values());
    }

    private static void putEntry(LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope,
                                 MKWorkspacePlannerSettingsEntry entry) {
        if (entry == null) {
            return;
        }
        if (entry.isEmpty()) {
            byScope.remove(entry.scopeId());
        } else {
            byScope.put(entry.scopeId(), entry);
        }
    }

    public Optional<MKWorkspacePlannerSettingsEntry> plannerSettingsEntry(ResourceLocation settingsPlannerId,
                                                                          String scopeId) {
        return plannerSettings.stream()
                .filter(entry -> entry.plannerId().equals(settingsPlannerId))
                .filter(entry -> entry.scopeId().equals(scopeId))
                .findFirst();
    }

    public Optional<MKWorkspacePlannerSettingsEntry> plannerSettingsEntry(String scopeId) {
        return plannerSettings.stream()
                .filter(entry -> entry.scopeId().equals(scopeId))
                .findFirst();
    }

    public MKWorkspaceTopologyProfile withPlannerSettingsEntry(MKWorkspacePlannerSettingsEntry updatedSettings) {
        LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope = new LinkedHashMap<>();
        addEntries(byScope, plannerSettings);
        putEntry(byScope, updatedSettings);
        return new MKWorkspaceTopologyProfile(plannerId, List.copyOf(byScope.values()), terrainAdjustment);
    }

    public List<MKWorkspaceTopologyGroupSettings> topologyGroupSettings() {
        return plannerSettings.stream()
                .filter(entry -> entry.paletteOverride().isPresent())
                .map(entry -> MKWorkspaceTopologyGroupSettings.palette(entry.scopeId(), entry.paletteOverride()))
                .toList();
    }

    public Optional<MKWorkspaceTopologyGroupSettings> topologyGroupSettings(String topologyGroupId) {
        return MKWorkspaceTopologyGroupSettings.find(topologyGroupSettings(), topologyGroupId);
    }

    public Optional<MKWorkspacePaletteOverride> topologyGroupPaletteOverride(String topologyGroupId) {
        return plannerSettingsEntry(topologyGroupId)
                .flatMap(MKWorkspacePlannerSettingsEntry::paletteOverride);
    }

    public MKWorkspaceTopologyProfile withTopologyGroupSettings(MKWorkspaceTopologyGroupSettings updatedSettings) {
        if (updatedSettings == null || updatedSettings.topologyGroupId().isBlank()) {
            return this;
        }
        MKWorkspacePlannerSettingsEntry current = plannerSettingsEntry(updatedSettings.topologyGroupId())
                .orElseGet(() -> new MKWorkspacePlannerSettingsEntry(plannerId, updatedSettings.topologyGroupId(),
                        new net.minecraft.nbt.CompoundTag()));
        return withPlannerSettingsEntry(current.withPaletteOverride(updatedSettings.paletteOverride()));
    }

    public MKWorkspaceTopologyProfile withTopologyGroupPaletteOverride(
            String topologyGroupId, Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return withTopologyGroupSettings(MKWorkspaceTopologyGroupSettings.palette(topologyGroupId, paletteOverride));
    }

    public List<MKWorkspaceVerticalStackSettings> verticalStackSettings() {
        return plannerSettings.stream()
                .filter(entry -> entry.plannerId().equals(MKWorkspaceVerticalStackSettings.PLANNER_ID))
                .map(MKWorkspaceVerticalStackSettings::fromPlannerSettingsEntry)
                .toList();
    }

    public Optional<MKWorkspaceVerticalStackSettings> verticalStackSettings(String stackId) {
        return plannerSettingsEntry(MKWorkspaceVerticalStackSettings.PLANNER_ID, stackId)
                .map(MKWorkspaceVerticalStackSettings::fromPlannerSettingsEntry);
    }

    public MKWorkspaceVerticalStackSettings verticalStackSettingsOrDefault(String stackId) {
        return verticalStackSettings(stackId)
                .orElseGet(() -> MKWorkspaceVerticalStackSettings.defaults(stackId, 7));
    }

    public MKWorkspaceTopologyProfile withVerticalStackSettings(MKWorkspaceVerticalStackSettings updatedSettings) {
        return withPlannerSettingsEntry(updatedSettings.plannerSettingsEntry());
    }

    public List<MKWorkspaceFloorTopologySettings> floorTopologySettings() {
        return MKWorkspaceFloorTopologySettings.normalize(plannerSettings.stream()
                .filter(entry -> entry.plannerId().equals(MKWorkspaceFloorTopologySettings.PLANNER_ID))
                .map(MKWorkspaceFloorTopologySettings::fromPlannerSettingsEntry)
                .toList(), verticalStackSettings());
    }

    public Optional<MKWorkspaceFloorTopologySettings> floorTopologySettings(String stackId, String floorRole) {
        return MKWorkspaceFloorTopologySettings.find(floorTopologySettings(), stackId, floorRole);
    }

    public MKWorkspaceFloorTopologySettings floorTopologySettingsOrDefault(String stackId, String floorRole) {
        return floorTopologySettings(stackId, floorRole)
                .orElseGet(() -> MKWorkspaceFloorTopologySettings.defaults(
                        verticalStackSettingsOrDefault(stackId), floorRole));
    }

    public MKWorkspaceTopologyProfile withFloorTopologySettings(MKWorkspaceFloorTopologySettings updatedSettings) {
        return withPlannerSettingsEntry(updatedSettings.plannerSettingsEntry());
    }

    public List<MKWorkspaceTopologyPathSettings> pathSettings() {
        return MKWorkspaceTopologyPathSettings.normalize(plannerSettings.stream()
                .filter(entry -> entry.plannerId().equals(MKWorkspaceTopologyPathSettings.PLANNER_ID))
                .map(MKWorkspaceTopologyPathSettings::fromPlannerSettingsEntry)
                .toList());
    }

    public Optional<MKWorkspaceTopologyPathSettings> pathSettings(String topologyGroupId) {
        return MKWorkspaceTopologyPathSettings.find(pathSettings(), topologyGroupId);
    }

    public MKWorkspaceTopologyPathSettings pathSettingsOrDefault(String topologyGroupId) {
        return pathSettings(topologyGroupId)
                .orElseGet(() -> MKWorkspaceTopologyPathSettings.defaultForTopologyGroup(topologyGroupId));
    }

    public MKWorkspaceTopologyProfile withPathSettings(MKWorkspaceTopologyPathSettings updatedSettings) {
        return withPlannerSettingsEntry(updatedSettings.plannerSettingsEntry());
    }

    public MKWorkspaceTopologyProfile withTerrainAdjustment(TerrainAdjustment updatedTerrainAdjustment) {
        return new MKWorkspaceTopologyProfile(plannerId, plannerSettings,
                updatedTerrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : updatedTerrainAdjustment);
    }
}
