package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceTowerStackSettings(
        String stackId,
        int mainFloors,
        int basementFloors,
        int height,
        int width,
        int length,
        int shaftSize,
        MKVerticalAccessPlacement verticalAccessPlacement,
        boolean topCapApproachEnabled,
        boolean basementCapApproachEnabled
) {
    public static final Codec<MKWorkspaceTowerStackSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stack_id").forGetter(MKWorkspaceTowerStackSettings::stackId),
            Codec.INT.optionalFieldOf("main_floors", 1).forGetter(MKWorkspaceTowerStackSettings::mainFloors),
            Codec.INT.optionalFieldOf("basement_floors", 1).forGetter(MKWorkspaceTowerStackSettings::basementFloors),
            Codec.INT.optionalFieldOf("height", 7).forGetter(MKWorkspaceTowerStackSettings::height),
            Codec.INT.optionalFieldOf("width", 7).forGetter(MKWorkspaceTowerStackSettings::width),
            Codec.INT.optionalFieldOf("length", 7).forGetter(MKWorkspaceTowerStackSettings::length),
            Codec.INT.optionalFieldOf("shaft_size", 3).forGetter(MKWorkspaceTowerStackSettings::shaftSize),
            MKWorkspaceCodecs.VERTICAL_ACCESS_PLACEMENT_CODEC.optionalFieldOf("vertical_access_placement",
                    MKVerticalAccessPlacement.CENTER).forGetter(MKWorkspaceTowerStackSettings::verticalAccessPlacement),
            Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                    .forGetter(MKWorkspaceTowerStackSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                    .forGetter(MKWorkspaceTowerStackSettings::basementCapApproachEnabled)
    ).apply(instance, MKWorkspaceTowerStackSettings::new));

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height) {
        this(stackId, mainFloors, basementFloors, height, defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, true, false);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, basementFloors, height, defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, topCapApproachEnabled,
                basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, basementFloors, height, width, length, defaultShaftSize(),
                MKVerticalAccessPlacement.CENTER, topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings {
        stackId = stackId == null ? "" : stackId;
        mainFloors = Math.max(0, mainFloors);
        basementFloors = Math.max(0, basementFloors);
        height = Math.max(3, height);
        width = normalizeFootprint(width);
        length = normalizeFootprint(length);
        shaftSize = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(width, length, shaftSize);
        verticalAccessPlacement = verticalAccessPlacement == null ? MKVerticalAccessPlacement.CENTER : verticalAccessPlacement;
    }

    public static MKWorkspaceTowerStackSettings defaults(String stackId, int height) {
        MKTowerWorkspaceFloorSettings defaults = MKTowerWorkspaceFloorSettings.defaultSettings();
        return new MKWorkspaceTowerStackSettings(stackId, defaults.mainFloors(), defaults.basementFloors(), height,
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER,
                defaults.topCapApproachEnabled(), defaults.basementCapApproachEnabled());
    }

    public MKWorkspaceTowerStackSettings withMainFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, value, basementFloors, height, width, length,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withStackId(String value) {
        return new MKWorkspaceTowerStackSettings(value, mainFloors, basementFloors, height, width, length,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withBasementFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, value, height, width, length,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withHeight(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, value, width, length,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withWidth(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, value, length,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withLength(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, value,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withShaftSize(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, length,
                value, verticalAccessPlacement,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withVerticalAccessPlacement(MKVerticalAccessPlacement value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, length,
                shaftSize, value,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withTopCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, length,
                shaftSize, verticalAccessPlacement,
                value, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withBasementCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, length,
                shaftSize, verticalAccessPlacement,
                topCapApproachEnabled, value);
    }

    private static int defaultFootprint(String stackId) {
        return "keep.center".equals(stackId) ? 17 : 7;
    }

    private static int normalizeFootprint(int value) {
        int footprint = Math.max(3, value);
        return footprint % 2 == 0 ? footprint + 1 : footprint;
    }

    private static int defaultShaftSize() {
        return MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
    }
}
