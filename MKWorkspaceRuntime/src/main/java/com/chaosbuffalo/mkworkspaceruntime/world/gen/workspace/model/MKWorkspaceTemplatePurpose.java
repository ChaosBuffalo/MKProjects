package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;

import java.util.Locale;
import java.util.Optional;

/**
 * Declares why a physical workspace template exists. Selection code must use this value instead of
 * inferring eligibility from a piece name or variant index.
 */
public enum MKWorkspaceTemplatePurpose {
    SLOT_SCAFFOLD("slot_scaffold", false),
    FAMILY_CANONICAL("family_canonical", true),
    FAMILY_VARIANT("family_variant", true),
    DERIVED_DATA_ONLY("derived_data_only", false);

    public static final Codec<MKWorkspaceTemplatePurpose> CODEC = Codec.STRING.xmap(
            value -> fromSerializedName(value)
                    .orElseThrow(() -> new IllegalArgumentException("unknown workspace template purpose " + value)),
            MKWorkspaceTemplatePurpose::serializedName
    );

    private final String serializedName;
    private final boolean placeable;

    MKWorkspaceTemplatePurpose(String serializedName, boolean placeable) {
        this.serializedName = serializedName;
        this.placeable = placeable;
    }

    public String serializedName() {
        return serializedName;
    }

    public boolean placeable() {
        return placeable;
    }

    public boolean canonical() {
        return this == FAMILY_CANONICAL;
    }

    public boolean variant() {
        return this == FAMILY_VARIANT;
    }

    public static Optional<MKWorkspaceTemplatePurpose> fromSerializedName(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (MKWorkspaceTemplatePurpose purpose : values()) {
            if (purpose.serializedName.equals(normalized)) {
                return Optional.of(purpose);
            }
        }
        return Optional.empty();
    }
}
