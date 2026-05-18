package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceTowerStackSettings(
        String stackId,
        int mainFloors,
        int basementFloors,
        int height,
        boolean topCapApproachEnabled,
        boolean basementCapApproachEnabled
) {
    public static final Codec<MKWorkspaceTowerStackSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stack_id").forGetter(MKWorkspaceTowerStackSettings::stackId),
            Codec.INT.optionalFieldOf("main_floors", 1).forGetter(MKWorkspaceTowerStackSettings::mainFloors),
            Codec.INT.optionalFieldOf("basement_floors", 1).forGetter(MKWorkspaceTowerStackSettings::basementFloors),
            Codec.INT.optionalFieldOf("height", 7).forGetter(MKWorkspaceTowerStackSettings::height),
            Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                    .forGetter(MKWorkspaceTowerStackSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                    .forGetter(MKWorkspaceTowerStackSettings::basementCapApproachEnabled)
    ).apply(instance, MKWorkspaceTowerStackSettings::new));

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height) {
        this(stackId, mainFloors, basementFloors, height, true, false);
    }

    public MKWorkspaceTowerStackSettings {
        stackId = stackId == null ? "" : stackId;
        mainFloors = Math.max(0, mainFloors);
        basementFloors = Math.max(0, basementFloors);
        height = Math.max(3, height);
    }

    public static MKWorkspaceTowerStackSettings defaults(String stackId, int height) {
        MKTowerWorkspaceFloorSettings defaults = MKTowerWorkspaceFloorSettings.defaultSettings();
        return new MKWorkspaceTowerStackSettings(stackId, defaults.mainFloors(), defaults.basementFloors(), height,
                defaults.topCapApproachEnabled(), defaults.basementCapApproachEnabled());
    }

    public MKWorkspaceTowerStackSettings withMainFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, value, basementFloors, height,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withStackId(String value) {
        return new MKWorkspaceTowerStackSettings(value, mainFloors, basementFloors, height,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withBasementFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, value, height,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withHeight(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, value,
                topCapApproachEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withTopCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height,
                value, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings withBasementCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, mainFloors, basementFloors, height,
                topCapApproachEnabled, value);
    }
}
