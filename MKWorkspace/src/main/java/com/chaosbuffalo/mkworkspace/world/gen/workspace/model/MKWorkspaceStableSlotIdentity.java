package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import java.util.Map;

public final class MKWorkspaceStableSlotIdentity {
    public static final String TEMPLATE_KIND_TAG = "workspace_template_kind";
    public static final String TEMPLATE_SLOT_ID_TAG = "workspace_template_slot_id";

    private MKWorkspaceStableSlotIdentity() {
    }

    public static void apply(Map<String, String> tags, String templateKind, String slotId) {
        if (templateKind == null || templateKind.isBlank()) {
            throw new IllegalArgumentException("template kind cannot be blank");
        }
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("template slot id cannot be blank");
        }
        tags.put(TEMPLATE_KIND_TAG, templateKind);
        tags.put(TEMPLATE_SLOT_ID_TAG, slotId);
    }

    public static boolean hasStableIdentity(Map<String, String> tags) {
        return !templateKind(tags).isBlank() && !slotId(tags).isBlank();
    }

    public static String templateKind(Map<String, String> tags) {
        return tags.getOrDefault(TEMPLATE_KIND_TAG, "");
    }

    public static String slotId(Map<String, String> tags) {
        return tags.getOrDefault(TEMPLATE_SLOT_ID_TAG, "");
    }

    public static String key(Map<String, String> tags) {
        if (!hasStableIdentity(tags)) {
            return "";
        }
        return templateKind(tags) + ":" + slotId(tags);
    }
}
