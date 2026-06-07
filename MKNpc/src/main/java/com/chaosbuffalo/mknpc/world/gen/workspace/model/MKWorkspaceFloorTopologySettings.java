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
        List<MKWorkspaceFloorRoomProfile> mainRoomProfiles,
        List<MKWorkspaceFloorRoomProfile> branchRoomProfiles
) {
    public static final int DEFAULT_MIN_MAIN_PATH_PIECES = 1;
    public static final int DEFAULT_MAX_MAIN_PATH_PIECES = 2;
    public static final int DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP = 10;
    public static final int MAX_BRANCH_PIECES_BEFORE_CAP = DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP;

    public static final Codec<MKWorkspaceFloorTopologySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stack_id").forGetter(MKWorkspaceFloorTopologySettings::stackId),
            Codec.STRING.fieldOf("floor_role").forGetter(MKWorkspaceFloorTopologySettings::floorRole),
            Codec.INT.optionalFieldOf("min_main_path_pieces", DEFAULT_MIN_MAIN_PATH_PIECES)
                    .forGetter(MKWorkspaceFloorTopologySettings::minMainPathPieces),
            Codec.INT.optionalFieldOf("max_main_path_pieces", DEFAULT_MAX_MAIN_PATH_PIECES)
                    .forGetter(MKWorkspaceFloorTopologySettings::maxMainPathPieces),
            Codec.INT.optionalFieldOf("max_branch_pieces_before_cap", DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP)
                    .forGetter(MKWorkspaceFloorTopologySettings::maxBranchPiecesBeforeCap),
            MKWorkspaceHallwayLeadInMode.CODEC.optionalFieldOf("hallway_lead_in_mode",
                            MKWorkspaceHallwayLeadInMode.AUTO)
                    .forGetter(MKWorkspaceFloorTopologySettings::hallwayLeadInMode),
            Codec.INT.optionalFieldOf("manual_hallway_lead_in_pieces", 1)
                    .forGetter(MKWorkspaceFloorTopologySettings::manualHallwayLeadInPieces),
            MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("main_room_profiles", List.of())
                    .forGetter(MKWorkspaceFloorTopologySettings::mainRoomProfiles),
            MKWorkspaceFloorRoomProfile.CODEC.listOf().optionalFieldOf("branch_room_profiles", List.of())
                    .forGetter(MKWorkspaceFloorTopologySettings::branchRoomProfiles)
    ).apply(instance, MKWorkspaceFloorTopologySettings::new));

    public MKWorkspaceFloorTopologySettings {
        stackId = stackId == null || stackId.isBlank() ? "tower.primary" : stackId;
        floorRole = floorRole == null || floorRole.isBlank() ? MKTowerWorkspaceStackSlot.MAIN_FLOOR.suffix() : floorRole;
        minMainPathPieces = Math.max(0, minMainPathPieces);
        maxMainPathPieces = Math.max(minMainPathPieces, maxMainPathPieces);
        maxBranchPiecesBeforeCap = Math.max(0, Math.min(MAX_BRANCH_PIECES_BEFORE_CAP, maxBranchPiecesBeforeCap));
        hallwayLeadInMode = hallwayLeadInMode == null ? MKWorkspaceHallwayLeadInMode.AUTO : hallwayLeadInMode;
        manualHallwayLeadInPieces = Math.max(0, manualHallwayLeadInPieces);
        mainRoomProfiles = normalizeProfiles(mainRoomProfiles, MKWorkspaceFloorRoomKind.MAIN_ROOM);
        branchRoomProfiles = normalizeProfiles(branchRoomProfiles, MKWorkspaceFloorRoomKind.BRANCH_ROOM);
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
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.MAIN_ROOM,
                        stackSettings.width(), stackSettings.length(), height)),
                List.of(MKWorkspaceFloorRoomProfile.defaults(MKWorkspaceFloorRoomKind.BRANCH_ROOM,
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
                mainRoomProfiles, branchRoomProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxMainPathPieces(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, Math.min(minMainPathPieces, value), value,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainRoomProfiles, branchRoomProfiles);
    }

    public MKWorkspaceFloorTopologySettings withMaxBranchPiecesBeforeCap(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                value, hallwayLeadInMode, manualHallwayLeadInPieces, mainRoomProfiles, branchRoomProfiles);
    }

    public MKWorkspaceFloorTopologySettings withHallwayLeadInMode(MKWorkspaceHallwayLeadInMode value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, value, manualHallwayLeadInPieces, mainRoomProfiles, branchRoomProfiles);
    }

    public MKWorkspaceFloorTopologySettings withManualHallwayLeadInPieces(int value) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, value, mainRoomProfiles, branchRoomProfiles);
    }

    public MKWorkspaceFloorTopologySettings withRoomProfile(MKWorkspaceFloorRoomKind kind, int index,
                                                            MKWorkspaceFloorRoomProfile profile) {
        List<MKWorkspaceFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchProfiles = branchRoomProfiles;
        if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM) {
            mainProfiles = replaceProfile(mainRoomProfiles, index, profile);
        } else {
            branchProfiles = replaceProfile(branchRoomProfiles, index, profile);
        }
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainProfiles, branchProfiles);
    }

    public MKWorkspaceFloorTopologySettings withAddedRoomProfile(MKWorkspaceFloorRoomKind kind,
                                                                 MKWorkspaceFloorRoomProfile profile) {
        List<MKWorkspaceFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchProfiles = branchRoomProfiles;
        if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM) {
            mainProfiles = addProfile(mainRoomProfiles, profile, kind);
        } else {
            branchProfiles = addProfile(branchRoomProfiles, profile, kind);
        }
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainProfiles, branchProfiles);
    }

    public MKWorkspaceFloorTopologySettings withRemovedRoomProfile(MKWorkspaceFloorRoomKind kind, int index) {
        List<MKWorkspaceFloorRoomProfile> mainProfiles = mainRoomProfiles;
        List<MKWorkspaceFloorRoomProfile> branchProfiles = branchRoomProfiles;
        if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM) {
            mainProfiles = removeProfile(mainRoomProfiles, index);
        } else {
            branchProfiles = removeProfile(branchRoomProfiles, index);
        }
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainProfiles, branchProfiles);
    }

    private MKWorkspaceFloorTopologySettings withDefaultProfilesFor(MKWorkspaceFloorTopologySettings defaults) {
        return new MKWorkspaceFloorTopologySettings(stackId, floorRole, minMainPathPieces, maxMainPathPieces,
                maxBranchPiecesBeforeCap, hallwayLeadInMode, manualHallwayLeadInPieces,
                mainRoomProfiles.isEmpty() ? defaults.mainRoomProfiles() : mainRoomProfiles,
                branchRoomProfiles.isEmpty() ? defaults.branchRoomProfiles() : branchRoomProfiles);
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
