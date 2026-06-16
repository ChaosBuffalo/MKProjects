package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.List;

public final class MKWorkspaceTopologyPaletteMerge {
    private MKWorkspaceTopologyPaletteMerge() {
    }

    public static MKWorkspaceTopologyProfile preserveMaterialSettings(MKWorkspaceTopologyProfile source,
                                                                      MKWorkspaceTopologyProfile materialSource) {
        List<MKWorkspaceVerticalStackSettings> stackSettings = source.verticalStackSettings().stream()
                .map(settings -> materialSource.verticalStackSettings(settings.stackId())
                        .map(requested -> settings.withPaletteOverride(requested.paletteOverrideOpt()))
                        .orElse(settings))
                .toList();
        List<MKWorkspaceFloorTopologySettings> floorSettings = source.floorTopologySettings().stream()
                .map(settings -> materialSource.floorTopologySettings(settings.stackId(), settings.floorRole())
                        .map(requested -> settings.withPaletteOverride(requested.paletteOverride()))
                        .orElse(settings))
                .toList();
        return new MKWorkspaceTopologyProfile(
                source.plannerId(),
                materialSource.plannerScopeSettings(),
                stackSettings,
                floorSettings,
                source.pathSettings(),
                source.plannerSettings(),
                source.terrainAdjustment()
        );
    }
}
