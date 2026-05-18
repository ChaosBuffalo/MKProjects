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
            Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                    .forGetter(MKWorkspaceTowerStackSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                    .forGetter(MKWorkspaceTowerStackSettings::basementCapApproachEnabled)
    ).apply(instance, MKWorkspaceTowerStackSettings::new));

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height) {
        this(stackId, mainFloors, basementFloors, height, defaultFootprint(stackId), defaultFootprint(stackId),
                true, false);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, basementFloors, height, defaultFootprint(stackId), defaultFootprint(stackId),
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings {
        stackId = stackId == null ? "" : stackId;
        mainFloors = Math.max(0, mainFloors);
        basementFloors = Math.max(0, basementFloors);
        height = Math.max(3, height);
        width = normalizeFootprint(width);
        length = normalizeFootprint(length);
    }

    public static MKWorkspaceTowerStackSettings defaults(String stackId, int height) {
        MKTowerWorkspaceFloorSettings defaults = MKTowerWorkspaceFloorSettings.defaultSettings();
        return new MKWorkspaceTowerStackSettings(stackId, defaults.mainFloors(), defaults.basementFloors(), height,
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaults.topCapApproachEnabled(), defaults.basementCapApproachEnabled());
    }

    public MKWorkspaceTowerStackSettings withMainFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, value, basementFloors, height, width, length,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withStackId(String value) {
        return new MKWorkspaceTowerStackSettings(value, mainFloors, basementFloors, height, width, length,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withBasementFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, value, height, width, length,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withHeight(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, value, width, length,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withWidth(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, value, length,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withLength(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, value,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withTopCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, length,
                value, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withBasementCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height, width, length,
                topCapApproachEnabled, value);
    }

    private static int defaultFootprint(String stackId) {
        return "keep.center".equals(stackId) ? 17 : 7;
    }

    private static int normalizeFootprint(int value) {
        int footprint = Math.max(3, value);
        return footprint % 2 == 0 ? footprint + 1 : footprint;
    }
}
