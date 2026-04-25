package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;

import java.util.Map;
import java.util.Optional;

public record MKWorkspaceRuntimePieceInfo(
        boolean start,
        MKJigsawPieceRole role,
        int progressionDelta,
        int verticalLevelDelta,
        boolean allowOnMainPath,
        boolean allowOnBranchPath,
        boolean terminal,
        boolean bossOnly
) {
    public static final String START_TAG = "runtime_start";
    public static final String ROLE_TAG = "runtime_piece_role";
    public static final String PROGRESSION_DELTA_TAG = "runtime_progression_delta";
    public static final String VERTICAL_LEVEL_DELTA_TAG = "runtime_vertical_level_delta";
    public static final String ALLOW_ON_MAIN_PATH_TAG = "runtime_allow_on_main_path";
    public static final String ALLOW_ON_BRANCH_PATH_TAG = "runtime_allow_on_branch_path";
    public static final String TERMINAL_TAG = "runtime_terminal";
    public static final String BOSS_ONLY_TAG = "runtime_boss_only";

    public void applyToTags(Map<String, String> tags) {
        tags.put(START_TAG, Boolean.toString(start));
        tags.put(ROLE_TAG, role.getSerializedName());
        tags.put(PROGRESSION_DELTA_TAG, Integer.toString(progressionDelta));
        tags.put(VERTICAL_LEVEL_DELTA_TAG, Integer.toString(verticalLevelDelta));
        tags.put(ALLOW_ON_MAIN_PATH_TAG, Boolean.toString(allowOnMainPath));
        tags.put(ALLOW_ON_BRANCH_PATH_TAG, Boolean.toString(allowOnBranchPath));
        tags.put(TERMINAL_TAG, Boolean.toString(terminal));
        tags.put(BOSS_ONLY_TAG, Boolean.toString(bossOnly));
    }

    public static Optional<MKWorkspaceRuntimePieceInfo> fromTags(Map<String, String> tags) {
        String roleName = tags.get(ROLE_TAG);
        if (roleName == null || roleName.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new MKWorkspaceRuntimePieceInfo(
                Boolean.parseBoolean(tags.getOrDefault(START_TAG, "false")),
                parseRole(roleName),
                parseInt(tags, PROGRESSION_DELTA_TAG, 0),
                parseInt(tags, VERTICAL_LEVEL_DELTA_TAG, 0),
                parseBoolean(tags, ALLOW_ON_MAIN_PATH_TAG, true),
                parseBoolean(tags, ALLOW_ON_BRANCH_PATH_TAG, false),
                parseBoolean(tags, TERMINAL_TAG, false),
                parseBoolean(tags, BOSS_ONLY_TAG, false)
        ));
    }

    private static boolean parseBoolean(Map<String, String> tags, String key, boolean defaultValue) {
        String value = tags.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static int parseInt(Map<String, String> tags, String key, int defaultValue) {
        String value = tags.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static MKJigsawPieceRole parseRole(String serializedName) {
        for (MKJigsawPieceRole role : MKJigsawPieceRole.values()) {
            if (role.getSerializedName().equals(serializedName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown runtime piece role: " + serializedName);
    }
}
