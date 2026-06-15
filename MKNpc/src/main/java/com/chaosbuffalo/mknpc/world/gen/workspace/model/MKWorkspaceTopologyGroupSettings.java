package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKWorkspaceTopologyGroupSettings(
        String topologyGroupId,
        Optional<MKWorkspacePaletteOverride> paletteOverride
) {
    public static final Codec<MKWorkspaceTopologyGroupSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("topology_group_id").forGetter(MKWorkspaceTopologyGroupSettings::topologyGroupId),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                    .forGetter(MKWorkspaceTopologyGroupSettings::paletteOverride)
    ).apply(instance, MKWorkspaceTopologyGroupSettings::new));

    public MKWorkspaceTopologyGroupSettings {
        topologyGroupId = topologyGroupId == null ? "" : topologyGroupId.trim();
        paletteOverride = paletteOverride == null ? Optional.empty() :
                paletteOverride.filter(override -> !override.isEmpty());
    }

    public static MKWorkspaceTopologyGroupSettings palette(String topologyGroupId,
                                                           Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKWorkspaceTopologyGroupSettings(topologyGroupId, paletteOverride);
    }

    public static List<MKWorkspaceTopologyGroupSettings> normalize(
            List<MKWorkspaceTopologyGroupSettings> settings) {
        Map<String, MKWorkspaceTopologyGroupSettings> byGroup = new LinkedHashMap<>();
        if (settings != null) {
            for (MKWorkspaceTopologyGroupSettings setting : settings) {
                if (!setting.topologyGroupId().isBlank() && setting.paletteOverride().isPresent()) {
                    byGroup.put(setting.topologyGroupId(), setting);
                }
            }
        }
        return List.copyOf(byGroup.values());
    }

    public static Optional<MKWorkspaceTopologyGroupSettings> find(
            List<MKWorkspaceTopologyGroupSettings> settings, String topologyGroupId) {
        if (topologyGroupId == null || topologyGroupId.isBlank()) {
            return Optional.empty();
        }
        return settings.stream()
                .filter(setting -> setting.topologyGroupId().equals(topologyGroupId))
                .findFirst();
    }

    public static List<String> hierarchy(String topologyGroupId) {
        if (topologyGroupId == null || topologyGroupId.isBlank()) {
            return List.of();
        }
        String[] parts = topologyGroupId.split("\\.");
        ArrayList<String> hierarchy = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                return List.of(topologyGroupId);
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
