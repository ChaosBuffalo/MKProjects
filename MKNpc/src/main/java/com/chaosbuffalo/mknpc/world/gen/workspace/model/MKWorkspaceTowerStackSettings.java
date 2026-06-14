package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKWorkspaceTowerStackSettings(
        String stackId,
        int minMainFloors,
        int mainFloors,
        int minBasementFloors,
        int basementFloors,
        MKTowerStackBudget heights,
        int width,
        int length,
        int shaftSize,
        MKVerticalAccessPlacement verticalAccessPlacement,
        MKWorkspaceStairAuthoringConfig stairConfig,
        boolean topCapApproachEnabled,
        boolean basementEntryEnabled,
        boolean basementCapApproachEnabled,
        MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
        MKWorkspaceFoundationPolicy foundationPolicy,
        @Nullable MKWorkspacePaletteOverride paletteOverride
) {
    public static final Codec<MKWorkspaceTowerStackSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stack_id").forGetter(MKWorkspaceTowerStackSettings::stackId),
            Codec.INT.optionalFieldOf("min_main_floors", 1).forGetter(MKWorkspaceTowerStackSettings::minMainFloors),
            Codec.INT.optionalFieldOf("main_floors", 1).forGetter(MKWorkspaceTowerStackSettings::mainFloors),
            Codec.INT.optionalFieldOf("min_basement_floors", 1).forGetter(MKWorkspaceTowerStackSettings::minBasementFloors),
            Codec.INT.optionalFieldOf("basement_floors", 1).forGetter(MKWorkspaceTowerStackSettings::basementFloors),
            MKTowerStackBudget.CODEC.optionalFieldOf("heights", MKTowerStackBudget.uniform(7))
                    .forGetter(MKWorkspaceTowerStackSettings::heights),
            Codec.INT.optionalFieldOf("width", 7).forGetter(MKWorkspaceTowerStackSettings::width),
            Codec.INT.optionalFieldOf("length", 7).forGetter(MKWorkspaceTowerStackSettings::length),
            Codec.INT.optionalFieldOf("shaft_size", 3).forGetter(MKWorkspaceTowerStackSettings::shaftSize),
            MKWorkspaceCodecs.VERTICAL_ACCESS_PLACEMENT_CODEC.optionalFieldOf("vertical_access_placement",
                    MKVerticalAccessPlacement.CENTER).forGetter(MKWorkspaceTowerStackSettings::verticalAccessPlacement),
            MKWorkspaceStairAuthoringConfig.CODEC.optionalFieldOf("stair_config",
                    MKWorkspaceStairAuthoringConfig.defaultConfig()).forGetter(MKWorkspaceTowerStackSettings::stairConfig),
            Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                    .forGetter(MKWorkspaceTowerStackSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basement_entry_enabled", true)
                    .forGetter(MKWorkspaceTowerStackSettings::basementEntryEnabled),
            Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                    .forGetter(MKWorkspaceTowerStackSettings::basementCapApproachEnabled),
            SerializationExtras.CODEC.forGetter(SerializationExtras::from)
    ).apply(instance, (stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, heights, width, length, shaftSize,
                       verticalAccessPlacement, stairConfig, topCapApproachEnabled, basementEntryEnabled,
                       basementCapApproachEnabled, extras) ->
            new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors,
                    heights, width, length, shaftSize,
                    verticalAccessPlacement, stairConfig, topCapApproachEnabled, basementEntryEnabled,
                    basementCapApproachEnabled,
                    extras.horizontalExtrusionMode(), extras.foundationPolicy(), extras.paletteOverride().orElse(null))));

    private record SerializationExtras(
            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
            MKWorkspaceFoundationPolicy foundationPolicy,
            Optional<MKWorkspacePaletteOverride> paletteOverride
    ) {
        private static final MapCodec<SerializationExtras> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MKWorkspaceCodecs.HORIZONTAL_EXTRUSION_MODE_CODEC.optionalFieldOf("horizontal_extrusion_mode",
                        MKWorkspaceHorizontalExtrusionMode.FULL_BODY)
                        .forGetter(SerializationExtras::horizontalExtrusionMode),
                MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy", MKWorkspaceFoundationPolicy.none())
                        .forGetter(SerializationExtras::foundationPolicy),
                MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                        .forGetter(SerializationExtras::paletteOverride)
        ).apply(instance, SerializationExtras::new));

        private static SerializationExtras from(MKWorkspaceTowerStackSettings settings) {
            return new SerializationExtras(settings.horizontalExtrusionMode(), settings.foundationPolicy(),
                    settings.paletteOverrideOpt());
        }
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, MKTowerStackBudget.uniform(height),
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, defaultStairConfig(), true, true, false,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, MKTowerStackBudget.uniform(height),
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, defaultStairConfig(), topCapApproachEnabled,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED, basementCapApproachEnabled,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length,
                topCapApproachEnabled,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementEntryEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int minMainFloors, int mainFloors,
                                         int minBasementFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementEntryEnabled, boolean basementCapApproachEnabled) {
        this(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, MKTowerStackBudget.uniform(height),
                width, length,
                defaultShaftSize(),
                MKVerticalAccessPlacement.CENTER, defaultStairConfig(), topCapApproachEnabled,
                basementEntryEnabled, basementCapApproachEnabled, MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length, shaftSize,
                verticalAccessPlacement,
                topCapApproachEnabled, MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int minMainFloors, int mainFloors,
                                         int minBasementFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         boolean topCapApproachEnabled, boolean basementEntryEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, MKTowerStackBudget.uniform(height),
                width, length,
                shaftSize, verticalAccessPlacement,
                defaultStairConfig(), topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         MKWorkspaceStairAuthoringConfig stairConfig,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length, shaftSize,
                verticalAccessPlacement,
                stairConfig, topCapApproachEnabled, MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                basementCapApproachEnabled);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int minMainFloors, int mainFloors,
                                         int minBasementFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         MKWorkspaceStairAuthoringConfig stairConfig,
                                         boolean topCapApproachEnabled, boolean basementEntryEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, MKTowerStackBudget.uniform(height),
                width, length,
                shaftSize, verticalAccessPlacement,
                stairConfig, topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceTowerStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         MKWorkspaceStairAuthoringConfig stairConfig,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled,
                                         MKWorkspaceFoundationPolicy foundationPolicy,
                                         @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, MKTowerStackBudget.uniform(height),
                width, length, shaftSize,
                verticalAccessPlacement,
                stairConfig, topCapApproachEnabled, MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                basementCapApproachEnabled, MKWorkspaceHorizontalExtrusionMode.FULL_BODY, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceTowerStackSettings {
        stackId = stackId == null ? "" : stackId;
        mainFloors = Math.max(0, mainFloors);
        minMainFloors = Math.max(0, Math.min(minMainFloors, mainFloors));
        basementFloors = Math.max(0, basementFloors);
        minBasementFloors = Math.max(0, Math.min(minBasementFloors, basementFloors));
        heights = heights == null ? MKTowerStackBudget.uniform(7) : heights;
        width = normalizeFootprint(width);
        length = normalizeFootprint(length);
        verticalAccessPlacement = verticalAccessPlacement == null ? MKVerticalAccessPlacement.CENTER : verticalAccessPlacement;
        stairConfig = normalizeStairConfig(stairConfig, shaftSize, width, length);
        shaftSize = MKWorkspaceDimensions.snapToNearestUsableShaftSize(stairConfig, width, length, shaftSize, 3);
        stairConfig = normalizeStairConfig(stairConfig, shaftSize, width, length);
        horizontalExtrusionMode = horizontalExtrusionMode == null ? MKWorkspaceHorizontalExtrusionMode.FULL_BODY :
                horizontalExtrusionMode;
        foundationPolicy = foundationPolicy == null ? MKWorkspaceFoundationPolicy.none() : foundationPolicy;
        paletteOverride = paletteOverride != null && !paletteOverride.isEmpty() ? paletteOverride : null;
    }

    public static MKWorkspaceTowerStackSettings defaults(String stackId, int height) {
        return new MKWorkspaceTowerStackSettings(stackId,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_MAIN_FLOORS,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_MAIN_FLOORS,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_FLOORS,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_FLOORS, MKTowerStackBudget.uniform(height),
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, defaultStairConfig(),
                MKWorkspaceTowerStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                MKWorkspaceTowerStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceTowerStackSettings withMainFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, Math.min(minMainFloors, Math.max(0, value)), value,
                minBasementFloors, basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withMinMainFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, value, mainFloors, minBasementFloors, basementFloors,
                heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withStackId(String value) {
        return new MKWorkspaceTowerStackSettings(value, minMainFloors, mainFloors, minBasementFloors, basementFloors,
                heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withBasementFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors,
                Math.min(minBasementFloors, Math.max(0, value)), value, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withMinBasementFloors(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, value, basementFloors,
                heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withHeight(int value) {
        return withHeights(MKTowerStackBudget.uniform(value));
    }

    public MKWorkspaceTowerStackSettings withHeights(MKTowerStackBudget value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, value, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withEntryHeight(int value) {
        return withHeights(new MKTowerStackBudget(value, heights.mainFloorHeight(), heights.basementFloorHeight(),
                heights.basementEntryHeight(), heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceTowerStackSettings withBasementHeight(int value) {
        return withHeights(new MKTowerStackBudget(heights.entryHeight(), heights.mainFloorHeight(), value,
                heights.basementEntryHeight(), heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceTowerStackSettings withBasementEntryHeight(int value) {
        return withHeights(new MKTowerStackBudget(heights.entryHeight(), heights.mainFloorHeight(),
                heights.basementFloorHeight(), value, heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceTowerStackSettings withBasementCapHeight(int value) {
        return withHeights(new MKTowerStackBudget(heights.entryHeight(), heights.mainFloorHeight(),
                heights.basementFloorHeight(), heights.basementEntryHeight(), heights.topCapHeight(), value));
    }

    public MKWorkspaceTowerStackSettings withMainHeight(int value) {
        return withHeights(new MKTowerStackBudget(heights.entryHeight(), value, heights.basementFloorHeight(),
                heights.basementEntryHeight(), heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceTowerStackSettings withMainCapHeight(int value) {
        return withHeights(new MKTowerStackBudget(heights.entryHeight(), heights.mainFloorHeight(),
                heights.basementFloorHeight(), heights.basementEntryHeight(), value, heights.basementCapHeight()));
    }

    public MKWorkspaceTowerStackSettings withWidth(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, value, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withLength(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, value,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withShaftSize(int value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                value, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withVerticalAccessPlacement(MKVerticalAccessPlacement value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, value, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withStairConfig(MKWorkspaceStairAuthoringConfig value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, value,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withTopCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                value, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withBasementEntryEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, value, basementCapApproachEnabled, horizontalExtrusionMode, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withBasementCapApproachEnabled(boolean value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, value, horizontalExtrusionMode, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withHorizontalExtrusionMode(MKWorkspaceHorizontalExtrusionMode value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, value, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withFoundationPolicy(MKWorkspaceFoundationPolicy value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                value, paletteOverride);
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return Optional.ofNullable(paletteOverride);
    }

    public MKWorkspaceTowerStackSettings withPaletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        return new MKWorkspaceTowerStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, value.orElse(null));
    }

    public int height() {
        return heights.maxHeight();
    }

    public int entryHeight() {
        return heights.entryHeight();
    }

    public int basementHeight() {
        return heights.basementFloorHeight();
    }

    public int basementEntryHeight() {
        return heights.basementEntryHeight();
    }

    public int basementCapHeight() {
        return heights.basementCapHeight();
    }

    public int mainHeight() {
        return heights.mainFloorHeight();
    }

    public int mainCapHeight() {
        return heights.topCapHeight();
    }

    public int heightForTopologySlot(String topologySlotId) {
        return heights.heightForTopologySlot(topologySlotId);
    }

    private static int defaultFootprint(String stackId) {
        if ("keep.center".equals(stackId)) {
            return 17;
        }
        if ("tower.primary".equals(stackId)) {
            return 9;
        }
        return 7;
    }

    private static int normalizeFootprint(int value) {
        int footprint = Math.max(3, value);
        return footprint % 2 == 0 ? footprint + 1 : footprint;
    }

    private static int defaultShaftSize() {
        return MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
    }

    private static MKWorkspaceStairAuthoringConfig defaultStairConfig() {
        return MKWorkspaceStairAuthoringConfig.defaultConfig();
    }

    private static MKWorkspaceStairAuthoringConfig normalizeStairConfig(MKWorkspaceStairAuthoringConfig value,
                                                                        int shaftSize,
                                                                        int width,
                                                                        int length) {
        MKWorkspaceStairAuthoringConfig config = value == null ? defaultStairConfig() : value;
        int normalizedShaftSize = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(width, length, shaftSize);
        int stairWidth = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(normalizedShaftSize,
                config.stairWidth());
        return new MKWorkspaceStairAuthoringConfig(
                config.mode(),
                config.riseType(),
                stairWidth
        );
    }
}
