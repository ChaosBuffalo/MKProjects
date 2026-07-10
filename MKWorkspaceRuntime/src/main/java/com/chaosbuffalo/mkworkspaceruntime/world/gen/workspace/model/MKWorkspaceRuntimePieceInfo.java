package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;

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
        boolean topCapOnly,
        String topologyGroup,
        boolean mainPathEnding,
        boolean branchCap
) {
    public static final String START_TAG = "runtime_start";
    public static final String ROLE_TAG = "runtime_piece_role";
    public static final String PROGRESSION_DELTA_TAG = "runtime_progression_delta";
    public static final String VERTICAL_LEVEL_DELTA_TAG = "runtime_vertical_level_delta";
    public static final String ALLOW_ON_MAIN_PATH_TAG = "runtime_allow_on_main_path";
    public static final String ALLOW_ON_BRANCH_PATH_TAG = "runtime_allow_on_branch_path";
    public static final String TERMINAL_TAG = "runtime_terminal";
    public static final String TOP_CAP_ONLY_TAG = "runtime_top_cap_only";
    public static final String TOPOLOGY_GROUP_TAG = "runtime_topology_group";
    public static final String MAIN_PATH_ENDING_TAG = "runtime_main_path_ending";
    public static final String BRANCH_CAP_TAG = "runtime_branch_cap";

    public MKWorkspaceRuntimePieceInfo(boolean start, MKJigsawPieceRole role,
                                       int progressionDelta, int verticalLevelDelta,
                                       boolean allowOnMainPath, boolean allowOnBranchPath,
                                       boolean terminal, boolean topCapOnly) {
        this(start, role, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, "", false, false);
    }

    public MKWorkspaceRuntimePieceInfo(boolean start, MKJigsawPieceRole role,
                                       int progressionDelta, int verticalLevelDelta,
                                       boolean allowOnMainPath, boolean allowOnBranchPath,
                                       boolean terminal, boolean topCapOnly, String topologyGroup,
                                       boolean mainPathEnding) {
        this(start, role, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath, terminal,
                topCapOnly, topologyGroup, mainPathEnding, false);
    }

    public void applyToTags(Map<String, String> tags) {
        tags.put(START_TAG, Boolean.toString(start));
        tags.put(ROLE_TAG, role.getSerializedName());
        tags.put(PROGRESSION_DELTA_TAG, Integer.toString(progressionDelta));
        tags.put(VERTICAL_LEVEL_DELTA_TAG, Integer.toString(verticalLevelDelta));
        tags.put(ALLOW_ON_MAIN_PATH_TAG, Boolean.toString(allowOnMainPath));
        tags.put(ALLOW_ON_BRANCH_PATH_TAG, Boolean.toString(allowOnBranchPath));
        tags.put(TERMINAL_TAG, Boolean.toString(terminal));
        tags.put(TOP_CAP_ONLY_TAG, Boolean.toString(topCapOnly));
        tags.put(TOPOLOGY_GROUP_TAG, topologyGroup);
        tags.put(MAIN_PATH_ENDING_TAG, Boolean.toString(mainPathEnding));
        tags.put(BRANCH_CAP_TAG, Boolean.toString(branchCap));
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
                parseBoolean(tags, TOP_CAP_ONLY_TAG, false),
                tags.getOrDefault(TOPOLOGY_GROUP_TAG, ""),
                parseBoolean(tags, MAIN_PATH_ENDING_TAG, false),
                parseBoolean(tags, BRANCH_CAP_TAG, false)
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
        return MKJigsawPieceRole.fromSerializedName(serializedName);
    }
}

