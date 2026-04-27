package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MKTowerWorkspaceFamilyDefinition {
    public static final Codec<MKTowerWorkspaceFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("baseName").forGetter(MKTowerWorkspaceFamilyDefinition::baseName),
            MKWorkspaceCodecs.TOWER_CATEGORY_CODEC.fieldOf("category").forGetter(MKTowerWorkspaceFamilyDefinition::category),
            MKWorkspaceCodecs.PIECE_ROLE_CODEC.fieldOf("pieceRole").forGetter(MKTowerWorkspaceFamilyDefinition::pieceRole),
            Codec.BOOL.optionalFieldOf("supportsVerticalAccess", true)
                    .forGetter(MKTowerWorkspaceFamilyDefinition::supportsVerticalAccess),
            Codec.INT.optionalFieldOf("roomWidth").forGetter(family -> Optional.of(family.roomWidth())),
            Codec.INT.optionalFieldOf("roomLength").forGetter(family -> Optional.of(family.roomLength())),
            Codec.INT.optionalFieldOf("roomHeight").forGetter(family -> Optional.of(family.roomHeight())),
            MKWorkspaceFamilyHorizontalExitDefinition.CODEC.listOf().optionalFieldOf("horizontalExits", List.of())
                    .forGetter(MKTowerWorkspaceFamilyDefinition::horizontalExits),
            MKWorkspaceCodecs.BRANCH_EXIT_MASK_CODEC.optionalFieldOf("branchExitMask")
                    .forGetter(family -> Optional.empty())
    ).apply(instance, MKTowerWorkspaceFamilyDefinition::fromSerializedData));

    private final String baseName;
    private final MKTowerWorkspaceCategory category;
    private final MKWorkspacePieceRole pieceRole;
    private final boolean supportsVerticalAccess;
    private final int roomWidth;
    private final int roomLength;
    private final int roomHeight;
    private final List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits;

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits) {
        this.baseName = baseName;
        this.category = category;
        this.pieceRole = pieceRole;
        this.supportsVerticalAccess = supportsVerticalAccess;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.roomHeight = roomHeight;
        this.horizontalExits = List.copyOf(horizontalExits);
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createDefaults() {
        return createDefaults(MKWorkspaceDimensions.defaultDimensions());
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createDefaults(MKWorkspaceDimensions dimensions) {
        MKTowerWorkspaceCategoryProfile entry = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions).stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.ENTRY)
                .findFirst()
                .orElseThrow();
        MKTowerWorkspaceCategoryProfile main = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions).stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.MAIN)
                .findFirst()
                .orElseThrow();
        MKTowerWorkspaceCategoryProfile basement = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions).stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.BASEMENT)
                .findFirst()
                .orElseThrow();
        MKTowerWorkspaceCategoryProfile top_cap = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions).stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.TOP_CAP)
                .findFirst()
                .orElseThrow();
        MKTowerWorkspaceCategoryProfile basementCap = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions).stream()
                .filter(profile -> profile.category() == MKTowerWorkspaceCategory.BASEMENT_CAP)
                .findFirst()
                .orElseThrow();
        return List.of(
                new MKTowerWorkspaceFamilyDefinition("entry", MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY, true,
                        entry.roomWidth(), entry.roomLength(), entry.fullHeight(),
                        List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "main_opening"))),
                new MKTowerWorkspaceFamilyDefinition("floor_main", MKTowerWorkspaceCategory.MAIN,
                        MKWorkspacePieceRole.FLOOR_MAIN, true,
                        main.roomWidth(), main.roomLength(), main.fullHeight(), List.of()),
                new MKTowerWorkspaceFamilyDefinition("top_cap_approach", MKTowerWorkspaceCategory.TOP_CAP,
                        MKWorkspacePieceRole.TOP_CAP_APPROACH, true,
                        top_cap.roomWidth(), top_cap.roomLength(), top_cap.fullHeight(), List.of()),
                new MKTowerWorkspaceFamilyDefinition("top_cap", MKTowerWorkspaceCategory.TOP_CAP,
                        MKWorkspacePieceRole.TOP_CAP, true,
                        top_cap.roomWidth(), top_cap.roomLength(), top_cap.fullHeight(), List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_entry", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_ENTRY, true,
                        basement.roomWidth(), basement.roomLength(), basement.fullHeight(), List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_main", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_MAIN, true,
                        basement.roomWidth(), basement.roomLength(), basement.fullHeight(), List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_cap", MKTowerWorkspaceCategory.BASEMENT_CAP,
                        MKWorkspacePieceRole.BASEMENT_CAP, true,
                        basementCap.roomWidth(), basementCap.roomLength(), basementCap.fullHeight(), List.of())
        );
    }

    public static MKTowerWorkspaceFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "tower workspace family definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "tower workspace family definition");
    }

    public List<String> validate(List<MKTowerWorkspaceFamilyDefinition> allFamilies,
                                 MKTowerWorkspaceCategoryProfile categoryProfile,
                                 MKWorkspaceVerticalAccessSpec verticalAccessSpec) {
        List<String> errors = new ArrayList<>();
        if (baseName.isBlank()) {
            errors.add("tower workspace family base name cannot be blank");
        }
        long duplicates = allFamilies.stream().filter(def -> def.baseName.equals(baseName)).count();
        if (duplicates > 1) {
            errors.add("tower workspace family base name must be unique: " + baseName);
        }
        if (pieceRole == MKWorkspacePieceRole.HALLWAY) {
            errors.add("tower workspace room families cannot use hallway role");
        }
        validateOdd(errors, "family " + baseName + " room width", roomWidth, 3);
        validateOdd(errors, "family " + baseName + " room length", roomLength, 3);
        if (supportsVerticalAccess) {
            if (roomHeight < 3) {
                errors.add("family " + baseName + " shaft-enabled room height must be at least 3");
            }
            if (roomWidth < verticalAccessSpec.shaftSize()) {
                errors.add("family " + baseName + " room width must be at least the shared shaft size");
            }
            if (roomLength < verticalAccessSpec.shaftSize()) {
                errors.add("family " + baseName + " room length must be at least the shared shaft size");
            }
            if (roomHeight != categoryProfile.fullHeight()) {
                errors.add("family " + baseName + " shaft-enabled room height must match category full height " +
                        categoryProfile.fullHeight());
            }
        } else if (roomHeight < MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT || roomHeight > categoryProfile.fullHeight()) {
            errors.add("family " + baseName + " non-shaft room height must be within category range " +
                    MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT + "-" + categoryProfile.fullHeight());
        }
        long mainEntryCount = horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY)
                .count();
        if (mainEntryCount > 1) {
            errors.add("family " + baseName + " can only define one main entry horizontal exit");
        }
        long mainExitCount = horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .count();
        if (mainExitCount > 1) {
            errors.add("family " + baseName + " can only define one main exit horizontal exit");
        }
        Set<Direction> reserved = reservedHorizontalDirections(pieceRole);
        Set<Direction> seenDirections = new LinkedHashSet<>();
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : horizontalExits) {
            if (exit.direction().getAxis().isVertical()) {
                errors.add("family " + baseName + " horizontal exit direction must be cardinal");
            }
            if (!seenDirections.add(exit.direction())) {
                errors.add("family " + baseName + " cannot define multiple horizontal exits on " +
                        exit.direction().getSerializedName());
            }
            if (reserved.contains(exit.direction())) {
                errors.add("family " + baseName + " cannot place a horizontal exit on reserved direction " +
                        exit.direction().getSerializedName() + " for role " + pieceRole.getSerializedName());
            }
            if (exit.openingProfileId().isBlank()) {
                errors.add("family " + baseName + " horizontal exit opening profile cannot be blank");
            }
        }
        return errors;
    }

    public static boolean defaultSupportsVerticalAccess(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case ENTRY, FLOOR_MAIN, TOP_CAP_APPROACH, TOP_CAP, BASEMENT_ENTRY, BASEMENT_MAIN, BASEMENT_CAP -> true;
            case HALLWAY -> false;
        };
    }

    public static Set<Direction> reservedHorizontalDirections(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case HALLWAY -> EnumSet.noneOf(Direction.class);
            default -> EnumSet.noneOf(Direction.class);
        };
    }

    public static List<MKTowerWorkspaceFamilyDefinition> normalize(List<MKTowerWorkspaceFamilyDefinition> families) {
        return normalize(families, List.of());
    }

    public static List<MKTowerWorkspaceFamilyDefinition> normalize(List<MKTowerWorkspaceFamilyDefinition> families,
                                                                   List<MKTowerWorkspaceCategoryProfile> categoryProfiles) {
        if (families.isEmpty()) {
            return categoryProfiles.isEmpty() ? createDefaults() : createDefaults(toLegacyDimensions(categoryProfiles));
        }
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<MKTowerWorkspaceFamilyDefinition> normalized = new ArrayList<>();
        for (MKTowerWorkspaceFamilyDefinition family : families) {
            if (seen.add(family.baseName())) {
                normalized.add(family.resolveGeometry(categoryProfiles));
            }
        }
        if (normalized.isEmpty()) {
            return categoryProfiles.isEmpty() ? createDefaults() : createDefaults(toLegacyDimensions(categoryProfiles));
        }
        return List.copyOf(normalized);
    }

    public String baseName() {
        return baseName;
    }

    public MKTowerWorkspaceCategory category() {
        return category;
    }

    public MKWorkspacePieceRole pieceRole() {
        return pieceRole;
    }

    public boolean supportsVerticalAccess() {
        return supportsVerticalAccess;
    }

    public int roomWidth() {
        return roomWidth;
    }

    public int roomLength() {
        return roomLength;
    }

    public int roomHeight() {
        return roomHeight;
    }

    public List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits() {
        return horizontalExits;
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> mainEntry() {
        return horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY)
                .findFirst();
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> mainExit() {
        return horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .findFirst();
    }

    public List<MKWorkspaceFamilyHorizontalExitDefinition> branchExits() {
        return horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH)
                .toList();
    }

    public MKTowerBranchExitMask legacyBranchExitMask() {
        return MKTowerBranchExitMask.fromDirections(branchExits().stream()
                .map(MKWorkspaceFamilyHorizontalExitDefinition::direction)
                .toList());
    }

    public String horizontalExitSummary() {
        if (horizontalExits.isEmpty()) {
            return "none";
        }
        return horizontalExits.stream()
                .map(exit -> exit.direction().getSerializedName() + ":" + exit.pathKind().getSerializedName() + ":" +
                        exit.openingProfileId())
                .collect(java.util.stream.Collectors.joining("|"));
    }

    private static MKTowerWorkspaceFamilyDefinition fromSerializedData(String baseName, MKTowerWorkspaceCategory category,
                                                                       MKWorkspacePieceRole pieceRole,
                                                                       boolean supportsVerticalAccess,
                                                                       Optional<Integer> roomWidth,
                                                                       Optional<Integer> roomLength,
                                                                       Optional<Integer> roomHeight,
                                                                       List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                       Optional<MKTowerBranchExitMask> branchExitMask) {
        if (!horizontalExits.isEmpty()) {
            return new MKTowerWorkspaceFamilyDefinition(baseName, category, pieceRole, supportsVerticalAccess,
                    roomWidth.orElse(0), roomLength.orElse(0), roomHeight.orElse(0),
                    horizontalExits);
        }
        return new MKTowerWorkspaceFamilyDefinition(baseName, category, pieceRole, supportsVerticalAccess,
                roomWidth.orElse(0), roomLength.orElse(0), roomHeight.orElse(0),
                buildLegacyHorizontalExits(category, pieceRole, branchExitMask.orElse(MKTowerBranchExitMask.NONE)));
    }

    private static List<MKWorkspaceFamilyHorizontalExitDefinition> buildLegacyHorizontalExits(MKTowerWorkspaceCategory category,
                                                                                               MKWorkspacePieceRole pieceRole,
                                                                                               MKTowerBranchExitMask branchExitMask) {
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new ArrayList<>();
        if (pieceRole == MKWorkspacePieceRole.ENTRY) {
            exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.SOUTH,
                    MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, "main_opening"));
        }
        for (Direction direction : branchExitMask.directions()) {
            exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(direction,
                    MKWorkspaceHorizontalExitPathKind.BRANCH, "branch_opening"));
        }
        return List.copyOf(exits);
    }

    private MKTowerWorkspaceFamilyDefinition resolveGeometry(List<MKTowerWorkspaceCategoryProfile> categoryProfiles) {
        if (roomWidth > 0 && roomLength > 0 && roomHeight > 0) {
            return this;
        }
        Optional<MKTowerWorkspaceCategoryProfile> profileOpt = categoryProfiles.stream()
                .filter(profile -> profile.category() == category)
                .findFirst();
        if (profileOpt.isEmpty()) {
            return this;
        }
        MKTowerWorkspaceCategoryProfile profile = profileOpt.get();
        return new MKTowerWorkspaceFamilyDefinition(
                baseName,
                category,
                pieceRole,
                supportsVerticalAccess,
                roomWidth > 0 ? roomWidth : profile.roomWidth(),
                roomLength > 0 ? roomLength : profile.roomLength(),
                roomHeight > 0 ? roomHeight : profile.fullHeight(),
                horizontalExits
        );
    }

    private static MKWorkspaceDimensions toLegacyDimensions(List<MKTowerWorkspaceCategoryProfile> categoryProfiles) {
        java.util.Map<MKTowerWorkspaceCategory, MKTowerWorkspaceCategoryProfile> byCategory = categoryProfiles.stream()
                .collect(java.util.stream.Collectors.toMap(MKTowerWorkspaceCategoryProfile::category, profile -> profile));
        MKTowerWorkspaceCategoryProfile entry = byCategory.getOrDefault(MKTowerWorkspaceCategory.ENTRY,
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.ENTRY, 9, 9, 5));
        MKTowerWorkspaceCategoryProfile main = byCategory.getOrDefault(MKTowerWorkspaceCategory.MAIN,
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.MAIN, 9, 9, 5));
        MKTowerWorkspaceCategoryProfile basement = byCategory.getOrDefault(MKTowerWorkspaceCategory.BASEMENT,
                new MKTowerWorkspaceCategoryProfile(MKTowerWorkspaceCategory.BASEMENT, 9, 9, 5));
        return new MKWorkspaceDimensions(
                main.roomWidth(),
                main.roomLength(),
                entry.fullHeight(),
                main.fullHeight(),
                basement.fullHeight(),
                3,
                3,
                3
        );
    }

    private static void validateOdd(List<String> errors, String label, int value, int min) {
        if (value < min) {
            errors.add(label + " must be at least " + min);
        }
        if (value % 2 == 0) {
            errors.add(label + " must be odd");
        }
    }
}

