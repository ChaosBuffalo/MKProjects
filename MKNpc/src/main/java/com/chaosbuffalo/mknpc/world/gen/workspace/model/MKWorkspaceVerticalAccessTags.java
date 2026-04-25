package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.Map;

public final class MKWorkspaceVerticalAccessTags {
    public static final String ENABLED_TAG = "supports_vertical_access";
    public static final String DIRECTION_TAG = "vertical_access_direction";
    public static final String PLACEMENT_TAG = "vertical_access_placement";
    public static final String TOP_CAP_TAG = "vertical_access_top_cap";
    public static final String BOTTOM_CAP_TAG = "vertical_access_bottom_cap";

    private MKWorkspaceVerticalAccessTags() {
    }

    public static boolean supportsVerticalAccess(Map<String, String> tags) {
        return readBoolean(tags, ENABLED_TAG, false);
    }

    public static boolean isTopCap(Map<String, String> tags) {
        return readBoolean(tags, TOP_CAP_TAG, false);
    }

    public static boolean isBottomCap(Map<String, String> tags) {
        return readBoolean(tags, BOTTOM_CAP_TAG, false);
    }

    public static String placement(Map<String, String> tags, String defaultPlacement) {
        return readString(tags, PLACEMENT_TAG, defaultPlacement);
    }

    public static String direction(Map<String, String> tags, String defaultDirection) {
        return readString(tags, DIRECTION_TAG, defaultDirection);
    }

    private static boolean readBoolean(Map<String, String> tags, String key, boolean defaultValue) {
        String value = tags.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static String readString(Map<String, String> tags, String key, String defaultValue) {
        String value = tags.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
