package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MKTowerWorkspaceFamilyDefinition {
    public static final Codec<MKTowerWorkspaceFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("baseName").forGetter(MKTowerWorkspaceFamilyDefinition::baseName),
            MKWorkspaceCodecs.TOWER_CATEGORY_CODEC.fieldOf("category").forGetter(MKTowerWorkspaceFamilyDefinition::category),
            MKWorkspaceCodecs.PIECE_ROLE_CODEC.fieldOf("pieceRole").forGetter(MKTowerWorkspaceFamilyDefinition::pieceRole),
            Codec.BOOL.optionalFieldOf("supportsVerticalAccess", true)
                    .forGetter(MKTowerWorkspaceFamilyDefinition::supportsVerticalAccess),
            MKWorkspaceCodecs.BRANCH_EXIT_MASK_CODEC.optionalFieldOf("branchExitMask", MKTowerBranchExitMask.NONE)
                    .forGetter(MKTowerWorkspaceFamilyDefinition::branchExitMask)
    ).apply(instance, MKTowerWorkspaceFamilyDefinition::new));

    private final String baseName;
    private final MKTowerWorkspaceCategory category;
    private final MKWorkspacePieceRole pieceRole;
    private final boolean supportsVerticalAccess;
    private final MKTowerBranchExitMask branchExitMask;

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            MKTowerBranchExitMask branchExitMask) {
        this.baseName = baseName;
        this.category = category;
        this.pieceRole = pieceRole;
        this.supportsVerticalAccess = supportsVerticalAccess;
        this.branchExitMask = branchExitMask;
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createDefaults() {
        return List.of(
                new MKTowerWorkspaceFamilyDefinition("entry", MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY, true, MKTowerBranchExitMask.NONE),
                new MKTowerWorkspaceFamilyDefinition("floor_main", MKTowerWorkspaceCategory.MAIN,
                        MKWorkspacePieceRole.FLOOR_MAIN, true, MKTowerBranchExitMask.NONE),
                new MKTowerWorkspaceFamilyDefinition("boss_approach", MKTowerWorkspaceCategory.BOSS,
                        MKWorkspacePieceRole.BOSS_APPROACH, true, MKTowerBranchExitMask.NONE),
                new MKTowerWorkspaceFamilyDefinition("boss_cap", MKTowerWorkspaceCategory.BOSS,
                        MKWorkspacePieceRole.BOSS_CAP, true, MKTowerBranchExitMask.NONE),
                new MKTowerWorkspaceFamilyDefinition("basement_entry", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_ENTRY, true, MKTowerBranchExitMask.NONE),
                new MKTowerWorkspaceFamilyDefinition("basement_main", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_MAIN, true, MKTowerBranchExitMask.NONE),
                new MKTowerWorkspaceFamilyDefinition("basement_cap", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_CAP, true, MKTowerBranchExitMask.NONE)
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
        Set<Direction> reserved = reservedHorizontalDirections(pieceRole);
        for (Direction direction : branchExitMask.directions()) {
            if (reserved.contains(direction)) {
                errors.add("family " + baseName + " cannot place a branch exit on reserved direction " +
                        direction.getSerializedName() + " for role " + pieceRole.getSerializedName());
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
            case ENTRY -> EnumSet.of(Direction.SOUTH);
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

    public MKTowerBranchExitMask branchExitMask() {
        return branchExitMask;
    }
}
