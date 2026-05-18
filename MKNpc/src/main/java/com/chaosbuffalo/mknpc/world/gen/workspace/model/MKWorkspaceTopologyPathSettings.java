package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKWorkspaceTopologyPathSettings(
        String categoryId,
        int minMainPathPieces,
        int maxMainPathPieces,
        int maxBranchPiecesBeforeCap
) {
    public static final int DEFAULT_MIN_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_MAIN_PATH_PIECES = 2;
    public static final int DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP = 10;
    public static final int MAX_BRANCH_PIECES_BEFORE_CAP = DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP;
    public static final Codec<MKWorkspaceTopologyPathSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("category_id").forGetter(MKWorkspaceTopologyPathSettings::categoryId),
            Codec.INT.optionalFieldOf("min_main_path_pieces", DEFAULT_MIN_MAIN_PATH_PIECES)
                    .forGetter(MKWorkspaceTopologyPathSettings::minMainPathPieces),
            Codec.INT.optionalFieldOf("max_main_path_pieces", DEFAULT_MAX_MAIN_PATH_PIECES)
                    .forGetter(MKWorkspaceTopologyPathSettings::maxMainPathPieces),
            Codec.INT.optionalFieldOf("max_branch_pieces_before_cap", DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP)
                    .forGetter(MKWorkspaceTopologyPathSettings::maxBranchPiecesBeforeCap)
    ).apply(instance, MKWorkspaceTopologyPathSettings::new));

    public MKWorkspaceTopologyPathSettings {
        categoryId = categoryId == null || categoryId.isBlank() ? "main" : categoryId;
        minMainPathPieces = Math.max(0, minMainPathPieces);
        maxMainPathPieces = Math.max(minMainPathPieces, maxMainPathPieces);
        maxBranchPiecesBeforeCap = Math.max(0, Math.min(
                DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP, maxBranchPiecesBeforeCap));
    }

    public static List<MKWorkspaceTopologyPathSettings> defaults() {
        ArrayList<MKWorkspaceTopologyPathSettings> defaults = new ArrayList<>();
        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            defaults.add(defaultForCategory(category.getSerializedName()));
        }
        return List.copyOf(defaults);
    }

    public static MKWorkspaceTopologyPathSettings defaultForCategory(String categoryId) {
        return new MKWorkspaceTopologyPathSettings(
                categoryId,
                DEFAULT_MIN_MAIN_PATH_PIECES,
                DEFAULT_MAX_MAIN_PATH_PIECES,
                DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP
        );
    }

    public static List<MKWorkspaceTopologyPathSettings> normalize(List<MKWorkspaceTopologyPathSettings> settings) {
        Map<String, MKWorkspaceTopologyPathSettings> byCategory = new LinkedHashMap<>();
        for (MKWorkspaceTopologyPathSettings defaultSetting : defaults()) {
            byCategory.put(defaultSetting.categoryId(), defaultSetting);
        }
        if (settings != null) {
            for (MKWorkspaceTopologyPathSettings setting : settings) {
                byCategory.put(setting.categoryId(), setting);
            }
        }
        return List.copyOf(byCategory.values());
    }

    public static Optional<MKWorkspaceTopologyPathSettings> find(List<MKWorkspaceTopologyPathSettings> settings,
                                                                 String categoryId) {
        return normalize(settings).stream()
                .filter(setting -> setting.categoryId().equals(categoryId))
                .findFirst();
    }

    public MKWorkspaceTopologyPathSettings withMinMainPathPieces(int value) {
        return new MKWorkspaceTopologyPathSettings(categoryId, value, Math.max(value, maxMainPathPieces),
                maxBranchPiecesBeforeCap);
    }

    public MKWorkspaceTopologyPathSettings withMaxMainPathPieces(int value) {
        return new MKWorkspaceTopologyPathSettings(categoryId, Math.min(minMainPathPieces, value), value,
                maxBranchPiecesBeforeCap);
    }

    public MKWorkspaceTopologyPathSettings withMaxBranchPiecesBeforeCap(int value) {
        return new MKWorkspaceTopologyPathSettings(categoryId, minMainPathPieces, maxMainPathPieces, value);
    }
}
