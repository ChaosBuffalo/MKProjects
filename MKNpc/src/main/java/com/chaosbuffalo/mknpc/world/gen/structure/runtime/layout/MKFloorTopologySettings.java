package com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerSettingsEntry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackBudget;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKFloorTopologySettings(
        String stackId,
        String floorRole,
        int minMainPathPieces,
        int maxMainPathPieces,
        int maxBranchPiecesBeforeCap,
        MKHallwayLeadInMode hallwayLeadInMode,
        int manualHallwayLeadInPieces,
        boolean mainHallwaysEnabled,
        boolean branchHallwaysEnabled,
        boolean mainCapApproachEnabled,
        float sprawl,
        boolean linksEnabled,
        float linkDensity,
        int maxLinksPerFloor,
        int maxLinksPerRoom,
        int maxLinkLength,
        MKFloorLinkGenerationMode linkGenerationMode,
        float linkDecay,
        int endpointIntactRadius,
        float middleDecayBonus,
        Optional<String> insertFamily,
        int insertDepth,
        int insertSpacing,
        float insertProbability,
        float insertMaxDecay,
        int mainHallwayLength,
        int mainHallwayWidth,
        int branchHallwayLength,
        int branchHallwayWidth,
        Optional<Long> lockedLayoutSeed,
        Optional<MKWorkspacePaletteOverride> paletteOverride,
        List<MKFloorRoomProfile> mainRoomProfiles,
        List<MKFloorRoomProfile> branchRoomProfiles,
        List<MKFloorRoomProfile> branchCapProfiles,
        List<MKFloorRoomProfile> mainCapApproachProfiles,
        List<MKFloorRoomProfile> mainCapProfiles
) {
    public static final int DEFAULT_MIN_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP = 0;
    public static final int MAX_BRANCH_PIECES_BEFORE_CAP = 10;
    public static final int MAX_MANUAL_HALLWAY_LEAD_IN_PIECES = 45;
    public static final float DEFAULT_SPRAWL = 0.5f;
    public static final boolean DEFAULT_LINKS_ENABLED = false;
    public static final float DEFAULT_LINK_DENSITY = 1.0f;
    public static final int DEFAULT_MAX_LINKS_PER_FLOOR = 10;
    public static final int DEFAULT_MAX_LINKS_PER_ROOM = 3;
    public static final int DEFAULT_MAX_LINK_LENGTH = 32;
    public static final MKFloorLinkGenerationMode DEFAULT_LINK_GENERATION_MODE =
            MKFloorLinkGenerationMode.FULL_HALLWAY;
    public static final float DEFAULT_LINK_DECAY = 0.0f;
    public static final int DEFAULT_ENDPOINT_INTACT_RADIUS = 3;
    public static final float DEFAULT_MIDDLE_DECAY_BONUS = 0.25f;
    public static final int DEFAULT_INSERT_DEPTH = 1;
    public static final int DEFAULT_INSERT_SPACING = 0;
    public static final float DEFAULT_INSERT_PROBABILITY = 1.0f;
    public static final float DEFAULT_INSERT_MAX_DECAY = 0.55f;
    public static final int MAX_LINKS_PER_FLOOR = 64;
    public static final int MAX_LINKS_PER_ROOM = 3;
    public static final int MAX_LINK_LENGTH = 128;
    public static final int MAX_ENDPOINT_INTACT_RADIUS = 16;
    public static final int MAX_INSERT_DEPTH = 16;
    public static final int MAX_INSERT_SPACING = 128;
    public static final ResourceLocation PLANNER_ID = ResourceLocation.fromNamespaceAndPath("mknpc", "floor_topology");

    public static final Codec<MKFloorTopologySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScalarSettings.CODEC.fieldOf("settings").forGetter(MKFloorTopologySettings::scalarSettings),
            LinkSettings.CODEC.optionalFieldOf("links", LinkSettings.DEFAULTS)
                    .forGetter(MKFloorTopologySettings::linkSettings),
            RoomProfiles.CODEC.fieldOf("profiles").forGetter(MKFloorTopologySettings::roomProfiles)
    ).apply(instance, (settings, links, profiles) -> new MKFloorTopologySettings(
            settings.stackId(),
            settings.floorRole(),
            settings.minMainPathPieces(),
            settings.maxMainPathPieces(),
            settings.maxBranchPiecesBeforeCap(),
            settings.hallwayLeadInMode(),
            settings.manualHallwayLeadInPieces(),
            settings.mainHallwaysEnabled(),
            settings.branchHallwaysEnabled(),
            settings.mainCapApproachEnabled(),
            settings.sprawl(),
            links.enabled(),
            links.density(),
            links.maxLinksPerFloor(),
            links.maxLinksPerRoom(),
            links.maxLinkLength(),
            links.generationMode(),
            links.decay(),
            links.endpointIntactRadius(),
            links.middleDecayBonus(),
            links.insertFamily(),
            links.insertDepth(),
            links.insertSpacing(),
            links.insertProbability(),
            links.insertMaxDecay(),
            links.mainHallwayLength(),
            links.mainHallwayWidth(),
            links.branchHallwayLength(),
            links.branchHallwayWidth(),
            settings.lockedLayoutSeed(),
            settings.paletteOverride(),
            profiles.mainRoomProfiles(),
            profiles.branchRoomProfiles(),
            profiles.branchCapProfiles(),
            profiles.mainCapApproachProfiles(),
            profiles.mainCapProfiles()
    )));

    private record ScalarSettings(
            String stackId,
            String floorRole,
            int minMainPathPieces,
            int maxMainPathPieces,
            int maxBranchPiecesBeforeCap,
            MKHallwayLeadInMode hallwayLeadInMode,
            int manualHallwayLeadInPieces,
            boolean mainHallwaysEnabled,
            boolean branchHallwaysEnabled,
            boolean mainCapApproachEnabled,
            float sprawl,
            Optional<Long> lockedLayoutSeed,
            Optional<MKWorkspacePaletteOverride> paletteOverride
    ) {
        private static final Codec<ScalarSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("stack_id").forGetter(ScalarSettings::stackId),
                Codec.STRING.fieldOf("floor_role").forGetter(ScalarSettings::floorRole),
                Codec.INT.optionalFieldOf("min_main_path_pieces", DEFAULT_MIN_MAIN_PATH_PIECES)
                        .forGetter(ScalarSettings::minMainPathPieces),
                Codec.INT.optionalFieldOf("max_main_path_pieces", DEFAULT_MAX_MAIN_PATH_PIECES)
                        .forGetter(ScalarSettings::maxMainPathPieces),
                Codec.INT.optionalFieldOf("max_branch_pieces_before_cap", DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP)
                        .forGetter(ScalarSettings::maxBranchPiecesBeforeCap),
                MKHallwayLeadInMode.CODEC.optionalFieldOf("hallway_lead_in_mode",
                                MKHallwayLeadInMode.AUTO)
                        .forGetter(ScalarSettings::hallwayLeadInMode),
                Codec.INT.optionalFieldOf("manual_hallway_lead_in_pieces", 1)
                        .forGetter(ScalarSettings::manualHallwayLeadInPieces),
                Codec.BOOL.optionalFieldOf("main_hallways_enabled", true)
                        .forGetter(ScalarSettings::mainHallwaysEnabled),
                Codec.BOOL.optionalFieldOf("branch_hallways_enabled", true)
                        .forGetter(ScalarSettings::branchHallwaysEnabled),
                Codec.BOOL.optionalFieldOf("main_cap_approach_enabled", false)
                        .forGetter(ScalarSettings::mainCapApproachEnabled),
                Codec.FLOAT.optionalFieldOf("sprawl", DEFAULT_SPRAWL)
                        .forGetter(ScalarSettings::sprawl),
                Codec.LONG.optionalFieldOf("locked_layout_seed")
                        .forGetter(ScalarSettings::lockedLayoutSeed),
                MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                        .forGetter(ScalarSettings::paletteOverride)
        ).apply(instance, ScalarSettings::new));
    }

    private record LinkSettings(
            boolean enabled,
            float density,
            int maxLinksPerFloor,
            int maxLinksPerRoom,
            int maxLinkLength,
            int mainHallwayLength,
            int mainHallwayWidth,
            int branchHallwayLength,
            int branchHallwayWidth,
            LinkGenerationSettings generation
    ) {
        private static final LinkSettings DEFAULTS = new LinkSettings(
                DEFAULT_LINKS_ENABLED,
                DEFAULT_LINK_DENSITY,
                DEFAULT_MAX_LINKS_PER_FLOOR,
                DEFAULT_MAX_LINKS_PER_ROOM,
                DEFAULT_MAX_LINK_LENGTH,
                0,
                0,
                0,
                0,
                LinkGenerationSettings.DEFAULTS
        );
        private static final Codec<LinkSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", DEFAULT_LINKS_ENABLED)
                        .forGetter(LinkSettings::enabled),
                Codec.FLOAT.optionalFieldOf("density", DEFAULT_LINK_DENSITY)
                        .forGetter(LinkSettings::density),
                Codec.INT.optionalFieldOf("max_links_per_floor", DEFAULT_MAX_LINKS_PER_FLOOR)
                        .forGetter(LinkSettings::maxLinksPerFloor),
                Codec.INT.optionalFieldOf("max_links_per_room", DEFAULT_MAX_LINKS_PER_ROOM)
                        .forGetter(LinkSettings::maxLinksPerRoom),
                Codec.INT.optionalFieldOf("max_link_length", DEFAULT_MAX_LINK_LENGTH)
                        .forGetter(LinkSettings::maxLinkLength),
                Codec.INT.optionalFieldOf("main_hallway_length", 0)
                        .forGetter(LinkSettings::mainHallwayLength),
                Codec.INT.optionalFieldOf("main_hallway_width", 0)
                        .forGetter(LinkSettings::mainHallwayWidth),
                Codec.INT.optionalFieldOf("branch_hallway_length", 0)
                        .forGetter(LinkSettings::branchHallwayLength),
                Codec.INT.optionalFieldOf("branch_hallway_width", 0)
                        .forGetter(LinkSettings::branchHallwayWidth),
                LinkGenerationSettings.CODEC.optionalFieldOf("generation", LinkGenerationSettings.DEFAULTS)
                        .forGetter(LinkSettings::generation)
        ).apply(instance, LinkSettings::new));

        private MKFloorLinkGenerationMode generationMode() {
            return generation.mode();
        }

        private float decay() {
            return generation.decay();
        }

        private int endpointIntactRadius() {
            return generation.endpointIntactRadius();
        }

        private float middleDecayBonus() {
            return generation.middleDecayBonus();
        }

        private Optional<String> insertFamily() {
            return generation.insertFamily();
        }

        private int insertDepth() {
            return generation.insertDepth();
        }

        private int insertSpacing() {
            return generation.insertSpacing();
        }

        private float insertProbability() {
            return generation.insertProbability();
        }

        private float insertMaxDecay() {
            return generation.insertMaxDecay();
        }
    }

    private record LinkGenerationSettings(
            MKFloorLinkGenerationMode mode,
            float decay,
            int endpointIntactRadius,
            float middleDecayBonus,
            Optional<String> insertFamily,
            int insertDepth,
            int insertSpacing,
            float insertProbability,
            float insertMaxDecay
    ) {
        private static final LinkGenerationSettings DEFAULTS = new LinkGenerationSettings(
                DEFAULT_LINK_GENERATION_MODE,
                DEFAULT_LINK_DECAY,
                DEFAULT_ENDPOINT_INTACT_RADIUS,
                DEFAULT_MIDDLE_DECAY_BONUS,
                Optional.empty(),
                DEFAULT_INSERT_DEPTH,
                DEFAULT_INSERT_SPACING,
                DEFAULT_INSERT_PROBABILITY,
                DEFAULT_INSERT_MAX_DECAY
        );

        private static final Codec<LinkGenerationSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKFloorLinkGenerationMode.CODEC.optionalFieldOf("mode",
                                DEFAULT_LINK_GENERATION_MODE)
                        .forGetter(LinkGenerationSettings::mode),
                Codec.FLOAT.optionalFieldOf("decay", DEFAULT_LINK_DECAY)
                        .forGetter(LinkGenerationSettings::decay),
                Codec.INT.optionalFieldOf("endpoint_intact_radius", DEFAULT_ENDPOINT_INTACT_RADIUS)
                        .forGetter(LinkGenerationSettings::endpointIntactRadius),
                Codec.FLOAT.optionalFieldOf("middle_decay_bonus", DEFAULT_MIDDLE_DECAY_BONUS)
                        .forGetter(LinkGenerationSettings::middleDecayBonus),
                Codec.STRING.optionalFieldOf("insert_family")
                        .forGetter(LinkGenerationSettings::insertFamily),
                Codec.INT.optionalFieldOf("insert_depth", DEFAULT_INSERT_DEPTH)
                        .forGetter(LinkGenerationSettings::insertDepth),
                Codec.INT.optionalFieldOf("insert_spacing", DEFAULT_INSERT_SPACING)
                        .forGetter(LinkGenerationSettings::insertSpacing),
                Codec.FLOAT.optionalFieldOf("insert_probability", DEFAULT_INSERT_PROBABILITY)
                        .forGetter(LinkGenerationSettings::insertProbability),
                Codec.FLOAT.optionalFieldOf("insert_max_decay", DEFAULT_INSERT_MAX_DECAY)
                        .forGetter(LinkGenerationSettings::insertMaxDecay)
        ).apply(instance, LinkGenerationSettings::new));
    }

    private record RoomProfiles(
            List<MKFloorRoomProfile> mainRoomProfiles,
            List<MKFloorRoomProfile> branchRoomProfiles,
            List<MKFloorRoomProfile> branchCapProfiles,
            List<MKFloorRoomProfile> mainCapApproachProfiles,
            List<MKFloorRoomProfile> mainCapProfiles
    ) {
        private static final Codec<RoomProfiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_room_profiles", List.of())
                        .forGetter(RoomProfiles::mainRoomProfiles),
                MKFloorRoomProfile.CODEC.listOf().optionalFieldOf("branch_room_profiles", List.of())
                        .forGetter(RoomProfiles::branchRoomProfiles),
                MKFloorRoomProfile.CODEC.listOf().optionalFieldOf("branch_cap_profiles", List.of())
                        .forGetter(RoomProfiles::branchCapProfiles),
                MKFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_cap_approach_profiles", List.of())
                        .forGetter(RoomProfiles::mainCapApproachProfiles),
                MKFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_cap_profiles", List.of())
                        .forGetter(RoomProfiles::mainCapProfiles)
        ).apply(instance, RoomProfiles::new));
    }

    private ScalarSettings scalarSettings() {
        return new ScalarSettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces, mainHallwaysEnabled,
                branchHallwaysEnabled, mainCapApproachEnabled, sprawl, lockedLayoutSeed, paletteOverride);
    }

    private LinkSettings linkSettings() {
        return new LinkSettings(linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth,
                new LinkGenerationSettings(linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus,
                        insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay));
    }

    private RoomProfiles roomProfiles() {
        return new RoomProfiles(mainRoomProfiles, branchRoomProfiles, branchCapProfiles,
                mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings {
        stackId = stackId == null || stackId.isBlank() ? "vertical_stack" : stackId;
        floorRole = floorRole == null || floorRole.isBlank() ? MKWorkspaceVerticalStackSlot.MAIN_FLOOR.suffix() : floorRole;
        minMainPathPieces = Math.max(0, minMainPathPieces);
        maxMainPathPieces = Math.max(minMainPathPieces, maxMainPathPieces);
        maxBranchPiecesBeforeCap = Math.max(0, Math.min(MAX_BRANCH_PIECES_BEFORE_CAP, maxBranchPiecesBeforeCap));
        hallwayLeadInMode = hallwayLeadInMode == null ? MKHallwayLeadInMode.AUTO : hallwayLeadInMode;
        manualHallwayLeadInPieces = Math.max(0,
                Math.min(MAX_MANUAL_HALLWAY_LEAD_IN_PIECES, manualHallwayLeadInPieces));
        sprawl = Math.max(0.0f, Math.min(1.0f, sprawl));
        linkDensity = Math.max(0.0f, Math.min(1.0f, linkDensity));
        maxLinksPerFloor = Math.max(0, Math.min(MAX_LINKS_PER_FLOOR, maxLinksPerFloor));
        maxLinksPerRoom = Math.max(0, Math.min(MAX_LINKS_PER_ROOM, maxLinksPerRoom));
        maxLinkLength = Math.max(0, Math.min(MAX_LINK_LENGTH, maxLinkLength));
        linkGenerationMode = linkGenerationMode == null ? DEFAULT_LINK_GENERATION_MODE : linkGenerationMode;
        linkDecay = Math.max(0.0f, Math.min(1.0f, linkDecay));
        endpointIntactRadius = Math.max(0, Math.min(MAX_ENDPOINT_INTACT_RADIUS, endpointIntactRadius));
        middleDecayBonus = Math.max(0.0f, Math.min(1.0f, middleDecayBonus));
        insertFamily = insertFamily == null ? Optional.empty() : insertFamily.filter(value -> !value.isBlank());
        insertDepth = Math.max(1, Math.min(MAX_INSERT_DEPTH, insertDepth));
        insertSpacing = Math.max(0, Math.min(MAX_INSERT_SPACING, insertSpacing));
        insertProbability = Math.max(0.0f, Math.min(1.0f, insertProbability));
        insertMaxDecay = Math.max(0.0f, Math.min(1.0f, insertMaxDecay));
        mainHallwayLength = Math.max(0, mainHallwayLength);
        mainHallwayWidth = Math.max(0, mainHallwayWidth);
        branchHallwayLength = Math.max(0, branchHallwayLength);
        branchHallwayWidth = Math.max(0, branchHallwayWidth);
        lockedLayoutSeed = lockedLayoutSeed == null ? Optional.empty() : lockedLayoutSeed;
        paletteOverride = paletteOverride == null ? Optional.empty() : paletteOverride.filter(override -> !override.isEmpty());
        mainRoomProfiles = normalizeProfiles(mainRoomProfiles, MKFloorRoomKind.MAIN_ROOM);
        branchRoomProfiles = normalizeProfiles(branchRoomProfiles, MKFloorRoomKind.BRANCH_ROOM);
        branchCapProfiles = normalizeProfiles(branchCapProfiles, MKFloorRoomKind.BRANCH_CAP);
        mainCapApproachProfiles = normalizeProfiles(mainCapApproachProfiles, MKFloorRoomKind.MAIN_CAP_APPROACH);
        mainCapProfiles = normalizeProfiles(mainCapProfiles, MKFloorRoomKind.MAIN_CAP);
    }

    public MKFloorTopologySettings(String stackId,
                                            String floorRole,
                                            int minMainPathPieces,
                                            int maxMainPathPieces,
                                            int maxBranchPiecesBeforeCap,
                                            MKHallwayLeadInMode hallwayLeadInMode,
                                            int manualHallwayLeadInPieces,
                                            boolean mainHallwaysEnabled,
                                            boolean branchHallwaysEnabled,
                                            boolean mainCapApproachEnabled,
                                            float sprawl,
                                            boolean linksEnabled,
                                            float linkDensity,
                                            int maxLinksPerFloor,
                                            int maxLinksPerRoom,
                                            int maxLinkLength,
                                            Optional<Long> lockedLayoutSeed,
                                            Optional<MKWorkspacePaletteOverride> paletteOverride,
                                            List<MKFloorRoomProfile> mainRoomProfiles,
                                            List<MKFloorRoomProfile> branchRoomProfiles,
                                            List<MKFloorRoomProfile> branchCapProfiles,
                                            List<MKFloorRoomProfile> mainCapApproachProfiles,
                                            List<MKFloorRoomProfile> mainCapProfiles) {
        this(stackId, floorRole, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                hallwayLeadInMode, manualHallwayLeadInPieces, mainHallwaysEnabled, branchHallwaysEnabled,
                mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom,
                maxLinkLength, DEFAULT_LINK_GENERATION_MODE, DEFAULT_LINK_DECAY, DEFAULT_ENDPOINT_INTACT_RADIUS,
                DEFAULT_MIDDLE_DECAY_BONUS, Optional.empty(), DEFAULT_INSERT_DEPTH, DEFAULT_INSERT_SPACING,
                DEFAULT_INSERT_PROBABILITY, DEFAULT_INSERT_MAX_DECAY, 0, 0, 0, 0, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings(String stackId,
                                            String floorRole,
                                            int minMainPathPieces,
                                            int maxMainPathPieces,
                                            int maxBranchPiecesBeforeCap,
                                            MKHallwayLeadInMode hallwayLeadInMode,
                                            int manualHallwayLeadInPieces,
                                            boolean mainHallwaysEnabled,
                                            boolean branchHallwaysEnabled,
                                            boolean mainCapApproachEnabled,
                                            float sprawl,
                                            Optional<Long> lockedLayoutSeed,
                                            List<MKFloorRoomProfile> mainRoomProfiles,
                                            List<MKFloorRoomProfile> branchRoomProfiles,
                                            List<MKFloorRoomProfile> branchCapProfiles,
                                            List<MKFloorRoomProfile> mainCapApproachProfiles,
                                            List<MKFloorRoomProfile> mainCapProfiles) {
        this(stackId, floorRole, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                hallwayLeadInMode, manualHallwayLeadInPieces, mainHallwaysEnabled, branchHallwaysEnabled,
                mainCapApproachEnabled, sprawl, DEFAULT_LINKS_ENABLED, DEFAULT_LINK_DENSITY,
                DEFAULT_MAX_LINKS_PER_FLOOR, DEFAULT_MAX_LINKS_PER_ROOM, DEFAULT_MAX_LINK_LENGTH,
                DEFAULT_LINK_GENERATION_MODE, DEFAULT_LINK_DECAY, DEFAULT_ENDPOINT_INTACT_RADIUS,
                DEFAULT_MIDDLE_DECAY_BONUS, Optional.empty(), DEFAULT_INSERT_DEPTH, DEFAULT_INSERT_SPACING,
                DEFAULT_INSERT_PROBABILITY, DEFAULT_INSERT_MAX_DECAY, 0, 0, 0, 0, lockedLayoutSeed, Optional.empty(),
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public String key() {
        return key(stackId, floorRole);
    }

    public static String key(String stackId, String floorRole) {
        return stackId + "." + floorRole;
    }

    public static MKFloorTopologySettings defaults(MKWorkspaceVerticalStackSettings stackSettings,
                                                            String floorRole) {
        int height = heightForFloorRole(stackSettings, floorRole);
        return new MKFloorTopologySettings(
                stackSettings.stackId(),
                floorRole,
                DEFAULT_MIN_MAIN_PATH_PIECES,
                DEFAULT_MAX_MAIN_PATH_PIECES,
                DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP,
                MKHallwayLeadInMode.AUTO,
                1,
                true,
                true,
                false,
                DEFAULT_SPRAWL,
                DEFAULT_LINKS_ENABLED,
                DEFAULT_LINK_DENSITY,
                DEFAULT_MAX_LINKS_PER_FLOOR,
                DEFAULT_MAX_LINKS_PER_ROOM,
                DEFAULT_MAX_LINK_LENGTH,
                Optional.empty(),
                Optional.empty(),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_ROOM,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_ROOM,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.BRANCH_CAP,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP_APPROACH,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKFloorRoomProfile.defaults(MKFloorRoomKind.MAIN_CAP,
                        stackSettings.width(), stackSettings.length(), height))
        );
    }

    public static List<MKFloorTopologySettings> defaults(List<MKWorkspaceVerticalStackSettings> stackSettings) {
        ArrayList<MKFloorTopologySettings> defaults = new ArrayList<>();
        for (MKWorkspaceVerticalStackSettings settings : stackSettings) {
            for (MKWorkspaceVerticalStackSlot slot : MKWorkspaceVerticalStackSlot.floorTopologyOrder()) {
                if (!MKWorkspaceVerticalStackSlot.supportsFloorTopology(settings, slot)) {
                    continue;
                }
                defaults.add(defaults(settings, slot.suffix()));
            }
        }
        return List.copyOf(defaults);
    }

    public static List<MKFloorTopologySettings> normalize(
            List<MKFloorTopologySettings> currentSettings,
            List<MKWorkspaceVerticalStackSettings> stackSettings) {
        Map<String, MKFloorTopologySettings> byKey = new LinkedHashMap<>();
        for (MKFloorTopologySettings defaultSettings : defaults(stackSettings)) {
            byKey.put(defaultSettings.key(), defaultSettings);
        }
        if (currentSettings != null) {
            for (MKFloorTopologySettings settings : currentSettings) {
                String key = settings.key();
                if (byKey.containsKey(key)) {
                    byKey.put(key, settings.withDefaultProfilesFor(byKey.get(key)));
                }
            }
        }
        return List.copyOf(byKey.values());
    }

    public static Optional<MKFloorTopologySettings> find(
            List<MKFloorTopologySettings> settings, String stackId, String floorRole) {
        String key = key(stackId, floorRole);
        return settings.stream()
                .filter(setting -> setting.key().equals(key))
                .findFirst();
    }

    public static MKFloorTopologySettings fromPlannerSettingsEntry(MKWorkspacePlannerSettingsEntry entry) {
        MKFloorTopologySettings settings = MKWorkspaceCodecs.parseNbt(CODEC, entry.settings(),
                "workspace floor topology settings");
        return settings.withPaletteOverride(entry.paletteOverride());
    }

    public MKWorkspacePlannerSettingsEntry plannerSettingsEntry() {
        MKFloorTopologySettings settingsPayload = withPaletteOverride(Optional.empty());
        return new MKWorkspacePlannerSettingsEntry(
                PLANNER_ID,
                key(),
                paletteOverride,
                MKWorkspaceCodecs.encodeNbt(CODEC, settingsPayload, "workspace floor topology settings"));
    }

    public MKFloorTopologySettings withMinMainPathPieces(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, value, Math.max(value, maxMainPathPieces),
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMaxMainPathPieces(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, Math.min(minMainPathPieces, value), value,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMaxBranchPiecesBeforeCap(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                value, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withHallwayLeadInMode(MKHallwayLeadInMode value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, value, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withManualHallwayLeadInPieces(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, value,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMainHallwaysEnabled(boolean value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                value, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withBranchHallwaysEnabled(boolean value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, value, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMainCapApproachEnabled(boolean value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, value, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withSprawl(float value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, value, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withLinksEnabled(boolean value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, value, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withLinkDensity(float value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, value,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMaxLinksPerFloor(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                value, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMaxLinksPerRoom(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, value, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMaxLinkLength(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, value,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withLinkGenerationMode(MKFloorLinkGenerationMode value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                value, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing,
                insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withLinkDecay(float value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, value, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withEndpointIntactRadius(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, value, middleDecayBonus, insertFamily, insertDepth, insertSpacing,
                insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withMiddleDecayBonus(float value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, value, insertFamily, insertDepth, insertSpacing,
                insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withInsertFamily(Optional<String> value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus,
                value == null ? Optional.empty() : value, insertDepth, insertSpacing, insertProbability,
                insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withInsertDepth(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, value,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withInsertSpacing(int value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                value, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withInsertProbability(float value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, value, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withInsertMaxDecay(float value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, value,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withLockedLayoutSeed(Optional<Long> value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth,
                value == null ? Optional.empty() : value,
                paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withPaletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed,
                value == null ? Optional.empty() : value,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withRoomProfile(MKFloorRoomKind kind, int index,
                                                            MKFloorRoomProfile profile) {
        List<MKFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKFloorRoomProfile> branchProfiles = branchRoomProfiles;
        List<MKFloorRoomProfile> branchCaps = branchCapProfiles;
        List<MKFloorRoomProfile> mainCapApproach = mainCapApproachProfiles;
        List<MKFloorRoomProfile> mainCaps = mainCapProfiles;
        switch (kind) {
            case MAIN_ROOM -> mainProfiles = replaceProfile(mainRoomProfiles, index, profile);
            case BRANCH_ROOM -> branchProfiles = replaceProfile(branchRoomProfiles, index, profile);
            case BRANCH_CAP -> branchCaps = replaceProfile(branchCapProfiles, index, profile);
            case MAIN_CAP_APPROACH -> mainCapApproach = replaceProfile(mainCapApproachProfiles, index, profile);
            case MAIN_CAP -> mainCaps = replaceProfile(mainCapProfiles, index, profile);
        }
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainProfiles, branchProfiles, branchCaps, mainCapApproach, mainCaps);
    }

    public MKFloorTopologySettings withAddedRoomProfile(MKFloorRoomKind kind,
                                                                 MKFloorRoomProfile profile) {
        List<MKFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKFloorRoomProfile> branchProfiles = branchRoomProfiles;
        List<MKFloorRoomProfile> branchCaps = branchCapProfiles;
        List<MKFloorRoomProfile> mainCapApproach = mainCapApproachProfiles;
        List<MKFloorRoomProfile> mainCaps = mainCapProfiles;
        switch (kind) {
            case MAIN_ROOM -> mainProfiles = addProfile(mainRoomProfiles, profile, kind);
            case BRANCH_ROOM -> branchProfiles = addProfile(branchRoomProfiles, profile, kind);
            case BRANCH_CAP -> branchCaps = addProfile(branchCapProfiles, profile, kind);
            case MAIN_CAP_APPROACH -> mainCapApproach = addProfile(mainCapApproachProfiles, profile, kind);
            case MAIN_CAP -> mainCaps = addProfile(mainCapProfiles, profile, kind);
        }
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainProfiles, branchProfiles, branchCaps, mainCapApproach, mainCaps);
    }

    public MKFloorTopologySettings withRemovedRoomProfile(MKFloorRoomKind kind, int index) {
        List<MKFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKFloorRoomProfile> branchProfiles = branchRoomProfiles;
        List<MKFloorRoomProfile> branchCaps = branchCapProfiles;
        List<MKFloorRoomProfile> mainCapApproach = mainCapApproachProfiles;
        List<MKFloorRoomProfile> mainCaps = mainCapProfiles;
        switch (kind) {
            case MAIN_ROOM -> mainProfiles = removeProfile(mainRoomProfiles, index);
            case BRANCH_ROOM -> branchProfiles = removeProfile(branchRoomProfiles, index);
            case BRANCH_CAP -> branchCaps = removeProfile(branchCapProfiles, index);
            case MAIN_CAP_APPROACH -> mainCapApproach = removeProfile(mainCapApproachProfiles, index);
            case MAIN_CAP -> mainCaps = removeProfile(mainCapProfiles, index);
        }
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainProfiles, branchProfiles, branchCaps, mainCapApproach, mainCaps);
    }

    public MKFloorTopologySettings withLayoutHallwayFootprints(int mainLength, int mainWidth,
                                                                        int branchLength, int branchWidth) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled,
                linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainLength, mainWidth, branchLength, branchWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKFloorTopologySettings withPhysicalFootprintPadding(int horizontalPadding) {
        int padding = Math.max(0, horizontalPadding);
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled,
                linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth,
                lockedLayoutSeed, paletteOverride,
                physicalProfiles(mainRoomProfiles, padding),
                physicalProfiles(branchRoomProfiles, padding),
                physicalProfiles(branchCapProfiles, padding),
                physicalProfiles(mainCapApproachProfiles, padding),
                physicalProfiles(mainCapProfiles, padding));
    }

    public MKFloorTopologySettings withRoomProfiles(
            List<MKFloorRoomProfile> mainRoomProfiles,
            List<MKFloorRoomProfile> branchRoomProfiles,
            List<MKFloorRoomProfile> branchCapProfiles,
            List<MKFloorRoomProfile> mainCapApproachProfiles,
            List<MKFloorRoomProfile> mainCapProfiles) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled,
                linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth,
                insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth,
                lockedLayoutSeed, paletteOverride,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    private MKFloorTopologySettings withDefaultProfilesFor(MKFloorTopologySettings defaults) {
        return new MKFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                linkGenerationMode, linkDecay, endpointIntactRadius, middleDecayBonus, insertFamily, insertDepth, insertSpacing, insertProbability, insertMaxDecay,
                mainHallwayLength, mainHallwayWidth, branchHallwayLength, branchHallwayWidth, lockedLayoutSeed, paletteOverride,
                mainRoomProfiles.isEmpty() ? defaults.mainRoomProfiles() : mainRoomProfiles,
                branchRoomProfiles.isEmpty() ? defaults.branchRoomProfiles() : branchRoomProfiles,
                branchCapProfiles.isEmpty() ? defaults.branchCapProfiles() : branchCapProfiles,
                mainCapApproachProfiles.isEmpty() ? defaults.mainCapApproachProfiles() : mainCapApproachProfiles,
                mainCapProfiles.isEmpty() ? defaults.mainCapProfiles() : mainCapProfiles);
    }

    private static List<MKFloorRoomProfile> physicalProfiles(List<MKFloorRoomProfile> profiles,
                                                                      int horizontalPadding) {
        return profiles.stream()
                .map(profile -> profile.withWidth(profile.width() + horizontalPadding)
                        .withLength(profile.length() + horizontalPadding))
                .toList();
    }

    private static List<MKFloorRoomProfile> normalizeProfiles(
            List<MKFloorRoomProfile> profiles, MKFloorRoomKind expectedKind) {
        if (profiles == null) {
            return List.of();
        }
        return profiles.stream()
                .filter(profile -> profile.kind() == expectedKind)
                .toList();
    }

    private static List<MKFloorRoomProfile> replaceProfile(List<MKFloorRoomProfile> profiles,
                                                                    int index,
                                                                    MKFloorRoomProfile profile) {
        if (profile == null || index < 0 || index >= profiles.size()) {
            return profiles;
        }
        ArrayList<MKFloorRoomProfile> updated = new ArrayList<>(profiles);
        updated.set(index, profile);
        return List.copyOf(updated);
    }

    private static List<MKFloorRoomProfile> addProfile(List<MKFloorRoomProfile> profiles,
                                                                MKFloorRoomProfile profile,
                                                                MKFloorRoomKind kind) {
        if (profile == null || profile.kind() != kind) {
            return profiles;
        }
        ArrayList<MKFloorRoomProfile> updated = new ArrayList<>(profiles);
        updated.add(profile);
        return List.copyOf(updated);
    }

    private static List<MKFloorRoomProfile> removeProfile(List<MKFloorRoomProfile> profiles,
                                                                   int index) {
        if (profiles.size() <= 1 || index < 0 || index >= profiles.size()) {
            return profiles;
        }
        ArrayList<MKFloorRoomProfile> updated = new ArrayList<>(profiles);
        updated.remove(index);
        return List.copyOf(updated);
    }

    private static int heightForFloorRole(MKWorkspaceVerticalStackSettings stackSettings, String floorRole) {
        MKWorkspaceVerticalStackBudget heights = stackSettings.heights();
        return switch (floorRole) {
            case "main_floor" -> heights.mainFloorHeight();
            case "basement_floor" -> heights.basementFloorHeight();
            case "basement_entry" -> heights.basementEntryHeight();
            case "basement_cap", "basement_cap_approach" -> heights.basementCapHeight();
            case "top_cap", "top_cap_approach" -> heights.topCapHeight();
            default -> heights.entryHeight();
        };
    }
}
