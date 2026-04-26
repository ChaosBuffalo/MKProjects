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
            MKWorkspaceFamilyHorizontalExitDefinition.CODEC.listOf().optionalFieldOf("horizontalExits", List.of())
                    .forGetter(MKTowerWorkspaceFamilyDefinition::horizontalExits),
            MKWorkspaceCodecs.BRANCH_EXIT_MASK_CODEC.optionalFieldOf("branchExitMask")
                    .forGetter(family -> Optional.empty())
    ).apply(instance, MKTowerWorkspaceFamilyDefinition::fromSerializedData));

    private final String baseName;
    private final MKTowerWorkspaceCategory category;
    private final MKWorkspacePieceRole pieceRole;
    private final boolean supportsVerticalAccess;
    private final List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits;

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits) {
        this.baseName = baseName;
        this.category = category;
        this.pieceRole = pieceRole;
        this.supportsVerticalAccess = supportsVerticalAccess;
        this.horizontalExits = List.copyOf(horizontalExits);
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createDefaults() {
        return List.of(
                new MKTowerWorkspaceFamilyDefinition("entry", MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY, true,
                        List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN, "entry_main"))),
                new MKTowerWorkspaceFamilyDefinition("floor_main", MKTowerWorkspaceCategory.MAIN,
                        MKWorkspacePieceRole.FLOOR_MAIN, true, List.of()),
                new MKTowerWorkspaceFamilyDefinition("boss_approach", MKTowerWorkspaceCategory.BOSS,
                        MKWorkspacePieceRole.BOSS_APPROACH, true, List.of()),
                new MKTowerWorkspaceFamilyDefinition("boss_cap", MKTowerWorkspaceCategory.BOSS,
                        MKWorkspacePieceRole.BOSS_CAP, true, List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_entry", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_ENTRY, true, List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_main", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_MAIN, true, List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_cap", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_CAP, true, List.of())
        );
    }

    public static MKTowerWorkspaceFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "tower workspace family definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "tower workspace family definition");
    }

    public List<String> validate(List<MKTowerWorkspaceFamilyDefinition> allFamilies) {
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
        long mainExitCount = horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN)
                .count();
        if (mainExitCount > 1) {
            errors.add("family " + baseName + " can only define one main horizontal exit");
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
                errors.add("family " + baseName + " cannot place a branch exit on reserved direction " +
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
            case ENTRY, FLOOR_MAIN, BOSS_APPROACH, BOSS_CAP, BASEMENT_ENTRY, BASEMENT_MAIN, BASEMENT_CAP -> true;
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
        if (families.isEmpty()) {
            return createDefaults();
        }
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<MKTowerWorkspaceFamilyDefinition> normalized = new ArrayList<>();
        for (MKTowerWorkspaceFamilyDefinition family : families) {
            if (seen.add(family.baseName())) {
                normalized.add(family);
            }
        }
        if (normalized.isEmpty()) {
            return createDefaults();
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

    public List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits() {
        return horizontalExits;
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> mainExit() {
        return horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN)
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
                                                                       List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                       Optional<MKTowerBranchExitMask> branchExitMask) {
        if (!horizontalExits.isEmpty()) {
            return new MKTowerWorkspaceFamilyDefinition(baseName, category, pieceRole, supportsVerticalAccess,
                    horizontalExits);
        }
        return new MKTowerWorkspaceFamilyDefinition(baseName, category, pieceRole, supportsVerticalAccess,
                buildLegacyHorizontalExits(category, pieceRole, branchExitMask.orElse(MKTowerBranchExitMask.NONE)));
    }

    private static List<MKWorkspaceFamilyHorizontalExitDefinition> buildLegacyHorizontalExits(MKTowerWorkspaceCategory category,
                                                                                               MKWorkspacePieceRole pieceRole,
                                                                                               MKTowerBranchExitMask branchExitMask) {
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new ArrayList<>();
        if (pieceRole == MKWorkspacePieceRole.ENTRY) {
            exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.SOUTH,
                    MKWorkspaceHorizontalExitPathKind.MAIN, category.getSerializedName() + "_main"));
        }
        for (Direction direction : branchExitMask.directions()) {
            exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(direction,
                    MKWorkspaceHorizontalExitPathKind.BRANCH, category.getSerializedName() + "_branch"));
        }
        return List.copyOf(exits);
    }
}
