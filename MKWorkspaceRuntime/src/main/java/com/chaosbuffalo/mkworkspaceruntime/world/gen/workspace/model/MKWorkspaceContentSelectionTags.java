package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorMaskPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;

import java.util.LinkedHashMap;
import java.util.Map;

/** Runtime-safe tag vocabulary for the slot -> family -> variant content hierarchy. */
public final class MKWorkspaceContentSelectionTags {
    public static final String TEMPLATE_PURPOSE = "workspace_template_purpose";
    public static final String TOPOLOGY_SLOT_ID = "workspace_content_slot_id";
    public static final String FAMILY_ID = "workspace_content_family_id";
    public static final String FAMILY_WEIGHT = "workspace_family_weight";
    public static final String FAMILY_ENABLED = "workspace_family_enabled";
    public static final String VARIANT_ID = "workspace_content_variant_id";
    public static final String VARIANT_WEIGHT = "workspace_variant_weight";
    public static final String VARIANT_ENABLED = "workspace_variant_enabled";

    private static final String LEGACY_BASE_NAME = "workspace_base_name";
    private static final String LEGACY_PIECE_KIND = "workspace_piece_kind";
    private static final String LEGACY_ROOM_FAMILY_ID = "workspace_family_id";
    private static final String LEGACY_LINEAR_RUN_FAMILY_ID = "workspace_linear_run_family_id";
    private static final String LEGACY_TOPOLOGY_SLOT_ID = "workspace_topology_slot_id";
    private static final java.util.List<String> EXPLICIT_KEYS = java.util.List.of(
            TEMPLATE_PURPOSE, TOPOLOGY_SLOT_ID, FAMILY_ID, FAMILY_WEIGHT, FAMILY_ENABLED,
            VARIANT_ID, VARIANT_WEIGHT, VARIANT_ENABLED);

    private MKWorkspaceContentSelectionTags() {
    }

    public static MKWorkspaceTemplatePurpose purpose(MKWorkspacePieceDefinition piece) {
        return purpose(piece.tags(), piece.variantIndex());
    }

    public static MKWorkspaceTemplatePurpose purpose(Map<String, String> tags, int variantIndex) {
        MKWorkspaceTemplatePurpose explicit = MKWorkspaceTemplatePurpose
                .fromSerializedName(tags.get(TEMPLATE_PURPOSE))
                .orElse(null);
        if (explicit != null) {
            return explicit;
        }
        if (variantIndex > 0 || "instance".equals(tags.get(LEGACY_PIECE_KIND))) {
            return MKWorkspaceTemplatePurpose.FAMILY_VARIANT;
        }
        if (tags.containsKey(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID) &&
                "template".equals(tags.getOrDefault(LEGACY_PIECE_KIND, "instance"))) {
            return MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD;
        }
        return MKWorkspaceTemplatePurpose.FAMILY_CANONICAL;
    }

    public static String familyId(MKWorkspacePieceDefinition piece) {
        return familyId(piece.pieceName(), piece.tags());
    }

    public static String familyId(String pieceName, Map<String, String> tags) {
        return firstNonBlank(
                tags.get(FAMILY_ID),
                tags.get(LEGACY_ROOM_FAMILY_ID),
                tags.get(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID),
                tags.get(LEGACY_BASE_NAME),
                tags.get(LEGACY_LINEAR_RUN_FAMILY_ID),
                pieceName
        );
    }

    public static String topologySlotId(MKWorkspacePieceDefinition piece) {
        return topologySlotId(piece.roleId(), piece.tags());
    }

    public static String topologySlotId(String roleId, Map<String, String> tags) {
        return firstNonBlank(
                tags.get(TOPOLOGY_SLOT_ID),
                tags.get(LEGACY_TOPOLOGY_SLOT_ID),
                tags.get(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID),
                roleId
        );
    }

    public static String variantId(MKWorkspacePieceDefinition piece) {
        return variantId(piece.pieceName(), piece.tags());
    }

    public static String variantId(String pieceName, Map<String, String> tags) {
        return firstNonBlank(tags.get(VARIANT_ID), pieceName);
    }

    public static int familyWeight(Map<String, String> tags) {
        return positiveInt(tags.get(FAMILY_WEIGHT), 1);
    }

    public static boolean familyEnabled(Map<String, String> tags) {
        return booleanValue(tags.get(FAMILY_ENABLED), true);
    }

    public static int variantWeight(Map<String, String> tags) {
        return positiveInt(tags.get(VARIANT_WEIGHT),
                positiveInt(tags.get(MKFloorMaskPools.FLOOR_MASK_WEIGHT_TAG), 1));
    }

    public static boolean variantEnabled(Map<String, String> tags) {
        return booleanValue(tags.get(VARIANT_ENABLED), true);
    }

    public static Map<String, String> applyFamily(Map<String, String> tags, String topologySlotId,
                                                   String familyId, int familyWeight, boolean enabled) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(tags);
        result.put(TOPOLOGY_SLOT_ID, topologySlotId);
        result.put(FAMILY_ID, familyId);
        result.put(FAMILY_WEIGHT, Integer.toString(Math.max(1, familyWeight)));
        result.put(FAMILY_ENABLED, Boolean.toString(enabled));
        return result;
    }

    public static Map<String, String> applyTemplate(Map<String, String> tags,
                                                     MKWorkspaceTemplatePurpose purpose,
                                                     String variantId, int variantWeight, boolean enabled) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(tags);
        result.put(TEMPLATE_PURPOSE, purpose.serializedName());
        if (!variantId.isBlank()) {
            result.put(VARIANT_ID, variantId);
        } else {
            result.remove(VARIANT_ID);
        }
        result.put(VARIANT_WEIGHT, Integer.toString(Math.max(1, variantWeight)));
        result.put(VARIANT_ENABLED, Boolean.toString(enabled));
        return result;
    }

    /** Carries author-owned selection metadata across planner-driven relayout tag regeneration. */
    public static Map<String, String> preserveExplicitMetadata(Map<String, String> generatedTags,
                                                                Map<String, String> authoredTags) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(generatedTags);
        for (String key : EXPLICIT_KEYS) {
            if (authoredTags.containsKey(key)) {
                result.put(key, authoredTags.get(key));
            }
        }
        return result;
    }

    private static int positiveInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean booleanValue(String value, boolean fallback) {
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
