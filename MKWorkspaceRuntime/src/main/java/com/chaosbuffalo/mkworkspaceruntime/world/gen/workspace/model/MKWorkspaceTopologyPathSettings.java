package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKWorkspaceTopologyPathSettings(
        String topologyGroupId,
        int minMainPathPieces,
        int maxMainPathPieces,
        int maxBranchPiecesBeforeCap
) {
    public static final int DEFAULT_MIN_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP = 0;
    public static final int MAX_BRANCH_PIECES_BEFORE_CAP = 10;
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "topology_path");
    public static final List<String> DEFAULT_TOPOLOGY_GROUP_IDS = List.of(
            "entry",
            "main",
            "basement",
            "top_cap",
            "basement_cap"
    );
    public static final Codec<MKWorkspaceTopologyPathSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("topology_group_id").forGetter(MKWorkspaceTopologyPathSettings::topologyGroupId),
            Codec.INT.optionalFieldOf("min_main_path_pieces", DEFAULT_MIN_MAIN_PATH_PIECES)
                    .forGetter(MKWorkspaceTopologyPathSettings::minMainPathPieces),
            Codec.INT.optionalFieldOf("max_main_path_pieces", DEFAULT_MAX_MAIN_PATH_PIECES)
                    .forGetter(MKWorkspaceTopologyPathSettings::maxMainPathPieces),
            Codec.INT.optionalFieldOf("max_branch_pieces_before_cap", DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP)
                    .forGetter(MKWorkspaceTopologyPathSettings::maxBranchPiecesBeforeCap)
    ).apply(instance, MKWorkspaceTopologyPathSettings::new));

    public MKWorkspaceTopologyPathSettings {
        topologyGroupId = topologyGroupId == null || topologyGroupId.isBlank() ? "main" : topologyGroupId;
        minMainPathPieces = Math.max(0, minMainPathPieces);
        maxMainPathPieces = Math.max(minMainPathPieces, maxMainPathPieces);
        maxBranchPiecesBeforeCap = Math.max(0, Math.min(
                MAX_BRANCH_PIECES_BEFORE_CAP, maxBranchPiecesBeforeCap));
    }

    public static List<MKWorkspaceTopologyPathSettings> defaults() {
        ArrayList<MKWorkspaceTopologyPathSettings> defaults = new ArrayList<>();
        for (String topologyGroupId : DEFAULT_TOPOLOGY_GROUP_IDS) {
            defaults.add(defaultForTopologyGroup(topologyGroupId));
        }
        return List.copyOf(defaults);
    }

    public static MKWorkspaceTopologyPathSettings defaultForTopologyGroup(String topologyGroupId) {
        return new MKWorkspaceTopologyPathSettings(
                topologyGroupId,
                DEFAULT_MIN_MAIN_PATH_PIECES,
                DEFAULT_MAX_MAIN_PATH_PIECES,
                DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP
        );
    }

    public static List<MKWorkspaceTopologyPathSettings> normalize(List<MKWorkspaceTopologyPathSettings> settings) {
        Map<String, MKWorkspaceTopologyPathSettings> byTopologyGroup = new LinkedHashMap<>();
        for (MKWorkspaceTopologyPathSettings defaultSetting : defaults()) {
            byTopologyGroup.put(defaultSetting.topologyGroupId(), defaultSetting);
        }
        if (settings != null) {
            for (MKWorkspaceTopologyPathSettings setting : settings) {
                byTopologyGroup.put(setting.topologyGroupId(), setting);
            }
        }
        return List.copyOf(byTopologyGroup.values());
    }

    public static Optional<MKWorkspaceTopologyPathSettings> find(List<MKWorkspaceTopologyPathSettings> settings,
                                                                 String topologyGroupId) {
        return normalize(settings).stream()
                .filter(setting -> setting.topologyGroupId().equals(topologyGroupId))
                .findFirst();
    }

    public static MKWorkspaceTopologyPathSettings fromPlannerSettingsEntry(MKWorkspacePlannerSettingsEntry entry) {
        return MKWorkspaceCodecs.parseNbt(CODEC, entry.settings(), "workspace topology path settings");
    }

    public MKWorkspacePlannerSettingsEntry plannerSettingsEntry() {
        return new MKWorkspacePlannerSettingsEntry(
                PLANNER_ID,
                topologyGroupId,
                MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace topology path settings"));
    }

    public MKWorkspaceTopologyPathSettings withMinMainPathPieces(int value) {
        return new MKWorkspaceTopologyPathSettings(topologyGroupId, value, Math.max(value, maxMainPathPieces),
                maxBranchPiecesBeforeCap);
    }

    public MKWorkspaceTopologyPathSettings withMaxMainPathPieces(int value) {
        return new MKWorkspaceTopologyPathSettings(topologyGroupId, Math.min(minMainPathPieces, value), value,
                maxBranchPiecesBeforeCap);
    }

    public MKWorkspaceTopologyPathSettings withMaxBranchPiecesBeforeCap(int value) {
        return new MKWorkspaceTopologyPathSettings(topologyGroupId, minMainPathPieces, maxMainPathPieces, value);
    }
}
