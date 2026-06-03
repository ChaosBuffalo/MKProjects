package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record MKWalledKeepCourtyardSettings(
        boolean courtyardContentEnabled,
        boolean courtyardSocketGenerationEnabled,
        int courtyardContentTemplateHeight,
        int courtyardSocketClearance,
        int courtyardWalkwayContinuationLength,
        int courtyardSmallTemplateSize,
        int courtyardMediumTemplateSize,
        int courtyardLargeTemplateSize
) {
    public static final int DEFAULT_CONTENT_TEMPLATE_HEIGHT = 7;
    public static final int DEFAULT_SOCKET_CLEARANCE = 1;
    public static final int DEFAULT_WALKWAY_CONTINUATION_LENGTH = 3;
    public static final int DEFAULT_SMALL_TEMPLATE_SIZE = 5;
    public static final int DEFAULT_MEDIUM_TEMPLATE_SIZE = 7;
    public static final int DEFAULT_LARGE_TEMPLATE_SIZE = 9;

    public static final Codec<MKWalledKeepCourtyardSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("courtyard_content_enabled", true)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardContentEnabled),
            Codec.BOOL.optionalFieldOf("courtyard_socket_generation_enabled", true)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardSocketGenerationEnabled),
            Codec.INT.optionalFieldOf("courtyard_content_template_height", DEFAULT_CONTENT_TEMPLATE_HEIGHT)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardContentTemplateHeight),
            Codec.INT.optionalFieldOf("courtyard_socket_clearance", DEFAULT_SOCKET_CLEARANCE)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardSocketClearance),
            Codec.INT.optionalFieldOf("courtyard_walkway_continuation_length", DEFAULT_WALKWAY_CONTINUATION_LENGTH)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardWalkwayContinuationLength),
            Codec.INT.optionalFieldOf("courtyard_small_template_size", DEFAULT_SMALL_TEMPLATE_SIZE)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardSmallTemplateSize),
            Codec.INT.optionalFieldOf("courtyard_medium_template_size", DEFAULT_MEDIUM_TEMPLATE_SIZE)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardMediumTemplateSize),
            Codec.INT.optionalFieldOf("courtyard_large_template_size", DEFAULT_LARGE_TEMPLATE_SIZE)
                    .forGetter(MKWalledKeepCourtyardSettings::courtyardLargeTemplateSize)
    ).apply(instance, MKWalledKeepCourtyardSettings::new));

    public static MKWalledKeepCourtyardSettings defaults() {
        return new MKWalledKeepCourtyardSettings(
                true,
                true,
                DEFAULT_CONTENT_TEMPLATE_HEIGHT,
                DEFAULT_SOCKET_CLEARANCE,
                DEFAULT_WALKWAY_CONTINUATION_LENGTH,
                DEFAULT_SMALL_TEMPLATE_SIZE,
                DEFAULT_MEDIUM_TEMPLATE_SIZE,
                DEFAULT_LARGE_TEMPLATE_SIZE
        );
    }

    public List<String> validate() {
        ArrayList<String> errors = new ArrayList<>();
        if (courtyardContentTemplateHeight < 1) {
            errors.add("walled keep courtyard content template height must be at least 1");
        }
        if (courtyardSocketClearance < 0) {
            errors.add("walled keep courtyard socket clearance must be at least 0");
        }
        if (courtyardWalkwayContinuationLength < 1) {
            errors.add("walled keep courtyard walkway continuation length must be at least 1");
        }
        validateSize(errors, "small", courtyardSmallTemplateSize);
        validateSize(errors, "medium", courtyardMediumTemplateSize);
        validateSize(errors, "large", courtyardLargeTemplateSize);
        if (courtyardSmallTemplateSize >= courtyardMediumTemplateSize) {
            errors.add("walled keep courtyard small template size must be smaller than medium template size");
        }
        if (courtyardMediumTemplateSize >= courtyardLargeTemplateSize) {
            errors.add("walled keep courtyard medium template size must be smaller than large template size");
        }
        return List.copyOf(errors);
    }

    private void validateSize(List<String> errors, String label, int size) {
        if (size < 3) {
            errors.add("walled keep courtyard " + label + " template size must be at least 3");
        }
        if (size % 2 == 0) {
            errors.add("walled keep courtyard " + label + " template size must be odd");
        }
    }
}
