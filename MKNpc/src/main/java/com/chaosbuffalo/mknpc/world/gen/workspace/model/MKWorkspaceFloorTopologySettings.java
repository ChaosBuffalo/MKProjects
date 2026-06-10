package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKWorkspaceFloorTopologySettings(
        String stackId,
        String floorRole,
        int minMainPathPieces,
        int maxMainPathPieces,
        int maxBranchPiecesBeforeCap,
        MKWorkspaceHallwayLeadInMode hallwayLeadInMode,
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
        List<MKWorkspaceFloorRoomProfile> mainRoomProfiles,
        List<MKWorkspaceFloorRoomProfile> branchRoomProfiles,
        List<MKWorkspaceFloorRoomProfile> branchCapProfiles,
        List<MKWorkspaceFloorRoomProfile> mainCapApproachProfiles,
        List<MKWorkspaceFloorRoomProfile> mainCapProfiles
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
    public static final int DEFAULT_MAX_LINKS_PER_ROOM = 1;
    public static final int DEFAULT_MAX_LINK_LENGTH = 32;
    public static final int MAX_LINKS_PER_FLOOR = 64;
    public static final int MAX_LINKS_PER_ROOM = 16;
    public static final int MAX_LINK_LENGTH = 128;

    public static final Codec<MKWorkspaceFloorTopologySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScalarSettings.CODEC.fieldOf("settings").forGetter(MKWorkspaceFloorTopologySettings::scalarSettings),
            LinkSettings.CODEC.optionalFieldOf("links", LinkSettings.DEFAULTS)
                    .forGetter(MKWorkspaceFloorTopologySettings::linkSettings),
            RoomProfiles.CODEC.fieldOf("profiles").forGetter(MKWorkspaceFloorTopologySettings::roomProfiles)
    ).apply(instance, (settings, links, profiles) -> new MKWorkspaceFloorTopologySettings(
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
            settings.lockedLayoutSeed(),
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
            MKWorkspaceHallwayLeadInMode hallwayLeadInMode,
            int manualHallwayLeadInPieces,
            boolean mainHallwaysEnabled,
            boolean branchHallwaysEnabled,
            boolean mainCapApproachEnabled,
            float sprawl,
            Optional<Long> lockedLayoutSeed
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
                MKWorkspaceHallwayLeadInMode.CODEC.optionalFieldOf("hallway_lead_in_mode",
                                MKWorkspaceHallwayLeadInMode.AUTO)
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
                        .forGetter(ScalarSettings::lockedLayoutSeed)
        ).apply(instance, ScalarSettings::new));
    }

    private record LinkSettings(
            boolean enabled,
            float density,
            int maxLinksPerFloor,
            int maxLinksPerRoom,
            int maxLinkLength
    ) {
        private static final LinkSettings DEFAULTS = new LinkSettings(
                DEFAULT_LINKS_ENABLED,
                DEFAULT_LINK_DENSITY,
                DEFAULT_MAX_LINKS_PER_FLOOR,
                DEFAULT_MAX_LINKS_PER_ROOM,
                DEFAULT_MAX_LINK_LENGTH
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
                        .forGetter(LinkSettings::maxLinkLength)
        ).apply(instance, LinkSettings::new));
    }

    private record RoomProfiles(
            List<MKWorkspaceFloorRoomProfile> mainRoomProfiles,
            List<MKWorkspaceFloorRoomProfile> branchRoomProfiles,
            List<MKWorkspaceFloorRoomProfile> branchCapProfiles,
            List<MKWorkspaceFloorRoomProfile> mainCapApproachProfiles,
            List<MKWorkspaceFloorRoomProfile> mainCapProfiles
    ) {
        private static final Codec<RoomProfiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_room_profiles", List.of())
                        .forGetter(RoomProfiles::mainRoomProfiles),
                MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("branch_room_profiles", List.of())
                        .forGetter(RoomProfiles::branchRoomProfiles),
                MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("branch_cap_profiles", List.of())
                        .forGetter(RoomProfiles::branchCapProfiles),
                MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_cap_approach_profiles", List.of())
                        .forGetter(RoomProfiles::mainCapApproachProfiles),
                MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_cap_profiles", List.of())
                        .forGetter(RoomProfiles::mainCapProfiles)
        ).apply(instance, RoomProfiles::new));
    }

    private ScalarSettings scalarSettings() {
        return new ScalarSettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces, mainHallwaysEnabled,
                branchHallwaysEnabled, mainCapApproachEnabled, sprawl, lockedLayoutSeed);
    }

    private LinkSettings linkSettings() {
        return new LinkSettings(linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength);
    }

    private RoomProfiles roomProfiles() {
        return new RoomProfiles(mainRoomProfiles, branchRoomProfiles, branchCapProfiles,
                mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings {
        stackId = stackId == null || stackId.isBlank() ? "tower.primary" : stackId;
        floorRole = floorRole == null || floorRole.isBlank() ? MKTowerWorkspaceStackSlot.MAIN_FLOOR.suffix() : floorRole;
        minMainPathPieces = Math.max(0, minMainPathPieces);
        maxMainPathPieces = Math.max(minMainPathPieces, maxMainPathPieces);
        maxBranchPiecesBeforeCap = Math.max(0, Math.min(MAX_BRANCH_PIECES_BEFORE_CAP, maxBranchPiecesBeforeCap));
        hallwayLeadInMode = hallwayLeadInMode == null ? MKWorkspaceHallwayLeadInMode.AUTO : hallwayLeadInMode;
        manualHallwayLeadInPieces = Math.max(0,
                Math.min(MAX_MANUAL_HALLWAY_LEAD_IN_PIECES, manualHallwayLeadInPieces));
        sprawl = Math.max(0.0f, Math.min(1.0f, sprawl));
        linkDensity = Math.max(0.0f, Math.min(1.0f, linkDensity));
        maxLinksPerFloor = Math.max(0, Math.min(MAX_LINKS_PER_FLOOR, maxLinksPerFloor));
        maxLinksPerRoom = Math.max(0, Math.min(MAX_LINKS_PER_ROOM, maxLinksPerRoom));
        maxLinkLength = Math.max(0, Math.min(MAX_LINK_LENGTH, maxLinkLength));
        lockedLayoutSeed = lockedLayoutSeed == null ? Optional.empty() : lockedLayoutSeed;
        mainRoomProfiles = normalizeProfiles(mainRoomProfiles, MKWorkspaceFloorRoomKind.MAIN_ROOM);
        branchRoomProfiles = normalizeProfiles(branchRoomProfiles, MKWorkspaceFloorRoomKind.BRANCH_ROOM);
        branchCapProfiles = normalizeProfiles(branchCapProfiles, MKWorkspaceFloorRoomKind.BRANCH_CAP);
        mainCapApproachProfiles = normalizeProfiles(mainCapApproachProfiles, MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH);
        mainCapProfiles = normalizeProfiles(mainCapProfiles, MKWorkspaceFloorRoomKind.MAIN_CAP);
    }

    public MKWorkspaceFloorTopologySettings(String stackId,
                                            String floorRole,
                                            int minMainPathPieces,
                                            int maxMainPathPieces,
                                            int maxBranchPiecesBeforeCap,
                                            MKWorkspaceHallwayLeadInMode hallwayLeadInMode,
                                            int manualHallwayLeadInPieces,
                                            boolean mainHallwaysEnabled,
                                            boolean branchHallwaysEnabled,
                                            boolean mainCapApproachEnabled,
                                            float sprawl,
                                            Optional<Long> lockedLayoutSeed,
                                            List<MKWorkspaceFloorRoomProfile> mainRoomProfiles,
                                            List<MKWorkspaceFloorRoomProfile> branchRoomProfiles,
                                            List<MKWorkspaceFloorRoomProfile> branchCapProfiles,
                                            List<MKWorkspaceFloorRoomProfile> mainCapApproachProfiles,
                                            List<MKWorkspaceFloorRoomProfile> mainCapProfiles) {
        this(stackId, floorRole, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                hallwayLeadInMode, manualHallwayLeadInPieces, mainHallwaysEnabled, branchHallwaysEnabled,
                mainCapApproachEnabled, sprawl, DEFAULT_LINKS_ENABLED, DEFAULT_LINK_DENSITY,
                DEFAULT_MAX_LINKS_PER_FLOOR, DEFAULT_MAX_LINKS_PER_ROOM, DEFAULT_MAX_LINK_LENGTH, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public String key() {
        return key(stackId, floorRole);
    }

    public static String key(String stackId, String floorRole) {
        return stackId + "." + floorRole;
    }

    public static MKWorkspaceFloorTopologySettings defaults(MKWorkspaceTowerStackSettings stackSettings,
                                                            String floorRole) {
        int height = heightForFloorRole(stackSettings, floorRole);
        return new MKWorkspaceFloorTopologySettings(
                stackSettings.stackId(),
                floorRole,
                DEFAULT_MIN_MAIN_PATH_PIECES,
                DEFAULT_MAX_MAIN_PATH_PIECES,
                DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP,
                MKWorkspaceHallwayLeadInMode.AUTO,
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
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_ROOM,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_ROOM,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_CAP,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_CAP,
                        stackSettings.width(), stackSettings.length(), height))
        );
    }

    public static List<MKWorkspaceFloorTopologySettings> defaults(List<MKWorkspaceTowerStackSettings> stackSettings) {
        ArrayList<MKWorkspaceFloorTopologySettings> defaults = new ArrayList<>();
        for (MKWorkspaceTowerStackSettings settings : stackSettings) {
            for (MKTowerWorkspaceStackSlot slot : MKTowerWorkspaceStackSlot.familyDefaultOrder()) {
                defaults.add(defaults(settings, slot.suffix()));
            }
        }
        return List.copyOf(defaults);
    }

    public static List<MKWorkspaceFloorTopologySettings> normalize(
            List<MKWorkspaceFloorTopologySettings> currentSettings,
            List<MKWorkspaceTowerStackSettings> stackSettings) {
        Map<String, MKWorkspaceFloorTopologySettings> byKey = new LinkedHashMap<>();
        for (MKWorkspaceFloorTopologySettings defaultSettings : defaults(stackSettings)) {
            byKey.put(defaultSettings.key(), defaultSettings);
        }
        if (currentSettings != null) {
            for (MKWorkspaceFloorTopologySettings settings : currentSettings) {
                String key = settings.key();
                if (byKey.containsKey(key)) {
                    byKey.put(key, settings.withDefaultProfilesFor(byKey.get(key)));
                }
            }
        }
        return List.copyOf(byKey.values());
    }

    public static Optional<MKWorkspaceFloorTopologySettings> find(
            List<MKWorkspaceFloorTopologySettings> settings, String stackId, String floorRole) {
        String key = key(stackId, floorRole);
        return settings.stream()
                .filter(setting -> setting.key().equals(key))
                .findFirst();
    }

    public MKWorkspaceFloorTopologySettings withMinMainPathPieces(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, value, Math.max(value, maxMainPathPieces),
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxMainPathPieces(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, Math.min(minMainPathPieces, value), value,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxBranchPiecesBeforeCap(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                value, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withHallwayLeadInMode(MKWorkspaceHallwayLeadInMode value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, value, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withManualHallwayLeadInPieces(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, value,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMainHallwaysEnabled(boolean value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                value, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withBranchHallwaysEnabled(boolean value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, value, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMainCapApproachEnabled(boolean value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, value, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withSprawl(float value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, value, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withLinksEnabled(boolean value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, value, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withLinkDensity(float value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, value,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxLinksPerFloor(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                value, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxLinksPerRoom(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, value, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxLinkLength(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, value, lockedLayoutSeed,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withLockedLayoutSeed(Optional<Long> value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity,
                maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                value == null ? Optional.empty() : value,
                mainRoomProfiles, branchRoomProfiles, branchCapProfiles, mainCapApproachProfiles, mainCapProfiles);
    }

    public MKWorkspaceFloorTopologySettings withRoomProfile(MKWorkspaceFloorRoomKind kind, int index,
                                                            MKWorkspaceFloorRoomProfile profile) {
        List<MKWorkspaceFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchProfiles = branchRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchCaps = branchCapProfiles;
        List<MKWorkspaceFloorRoomProfile> mainCapApproach = mainCapApproachProfiles;
        List<MKWorkspaceFloorRoomProfile> mainCaps = mainCapProfiles;
        switch (kind) {
            case MAIN_ROOM -> mainProfiles = replaceProfile(mainRoomProfiles, index, profile);
            case BRANCH_ROOM -> branchProfiles = replaceProfile(branchRoomProfiles, index, profile);
            case BRANCH_CAP -> branchCaps = replaceProfile(branchCapProfiles, index, profile);
            case MAIN_CAP_APPROACH -> mainCapApproach = replaceProfile(mainCapApproachProfiles, index, profile);
            case MAIN_CAP -> mainCaps = replaceProfile(mainCapProfiles, index, profile);
        }
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainProfiles, branchProfiles, branchCaps, mainCapApproach, mainCaps);
    }

    public MKWorkspaceFloorTopologySettings withAddedRoomProfile(MKWorkspaceFloorRoomKind kind,
                                                                 MKWorkspaceFloorRoomProfile profile) {
        List<MKWorkspaceFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchProfiles = branchRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchCaps = branchCapProfiles;
        List<MKWorkspaceFloorRoomProfile> mainCapApproach = mainCapApproachProfiles;
        List<MKWorkspaceFloorRoomProfile> mainCaps = mainCapProfiles;
        switch (kind) {
            case MAIN_ROOM -> mainProfiles = addProfile(mainRoomProfiles, profile, kind);
            case BRANCH_ROOM -> branchProfiles = addProfile(branchRoomProfiles, profile, kind);
            case BRANCH_CAP -> branchCaps = addProfile(branchCapProfiles, profile, kind);
            case MAIN_CAP_APPROACH -> mainCapApproach = addProfile(mainCapApproachProfiles, profile, kind);
            case MAIN_CAP -> mainCaps = addProfile(mainCapProfiles, profile, kind);
        }
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainProfiles, branchProfiles, branchCaps, mainCapApproach, mainCaps);
    }

    public MKWorkspaceFloorTopologySettings withRemovedRoomProfile(MKWorkspaceFloorRoomKind kind, int index) {
        List<MKWorkspaceFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchProfiles = branchRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchCaps = branchCapProfiles;
        List<MKWorkspaceFloorRoomProfile> mainCapApproach = mainCapApproachProfiles;
        List<MKWorkspaceFloorRoomProfile> mainCaps = mainCapProfiles;
        switch (kind) {
            case MAIN_ROOM -> mainProfiles = removeProfile(mainRoomProfiles, index);
            case BRANCH_ROOM -> branchProfiles = removeProfile(branchRoomProfiles, index);
            case BRANCH_CAP -> branchCaps = removeProfile(branchCapProfiles, index);
            case MAIN_CAP_APPROACH -> mainCapApproach = removeProfile(mainCapApproachProfiles, index);
            case MAIN_CAP -> mainCaps = removeProfile(mainCapProfiles, index);
        }
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainProfiles, branchProfiles, branchCaps, mainCapApproach, mainCaps);
    }

    private MKWorkspaceFloorTopologySettings withDefaultProfilesFor(MKWorkspaceFloorTopologySettings defaults) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainHallwaysEnabled, branchHallwaysEnabled, mainCapApproachEnabled, sprawl, linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                mainRoomProfiles.isEmpty() ? defaults.mainRoomProfiles() : mainRoomProfiles,
                branchRoomProfiles.isEmpty() ? defaults.branchRoomProfiles() : branchRoomProfiles,
                branchCapProfiles.isEmpty() ? defaults.branchCapProfiles() : branchCapProfiles,
                mainCapApproachProfiles.isEmpty() ? defaults.mainCapApproachProfiles() : mainCapApproachProfiles,
                mainCapProfiles.isEmpty() ? defaults.mainCapProfiles() : mainCapProfiles);
    }

    private static List<MKWorkspaceFloorRoomProfile> normalizeProfiles(
            List<MKWorkspaceFloorRoomProfile> profiles, MKWorkspaceFloorRoomKind expectedKind) {
        if (profiles == null) {
            return List.of();
        }
        return profiles.stream()
                .filter(profile -> profile.kind() == expectedKind)
                .toList();
    }

    private static List<MKWorkspaceFloorRoomProfile> replaceProfile(List<MKWorkspaceFloorRoomProfile> profiles,
                                                                    int index,
                                                                    MKWorkspaceFloorRoomProfile profile) {
        if (profile == null || index < 0 || index >= profiles.size()) {
            return profiles;
        }
        ArrayList<MKWorkspaceFloorRoomProfile> updated = new ArrayList<>(profiles);
        updated.set(index, profile);
        return List.copyOf(updated);
    }

    private static List<MKWorkspaceFloorRoomProfile> addProfile(List<MKWorkspaceFloorRoomProfile> profiles,
                                                                MKWorkspaceFloorRoomProfile profile,
                                                                MKWorkspaceFloorRoomKind kind) {
        if (profile == null || profile.kind() != kind) {
            return profiles;
        }
        ArrayList<MKWorkspaceFloorRoomProfile> updated = new ArrayList<>(profiles);
        updated.add(profile);
        return List.copyOf(updated);
    }

    private static List<MKWorkspaceFloorRoomProfile> removeProfile(List<MKWorkspaceFloorRoomProfile> profiles,
                                                                   int index) {
        if (profiles.size() <= 1 || index < 0 || index >= profiles.size()) {
            return profiles;
        }
        ArrayList<MKWorkspaceFloorRoomProfile> updated = new ArrayList<>(profiles);
        updated.remove(index);
        return List.copyOf(updated);
    }

    private static int heightForFloorRole(MKWorkspaceTowerStackSettings stackSettings, String floorRole) {
        MKTowerStackBudget heights = stackSettings.heights();
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
