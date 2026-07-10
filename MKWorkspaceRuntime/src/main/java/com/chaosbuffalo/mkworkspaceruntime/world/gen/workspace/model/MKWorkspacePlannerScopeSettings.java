package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKWorkspacePlannerScopeSettings(
        String scopeId,
        Optional<MKWorkspacePaletteOverride> paletteOverride
) {
    public static final Codec<MKWorkspacePlannerScopeSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("scope_id").forGetter(MKWorkspacePlannerScopeSettings::scopeId),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                    .forGetter(MKWorkspacePlannerScopeSettings::paletteOverride)
    ).apply(instance, MKWorkspacePlannerScopeSettings::new));

    public MKWorkspacePlannerScopeSettings {
        scopeId = scopeId == null ? "" : scopeId.trim();
        paletteOverride = paletteOverride == null ? Optional.empty() :
                paletteOverride.filter(override -> !override.isEmpty());
    }

    public static MKWorkspacePlannerScopeSettings palette(String scopeId,
                                                          Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKWorkspacePlannerScopeSettings(scopeId, paletteOverride);
    }

    public static List<MKWorkspacePlannerScopeSettings> normalize(
            List<MKWorkspacePlannerScopeSettings> settings) {
        Map<String, MKWorkspacePlannerScopeSettings> byScope = new LinkedHashMap<>();
        if (settings != null) {
            for (MKWorkspacePlannerScopeSettings setting : settings) {
                if (!setting.scopeId().isBlank() && setting.paletteOverride().isPresent()) {
                    byScope.put(setting.scopeId(), setting);
                }
            }
        }
        return List.copyOf(byScope.values());
    }

    public static Optional<MKWorkspacePlannerScopeSettings> find(
            List<MKWorkspacePlannerScopeSettings> settings, String scopeId) {
        if (scopeId == null || scopeId.isBlank()) {
            return Optional.empty();
        }
        return settings.stream()
                .filter(setting -> setting.scopeId().equals(scopeId))
                .findFirst();
    }

    public static List<String> hierarchy(String scopeId) {
        if (scopeId == null || scopeId.isBlank()) {
            return List.of();
        }
        String[] parts = scopeId.split("\\.");
        ArrayList<String> hierarchy = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                return List.of(scopeId);
            }
            if (current.length() > 0) {
                current.append('.');
            }
            current.append(part);
            hierarchy.add(current.toString());
        }
        return List.copyOf(hierarchy);
    }
}
