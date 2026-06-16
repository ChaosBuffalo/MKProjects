package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKWorkspaceVerticalStackSettings(
        String stackId,
        int minMainFloors,
        int mainFloors,
        int minBasementFloors,
        int basementFloors,
        MKWorkspaceVerticalStackBudget heights,
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
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "vertical_stack");
    public static final Codec<MKWorkspaceVerticalStackSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stack_id").forGetter(MKWorkspaceVerticalStackSettings::stackId),
            Codec.INT.optionalFieldOf("min_main_floors", 1).forGetter(MKWorkspaceVerticalStackSettings::minMainFloors),
            Codec.INT.optionalFieldOf("main_floors", 1).forGetter(MKWorkspaceVerticalStackSettings::mainFloors),
            Codec.INT.optionalFieldOf("min_basement_floors", 1).forGetter(MKWorkspaceVerticalStackSettings::minBasementFloors),
            Codec.INT.optionalFieldOf("basement_floors", 1).forGetter(MKWorkspaceVerticalStackSettings::basementFloors),
            MKWorkspaceVerticalStackBudget.CODEC.optionalFieldOf("heights", MKWorkspaceVerticalStackBudget.uniform(7))
                    .forGetter(MKWorkspaceVerticalStackSettings::heights),
            Codec.INT.optionalFieldOf("width", 7).forGetter(MKWorkspaceVerticalStackSettings::width),
            Codec.INT.optionalFieldOf("length", 7).forGetter(MKWorkspaceVerticalStackSettings::length),
            Codec.INT.optionalFieldOf("shaft_size", 3).forGetter(MKWorkspaceVerticalStackSettings::shaftSize),
            MKWorkspaceCodecs.VERTICAL_ACCESS_PLACEMENT_CODEC.optionalFieldOf("vertical_access_placement",
                    MKVerticalAccessPlacement.CENTER).forGetter(MKWorkspaceVerticalStackSettings::verticalAccessPlacement),
            MKWorkspaceStairAuthoringConfig.CODEC.optionalFieldOf("stair_config",
                    MKWorkspaceStairAuthoringConfig.defaultConfig()).forGetter(MKWorkspaceVerticalStackSettings::stairConfig),
            Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                    .forGetter(MKWorkspaceVerticalStackSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basement_entry_enabled", true)
                    .forGetter(MKWorkspaceVerticalStackSettings::basementEntryEnabled),
            Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                    .forGetter(MKWorkspaceVerticalStackSettings::basementCapApproachEnabled),
            SerializationExtras.CODEC.forGetter(SerializationExtras::from)
    ).apply(instance, (stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, heights, width, length, shaftSize,
                       verticalAccessPlacement, stairConfig, topCapApproachEnabled, basementEntryEnabled,
                       basementCapApproachEnabled, extras) ->
            new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors,
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

        private static SerializationExtras from(MKWorkspaceVerticalStackSettings settings) {
            return new SerializationExtras(settings.horizontalExtrusionMode(), settings.foundationPolicy(),
                    settings.paletteOverrideOpt());
        }
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, MKWorkspaceVerticalStackBudget.uniform(height),
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, defaultStairConfig(), true, true, false,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, MKWorkspaceVerticalStackBudget.uniform(height),
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, defaultStairConfig(), topCapApproachEnabled,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED, basementCapApproachEnabled,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length,
                topCapApproachEnabled,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED, basementCapApproachEnabled);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementEntryEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int minMainFloors, int mainFloors,
                                         int minBasementFloors, int basementFloors, int height,
                                         int width, int length, boolean topCapApproachEnabled,
                                         boolean basementEntryEnabled, boolean basementCapApproachEnabled) {
        this(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, MKWorkspaceVerticalStackBudget.uniform(height),
                width, length,
                defaultShaftSize(),
                MKVerticalAccessPlacement.CENTER, defaultStairConfig(), topCapApproachEnabled,
                basementEntryEnabled, basementCapApproachEnabled, MKWorkspaceHorizontalExtrusionMode.FULL_BODY,
                MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length, shaftSize,
                verticalAccessPlacement,
                topCapApproachEnabled, MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                basementCapApproachEnabled);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int minMainFloors, int mainFloors,
                                         int minBasementFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         boolean topCapApproachEnabled, boolean basementEntryEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, MKWorkspaceVerticalStackBudget.uniform(height),
                width, length,
                shaftSize, verticalAccessPlacement,
                defaultStairConfig(), topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         MKWorkspaceStairAuthoringConfig stairConfig,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, height, width, length, shaftSize,
                verticalAccessPlacement,
                stairConfig, topCapApproachEnabled, MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                basementCapApproachEnabled);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int minMainFloors, int mainFloors,
                                         int minBasementFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         MKWorkspaceStairAuthoringConfig stairConfig,
                                         boolean topCapApproachEnabled, boolean basementEntryEnabled,
                                         boolean basementCapApproachEnabled) {
        this(stackId, minMainFloors, mainFloors, minBasementFloors, basementFloors, MKWorkspaceVerticalStackBudget.uniform(height),
                width, length,
                shaftSize, verticalAccessPlacement,
                stairConfig, topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public MKWorkspaceVerticalStackSettings(String stackId, int mainFloors, int basementFloors, int height,
                                         int width, int length, int shaftSize,
                                         MKVerticalAccessPlacement verticalAccessPlacement,
                                         MKWorkspaceStairAuthoringConfig stairConfig,
                                         boolean topCapApproachEnabled, boolean basementCapApproachEnabled,
                                         MKWorkspaceFoundationPolicy foundationPolicy,
                                         @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(stackId, mainFloors, mainFloors, basementFloors, basementFloors, MKWorkspaceVerticalStackBudget.uniform(height),
                width, length, shaftSize,
                verticalAccessPlacement,
                stairConfig, topCapApproachEnabled, MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                basementCapApproachEnabled, MKWorkspaceHorizontalExtrusionMode.FULL_BODY, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings {
        stackId = stackId == null ? "" : stackId;
        mainFloors = Math.max(0, mainFloors);
        minMainFloors = Math.max(0, Math.min(minMainFloors, mainFloors));
        basementFloors = Math.max(0, basementFloors);
        minBasementFloors = Math.max(0, Math.min(minBasementFloors, basementFloors));
        heights = heights == null ? MKWorkspaceVerticalStackBudget.uniform(7) : heights;
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

    public static MKWorkspaceVerticalStackSettings defaults(String stackId, int height) {
        return new MKWorkspaceVerticalStackSettings(stackId,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_MAIN_FLOORS,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_MAIN_FLOORS,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_FLOORS,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_FLOORS, MKWorkspaceVerticalStackBudget.uniform(height),
                defaultFootprint(stackId), defaultFootprint(stackId),
                defaultShaftSize(), MKVerticalAccessPlacement.CENTER, defaultStairConfig(),
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_TOP_CAP_APPROACH_ENABLED,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_ENTRY_ENABLED,
                MKWorkspaceVerticalStackFloorCounts.DEFAULT_BASEMENT_CAP_APPROACH_ENABLED,
                MKWorkspaceHorizontalExtrusionMode.FULL_BODY, MKWorkspaceFoundationPolicy.none(), null);
    }

    public static MKWorkspaceVerticalStackSettings fromPlannerSettingsEntry(MKWorkspacePlannerSettingsEntry entry) {
        MKWorkspaceVerticalStackSettings settings = MKWorkspaceCodecs.parseNbt(CODEC, entry.settings(),
                "workspace vertical stack settings");
        return settings.withPaletteOverride(entry.paletteOverride());
    }

    public MKWorkspacePlannerSettingsEntry plannerSettingsEntry() {
        MKWorkspaceVerticalStackSettings settingsPayload = withPaletteOverride(Optional.empty());
        return new MKWorkspacePlannerSettingsEntry(
                PLANNER_ID,
                stackId,
                paletteOverrideOpt(),
                MKWorkspaceCodecs.encodeNbt(CODEC, settingsPayload, "workspace vertical stack settings"));
    }

    public MKWorkspaceVerticalStackSettings withMainFloors(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, Math.min(minMainFloors, Math.max(0, value)), value,
                minBasementFloors, basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withMinMainFloors(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, value, mainFloors, minBasementFloors, basementFloors,
                heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withStackId(String value) {
        return new MKWorkspaceVerticalStackSettings(value, minMainFloors, mainFloors, minBasementFloors, basementFloors,
                heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withBasementFloors(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors,
                Math.min(minBasementFloors, Math.max(0, value)), value, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withMinBasementFloors(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, value, basementFloors,
                heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withHeight(int value) {
        return withHeights(MKWorkspaceVerticalStackBudget.uniform(value));
    }

    public MKWorkspaceVerticalStackSettings withHeights(MKWorkspaceVerticalStackBudget value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, value, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withEntryHeight(int value) {
        return withHeights(new MKWorkspaceVerticalStackBudget(value, heights.mainFloorHeight(), heights.basementFloorHeight(),
                heights.basementEntryHeight(), heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceVerticalStackSettings withBasementHeight(int value) {
        return withHeights(new MKWorkspaceVerticalStackBudget(heights.entryHeight(), heights.mainFloorHeight(), value,
                heights.basementEntryHeight(), heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceVerticalStackSettings withBasementEntryHeight(int value) {
        return withHeights(new MKWorkspaceVerticalStackBudget(heights.entryHeight(), heights.mainFloorHeight(),
                heights.basementFloorHeight(), value, heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceVerticalStackSettings withBasementCapHeight(int value) {
        return withHeights(new MKWorkspaceVerticalStackBudget(heights.entryHeight(), heights.mainFloorHeight(),
                heights.basementFloorHeight(), heights.basementEntryHeight(), heights.topCapHeight(), value));
    }

    public MKWorkspaceVerticalStackSettings withMainHeight(int value) {
        return withHeights(new MKWorkspaceVerticalStackBudget(heights.entryHeight(), value, heights.basementFloorHeight(),
                heights.basementEntryHeight(), heights.topCapHeight(), heights.basementCapHeight()));
    }

    public MKWorkspaceVerticalStackSettings withMainCapHeight(int value) {
        return withHeights(new MKWorkspaceVerticalStackBudget(heights.entryHeight(), heights.mainFloorHeight(),
                heights.basementFloorHeight(), heights.basementEntryHeight(), value, heights.basementCapHeight()));
    }

    public MKWorkspaceVerticalStackSettings withWidth(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, value, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withLength(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, value,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withShaftSize(int value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                value, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withVerticalAccessPlacement(MKVerticalAccessPlacement value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, value, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withStairConfig(MKWorkspaceStairAuthoringConfig value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, value,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                foundationPolicy, paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withTopCapApproachEnabled(boolean value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                value, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withBasementEntryEnabled(boolean value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, value, basementCapApproachEnabled, horizontalExtrusionMode, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withBasementCapApproachEnabled(boolean value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, value, horizontalExtrusionMode, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withHorizontalExtrusionMode(MKWorkspaceHorizontalExtrusionMode value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, value, foundationPolicy,
                paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withFoundationPolicy(MKWorkspaceFoundationPolicy value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
                basementFloors, heights, width, length,
                shaftSize, verticalAccessPlacement, stairConfig,
                topCapApproachEnabled, basementEntryEnabled, basementCapApproachEnabled, horizontalExtrusionMode,
                value, paletteOverride);
    }

    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return Optional.ofNullable(paletteOverride);
    }

    public MKWorkspaceVerticalStackSettings withPaletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        return new MKWorkspaceVerticalStackSettings(stackId, minMainFloors, mainFloors, minBasementFloors,
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
