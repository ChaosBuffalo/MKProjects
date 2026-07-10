package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.world.level.block.Rotation;

import java.util.Map;

public final class MKWorkspaceTemplateReuseTags {
    public static final String SOURCE_ID_TAG = "workspace_template_source_id";
    public static final String ROTATION_TAG = "workspace_template_rotation";
    public static final String REUSE_MODE_TAG = "workspace_template_reuse_mode";
    public static final String AUTHORING_PIECE_TAG = "workspace_authoring_piece";
    public static final String CROP_MODE_TAG = "workspace_template_crop_mode";
    public static final String REUSE_MODE_ROTATE_EXPORT = "rotate_export";
    public static final String CROP_MODE_NONE = "none";
    public static final String CROP_MODE_NON_STRUCTURE_VOID = "non_structure_void";
    public static final String ROTATION_NONE = "none";
    public static final String ROTATION_CLOCKWISE_90 = "clockwise_90";
    public static final String ROTATION_CLOCKWISE_180 = "clockwise_180";
    public static final String ROTATION_CLOCKWISE_270 = "clockwise_270";

    private MKWorkspaceTemplateReuseTags() {
    }

    public static boolean isRotatedExportReuse(Map<String, String> tags) {
        return REUSE_MODE_ROTATE_EXPORT.equals(tags.get(REUSE_MODE_TAG));
    }

    public static boolean isDerived(Map<String, String> tags) {
        return isRotatedExportReuse(tags) && "false".equals(tags.get(AUTHORING_PIECE_TAG));
    }

    public static boolean isAuthoringSource(Map<String, String> tags) {
        return isRotatedExportReuse(tags) && !"false".equals(tags.get(AUTHORING_PIECE_TAG));
    }

    public static boolean cropsNonStructureVoid(Map<String, String> tags) {
        return CROP_MODE_NON_STRUCTURE_VOID.equals(tags.getOrDefault(CROP_MODE_TAG, CROP_MODE_NONE));
    }

    public static String sourceId(Map<String, String> tags) {
        return tags.getOrDefault(SOURCE_ID_TAG, "");
    }

    public static String rotationName(Map<String, String> tags) {
        return tags.getOrDefault(ROTATION_TAG, ROTATION_NONE);
    }

    public static Rotation rotation(Map<String, String> tags) {
        return switch (rotationName(tags)) {
            case ROTATION_CLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            case ROTATION_CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
            case ROTATION_CLOCKWISE_270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
