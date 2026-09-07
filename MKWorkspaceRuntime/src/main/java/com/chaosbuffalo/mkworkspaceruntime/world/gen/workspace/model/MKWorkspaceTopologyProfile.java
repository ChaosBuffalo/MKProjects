package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorTopologySettings;
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
                                      List<MKFloorTopologySettings> floorTopologySettings,
                                      List<MKWorkspaceTopologyPathSettings> pathSettings,
                                      List<MKWorkspacePlannerSettingsEntry> plannerSettings,
                                      TerrainAdjustment terrainAdjustment) {
        this(plannerId, List.of(), verticalStackSettings, floorTopologySettings, pathSettings, plannerSettings,
                terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile(ResourceLocation plannerId,
                                      List<MKWorkspacePlannerScopeSettings> plannerScopeSettings,
                                      List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
                                      List<MKFloorTopologySettings> floorTopologySettings,
                                      List<MKWorkspaceTopologyPathSettings> pathSettings,
                                      List<MKWorkspacePlannerSettingsEntry> plannerSettings,
                                      TerrainAdjustment terrainAdjustment) {
        this(plannerId, entriesFrom(plannerId, plannerScopeSettings, verticalStackSettings, floorTopologySettings,
                pathSettings, plannerSettings), terrainAdjustment);
    }

    public MKWorkspaceTopologyProfile {
        if (plannerId == null) {
            plannerId = DEFAULT_PLANNER_ID;
        }
        plannerSettings = normalizePlannerSettings(plannerSettings);
        terrainAdjustment = terrainAdjustment == null ? TerrainAdjustment.BEARD_THIN : terrainAdjustment;
    }

    public static MKWorkspaceTopologyProfile defaults() {
        return new MKWorkspaceTopologyProfile(DEFAULT_PLANNER_ID, List.of(), TerrainAdjustment.BEARD_THIN);
    }

    private static List<MKWorkspacePlannerSettingsEntry> entriesFrom(
            ResourceLocation ownerPlannerId,
            List<MKWorkspacePlannerScopeSettings> plannerScopeSettings,
            List<MKWorkspaceVerticalStackSettings> verticalStackSettings,
            List<MKFloorTopologySettings> floorTopologySettings,
            List<MKWorkspaceTopologyPathSettings> pathSettings,
            List<MKWorkspacePlannerSettingsEntry> plannerSettings) {
        LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope = new LinkedHashMap<>();
        addEntries(byScope, plannerSettings);
        addPlannerScopePaletteEntries(byScope, ownerPlannerId == null ? DEFAULT_PLANNER_ID : ownerPlannerId,
                plannerScopeSettings);

        List<MKWorkspaceVerticalStackSettings> normalizedStackSettings =
                List.copyOf(verticalStackSettings == null ? List.of() : verticalStackSettings);
        for (MKWorkspaceVerticalStackSettings settings : normalizedStackSettings) {
            putEntry(byScope, settings.plannerSettingsEntry());
        }

        List<MKFloorTopologySettings> normalizedFloorSettings =
                MKFloorTopologySettings.normalize(floorTopologySettings, normalizedStackSettings);
        for (MKFloorTopologySettings settings : normalizedFloorSettings) {
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

    private static void addPlannerScopePaletteEntries(
            LinkedHashMap<String, MKWorkspacePlannerSettingsEntry> byScope,
            ResourceLocation ownerPlannerId,
            List<MKWorkspacePlannerScopeSettings> plannerScopeSettings) {
        for (MKWorkspacePlannerScopeSettings settings : MKWorkspacePlannerScopeSettings.normalize(plannerScopeSettings)) {
            MKWorkspacePlannerSettingsEntry current = byScope.get(settings.scopeId());
            if (current == null) {
                putEntry(byScope, new MKWorkspacePlannerSettingsEntry(ownerPlannerId, settings.scopeId(),
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

    public MKWorkspaceTopologyProfile withoutPlannerSettingsEntry(ResourceLocation settingsPlannerId, String scopeId) {
        return withPlannerSettingsEntry(new MKWorkspacePlannerSettingsEntry(settingsPlannerId, scopeId,
                new net.minecraft.nbt.CompoundTag()));
    }

    public List<MKWorkspacePlannerScopeSettings> plannerScopeSettings() {
        return plannerSettings.stream()
                .filter(entry -> entry.paletteOverride().isPresent())
                .map(entry -> MKWorkspacePlannerScopeSettings.palette(entry.scopeId(), entry.paletteOverride()))
                .toList();
    }

    public Optional<MKWorkspacePlannerScopeSettings> plannerScopeSettings(String scopeId) {
        return MKWorkspacePlannerScopeSettings.find(plannerScopeSettings(), scopeId);
    }

    public Optional<MKWorkspacePaletteOverride> plannerScopePaletteOverride(String scopeId) {
        return plannerSettingsEntry(scopeId)
                .flatMap(MKWorkspacePlannerSettingsEntry::paletteOverride);
    }

    public MKWorkspaceTopologyProfile withPlannerScopeSettings(MKWorkspacePlannerScopeSettings updatedSettings) {
        if (updatedSettings == null || updatedSettings.scopeId().isBlank()) {
            return this;
        }
        MKWorkspacePlannerSettingsEntry current = plannerSettingsEntry(updatedSettings.scopeId())
                .orElseGet(() -> new MKWorkspacePlannerSettingsEntry(plannerId, updatedSettings.scopeId(),
                        new net.minecraft.nbt.CompoundTag()));
        return withPlannerSettingsEntry(current.withPaletteOverride(updatedSettings.paletteOverride()));
    }

    public MKWorkspaceTopologyProfile withPlannerScopePaletteOverride(
            String scopeId, Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return withPlannerScopeSettings(MKWorkspacePlannerScopeSettings.palette(scopeId, paletteOverride));
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

    public List<MKFloorTopologySettings> floorTopologySettings() {
        return MKFloorTopologySettings.normalize(plannerSettings.stream()
                .filter(entry -> entry.plannerId().equals(MKFloorTopologySettings.PLANNER_ID))
                .map(MKFloorTopologySettings::fromPlannerSettingsEntry)
                .toList(), verticalStackSettings());
    }

    public Optional<MKFloorTopologySettings> floorTopologySettings(String stackId, String floorRole) {
        return MKFloorTopologySettings.find(floorTopologySettings(), stackId, floorRole);
    }

    public MKFloorTopologySettings floorTopologySettingsOrDefault(String stackId, String floorRole) {
        return floorTopologySettings(stackId, floorRole)
                .orElseGet(() -> MKFloorTopologySettings.defaults(
                        verticalStackSettingsOrDefault(stackId), floorRole));
    }

    public MKWorkspaceTopologyProfile withFloorTopologySettings(MKFloorTopologySettings updatedSettings) {
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
