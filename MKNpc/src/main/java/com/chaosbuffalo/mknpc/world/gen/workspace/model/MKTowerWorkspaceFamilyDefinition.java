package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MKTowerWorkspaceFamilyDefinition implements MKWorkspacePaletteFamily {
    public static final Codec<MKTowerWorkspaceFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("baseName").forGetter(MKTowerWorkspaceFamilyDefinition::baseName),
            MKWorkspaceCodecs.TOWER_CATEGORY_CODEC.fieldOf("category").forGetter(MKTowerWorkspaceFamilyDefinition::category),
            MKWorkspaceCodecs.PIECE_ROLE_CODEC.fieldOf("pieceRole").forGetter(MKTowerWorkspaceFamilyDefinition::pieceRole),
            Codec.STRING.optionalFieldOf("topologySlotId", "").forGetter(MKTowerWorkspaceFamilyDefinition::topologySlotId),
            Codec.STRING.optionalFieldOf("verticalAccessGroupId", "").forGetter(MKTowerWorkspaceFamilyDefinition::verticalAccessGroupId),
            Codec.BOOL.fieldOf("supportsVerticalAccess")
                    .forGetter(MKTowerWorkspaceFamilyDefinition::supportsVerticalAccess),
            Codec.INT.optionalFieldOf("roomWidth", 0).forGetter(MKTowerWorkspaceFamilyDefinition::roomWidth),
            Codec.INT.optionalFieldOf("roomLength", 0).forGetter(MKTowerWorkspaceFamilyDefinition::roomLength),
            Codec.INT.optionalFieldOf("roomHeight", 0).forGetter(MKTowerWorkspaceFamilyDefinition::roomHeight),
            MKWorkspaceCodecs.HORIZONTAL_EXTRUSION_MODE_CODEC.fieldOf("horizontalExtrusionMode")
                    .forGetter(MKTowerWorkspaceFamilyDefinition::horizontalExtrusionMode),
            MKWorkspaceFamilyHorizontalExitDefinition.CODEC.listOf().optionalFieldOf("horizontalExits", List.of())
                    .forGetter(MKTowerWorkspaceFamilyDefinition::horizontalExits),
            Codec.INT.optionalFieldOf("topVoidMargin", 0).forGetter(MKTowerWorkspaceFamilyDefinition::topVoidMargin),
            Codec.INT.optionalFieldOf("bottomVoidMargin", 0).forGetter(MKTowerWorkspaceFamilyDefinition::bottomVoidMargin),
            MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundationPolicy", MKWorkspaceFoundationPolicy.none())
                    .forGetter(MKTowerWorkspaceFamilyDefinition::foundationPolicy),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("paletteOverride")
                    .forGetter(MKTowerWorkspaceFamilyDefinition::paletteOverrideOpt)
    ).apply(instance, (baseName, category, pieceRole, topologySlotId, verticalAccessGroupId, supportsVerticalAccess,
                       roomWidth, roomLength, roomHeight,
                       horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin, foundationPolicy,
                       paletteOverride) ->
            new MKTowerWorkspaceFamilyDefinition(baseName, category, pieceRole, topologySlotId, verticalAccessGroupId,
                    supportsVerticalAccess,
                    roomWidth, roomLength, roomHeight, horizontalExtrusionMode, horizontalExits,
                    topVoidMargin, bottomVoidMargin, foundationPolicy, paletteOverride.orElse(null))));

    private final String baseName;
    private final MKTowerWorkspaceCategory category;
    private final MKWorkspacePieceRole pieceRole;
    private final String topologySlotId;
    private final String verticalAccessGroupId;
    private final boolean supportsVerticalAccess;
    private final int roomWidth;
    private final int roomLength;
    private final int roomHeight;
    private final MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode;
    private final List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits;
    private final int topVoidMargin;
    private final int bottomVoidMargin;
    private final MKWorkspaceFoundationPolicy foundationPolicy;
    @Nullable
    private final MKWorkspacePaletteOverride paletteOverride;

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits) {
        this(baseName, category, pieceRole, supportsVerticalAccess, roomWidth, roomLength, roomHeight,
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY, horizontalExits);
    }

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits) {
        this(baseName, category, pieceRole, supportsVerticalAccess, roomWidth, roomLength, roomHeight,
                horizontalExtrusionMode, horizontalExits, null);
    }

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                            @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(baseName, category, pieceRole, supportsVerticalAccess, roomWidth, roomLength, roomHeight,
                horizontalExtrusionMode, horizontalExits, 0, 0, MKWorkspaceFoundationPolicy.none(), paletteOverride);
    }

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                            int topVoidMargin, int bottomVoidMargin,
                                            @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(baseName, category, pieceRole, supportsVerticalAccess, roomWidth, roomLength, roomHeight,
                horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin,
                MKWorkspaceFoundationPolicy.none(), paletteOverride);
    }

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                            int topVoidMargin, int bottomVoidMargin,
                                            MKWorkspaceFoundationPolicy foundationPolicy,
                                            @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(baseName, category, pieceRole, defaultTopologySlotId(pieceRole), supportsVerticalAccess,
                roomWidth, roomLength, roomHeight, horizontalExtrusionMode, horizontalExits, topVoidMargin,
                bottomVoidMargin, foundationPolicy, paletteOverride);
    }

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, String topologySlotId,
                                            boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                            int topVoidMargin, int bottomVoidMargin,
                                            MKWorkspaceFoundationPolicy foundationPolicy,
                                            @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this(baseName, category, pieceRole, topologySlotId, defaultVerticalAccessGroupId(supportsVerticalAccess),
                supportsVerticalAccess, roomWidth, roomLength, roomHeight, horizontalExtrusionMode, horizontalExits,
                topVoidMargin, bottomVoidMargin, foundationPolicy, paletteOverride);
    }

    public MKTowerWorkspaceFamilyDefinition(String baseName, MKTowerWorkspaceCategory category,
                                            MKWorkspacePieceRole pieceRole, String topologySlotId,
                                            String verticalAccessGroupId, boolean supportsVerticalAccess,
                                            int roomWidth, int roomLength, int roomHeight,
                                            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                            List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                            int topVoidMargin, int bottomVoidMargin,
                                            MKWorkspaceFoundationPolicy foundationPolicy,
                                            @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this.baseName = baseName;
        this.category = category;
        this.pieceRole = pieceRole;
        this.topologySlotId = topologySlotId == null || topologySlotId.isBlank() ?
                defaultTopologySlotId(pieceRole) : topologySlotId;
        this.verticalAccessGroupId = verticalAccessGroupId == null || verticalAccessGroupId.isBlank() ?
                defaultVerticalAccessGroupId(supportsVerticalAccess) : verticalAccessGroupId;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.roomHeight = roomHeight;
        this.horizontalExtrusionMode = horizontalExtrusionMode;
        this.horizontalExits = normalizeFamilyExits(pieceRole, supportsVerticalAccess, horizontalExits);
        this.supportsVerticalAccess = this.horizontalExits.stream()
                .anyMatch(MKWorkspaceFamilyHorizontalExitDefinition::isVerticalAccess);
        this.topVoidMargin = Math.max(0, topVoidMargin);
        this.bottomVoidMargin = Math.max(0, bottomVoidMargin);
        this.foundationPolicy = foundationPolicy == null ? MKWorkspaceFoundationPolicy.none() : foundationPolicy;
        this.paletteOverride = paletteOverride != null && !paletteOverride.isEmpty() ? paletteOverride : null;
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
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY, "main_opening",
                                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION))),
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
                new MKTowerWorkspaceFamilyDefinition("basement_cap_approach", MKTowerWorkspaceCategory.BASEMENT_CAP,
                        MKWorkspacePieceRole.BASEMENT_CAP_APPROACH, true,
                        basementCap.roomWidth(), basementCap.roomLength(), basementCap.fullHeight(), List.of()),
                new MKTowerWorkspaceFamilyDefinition("basement_cap", MKTowerWorkspaceCategory.BASEMENT_CAP,
                        MKWorkspacePieceRole.BASEMENT_CAP, true,
                        basementCap.roomWidth(), basementCap.roomLength(), basementCap.fullHeight(), List.of())
        );
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createWalledKeepDefaults(MKWorkspaceDimensions dimensions) {
        int keepHeight = 7;
        int centerWidth = doubledOddFootprint(Math.max(9, dimensions.roomWidth()));
        int centerLength = doubledOddFootprint(Math.max(9, dimensions.roomLength()));
        int cornerFootprint = 7;
        return List.of(
                new MKTowerWorkspaceFamilyDefinition("keep_center_entry", MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY, "keep.center.entry", "keep.center", true,
                        centerWidth, centerLength, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null),
                new MKTowerWorkspaceFamilyDefinition("keep_center_main_floor", MKTowerWorkspaceCategory.MAIN,
                        MKWorkspacePieceRole.FLOOR_MAIN, "keep.center.main_floor", "keep.center", true,
                        centerWidth, centerLength, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null),
                new MKTowerWorkspaceFamilyDefinition("keep_center_top_cap", MKTowerWorkspaceCategory.TOP_CAP,
                        MKWorkspacePieceRole.TOP_CAP, "keep.center.top_cap", "keep.center", true,
                        centerWidth, centerLength, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null),
                new MKTowerWorkspaceFamilyDefinition("keep_center_basement_floor", MKTowerWorkspaceCategory.BASEMENT,
                        MKWorkspacePieceRole.BASEMENT_MAIN, "keep.center.basement_floor", "keep.center", true,
                        centerWidth, centerLength, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null),
                new MKTowerWorkspaceFamilyDefinition("keep_center_basement_cap", MKTowerWorkspaceCategory.BASEMENT_CAP,
                        MKWorkspacePieceRole.BASEMENT_CAP, "keep.center.basement_cap", "keep.center", true,
                        centerWidth, centerLength, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null),
                new MKTowerWorkspaceFamilyDefinition("keep_corner_shared", MKTowerWorkspaceCategory.MAIN,
                        MKWorkspacePieceRole.FLOOR_MAIN, "keep.corner.shared", "keep.corner.shared", false,
                        cornerFootprint, cornerFootprint, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null),
                new MKTowerWorkspaceFamilyDefinition("keep_gate_main", MKTowerWorkspaceCategory.ENTRY,
                        MKWorkspacePieceRole.ENTRY, "keep.gate.main", "keep.gate", false,
                        7, 5, keepHeight,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        MKWorkspaceFoundationPolicy.none(), null)
        );
    }

    private static int doubledOddFootprint(int footprint) {
        int oddFootprint = footprint % 2 == 0 ? footprint + 1 : footprint;
        return Math.max(3, (oddFootprint * 2) - 1);
    }

    private static List<MKWorkspaceFamilyHorizontalExitDefinition> normalizeFamilyExits(
            MKWorkspacePieceRole pieceRole, boolean supportsVerticalAccess,
            List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> normalized = new ArrayList<>(exits);
        boolean hasVerticalExit = normalized.stream()
                .anyMatch(MKWorkspaceFamilyHorizontalExitDefinition::isVerticalAccess);
        if (supportsVerticalAccess && !hasVerticalExit) {
            for (Direction direction : defaultVerticalAccessDirections(pieceRole)) {
                normalized.add(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(direction));
            }
        }
        return List.copyOf(normalized);
    }

    private static List<Direction> defaultVerticalAccessDirections(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case ENTRY, FLOOR_MAIN, TOP_CAP_APPROACH, BASEMENT_ENTRY, BASEMENT_MAIN, BASEMENT_CAP_APPROACH ->
                    List.of(Direction.UP, Direction.DOWN);
            case TOP_CAP -> List.of(Direction.DOWN);
            case BASEMENT_CAP -> List.of(Direction.UP);
            case HALLWAY -> List.of();
        };
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
        if (topologySlotId.isBlank()) {
            errors.add("family " + baseName + " topology slot id cannot be blank");
        }
        if (supportsVerticalAccess() && verticalAccessGroupId.isBlank()) {
            errors.add("family " + baseName + " shaft-enabled room must declare a vertical access group id");
        }
        validateOdd(errors, "family " + baseName + " room width", roomWidth, 3);
        validateOdd(errors, "family " + baseName + " room length", roomLength, 3);
        if (supportsVerticalAccess()) {
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
        if (supportsVerticalAccess() && (topVoidMargin > 0 || bottomVoidMargin > 0)) {
            errors.add("family " + baseName + " shaft-enabled room cannot define top or bottom void margins");
        }
        if (!supportsVerticalAccess()) {
            int reducedRoomHeight = roomHeight - topVoidMargin - bottomVoidMargin;
            if (reducedRoomHeight < MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT) {
                errors.add("family " + baseName + " non-shaft room height after void margins must be at least " +
                        MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT);
            }
        }
        errors.addAll(foundationPolicy.validate("family " + baseName));
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
        long mainEndingEntryCount = horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY)
                .count();
        if (mainEndingEntryCount > 1) {
            errors.add("family " + baseName + " can only define one main ending entry horizontal exit");
        }
        long branchCapEntryCount = horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY)
                .count();
        if (branchCapEntryCount > 1) {
            errors.add("family " + baseName + " can only define one branch cap entry horizontal exit");
        }
        if (mainEndingEntryCount > 0 && mainExitCount > 0) {
            errors.add("family " + baseName + " cannot define a main ending entry and a main exit");
        }
        if (branchCapEntryCount > 0 && (mainEntryCount > 0 || mainExitCount > 0 || mainEndingEntryCount > 0)) {
            errors.add("family " + baseName + " cannot define a branch cap entry and a main path exit");
        }
        if (branchCapEntryCount > 0 && horizontalExits.stream()
                .anyMatch(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH)) {
            errors.add("family " + baseName + " cannot define a branch cap entry and a branch exit");
        }
        Set<Direction> reserved = reservedHorizontalDirections(pieceRole);
        Set<Direction> seenDirections = new LinkedHashSet<>();
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : horizontalExits) {
            if (!seenDirections.add(exit.direction())) {
                errors.add("family " + baseName + " cannot define multiple exits on " +
                        exit.direction().getSerializedName());
            }
            if (exit.isVerticalAccess()) {
                if (!exit.direction().getAxis().isVertical()) {
                    errors.add("family " + baseName + " vertical access exit direction must be up or down");
                }
                if (exit.pathKind() != MKWorkspaceHorizontalExitPathKind.VERTICAL_ACCESS) {
                    errors.add("family " + baseName + " vertical access exit must use vertical_access kind");
                }
                continue;
            }
            if (exit.direction().getAxis().isVertical()) {
                errors.add("family " + baseName + " horizontal exit direction must be cardinal");
            }
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.VERTICAL_ACCESS) {
                errors.add("family " + baseName + " horizontal exit cannot use vertical_access kind");
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
            case ENTRY, FLOOR_MAIN, TOP_CAP_APPROACH, TOP_CAP, BASEMENT_ENTRY, BASEMENT_MAIN,
                 BASEMENT_CAP_APPROACH, BASEMENT_CAP -> true;
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
            return createDefaults();
        }
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<MKTowerWorkspaceFamilyDefinition> normalized = new ArrayList<>();
        for (MKTowerWorkspaceFamilyDefinition family : families) {
            if (seen.add(family.baseName())) {
                normalized.add(family.resolveGeometry(categoryProfiles));
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

    public String topologySlotId() {
        return topologySlotId;
    }

    public String verticalAccessGroupId() {
        return verticalAccessGroupId;
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

    public MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode() {
        return horizontalExtrusionMode;
    }

    public List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits() {
        return horizontalExits;
    }

    public List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalOnlyExits() {
        return horizontalExits.stream()
                .filter(exit -> !exit.isVerticalAccess())
                .toList();
    }

    public List<MKWorkspaceFamilyHorizontalExitDefinition> verticalAccessExits() {
        return horizontalExits.stream()
                .filter(MKWorkspaceFamilyHorizontalExitDefinition::isVerticalAccess)
                .toList();
    }

    public boolean hasVerticalAccess(Direction direction) {
        return horizontalExits.stream()
                .anyMatch(exit -> exit.isVerticalAccess() && exit.direction() == direction);
    }

    public int topVoidMargin() {
        return topVoidMargin;
    }

    public int bottomVoidMargin() {
        return bottomVoidMargin;
    }

    public MKWorkspaceFoundationPolicy foundationPolicy() {
        return foundationPolicy;
    }

    @Override
    public String paletteFamilyId() {
        return baseName;
    }

    @Override
    public Optional<MKTowerWorkspaceCategory> paletteCategoryOpt() {
        return Optional.of(category);
    }

    @Override
    public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
        return Optional.ofNullable(paletteOverride);
    }

    @Nullable
    public MKWorkspacePaletteOverride paletteOverride() {
        return paletteOverride;
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> mainEntry() {
        return horizontalExits.stream()
                .filter(exit -> !exit.isVerticalAccess())
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY)
                .findFirst();
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> mainExit() {
        return horizontalExits.stream()
                .filter(exit -> !exit.isVerticalAccess())
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .findFirst();
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> mainEndingEntry() {
        return horizontalExits.stream()
                .filter(exit -> !exit.isVerticalAccess())
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY)
                .findFirst();
    }

    public boolean mainPathEnding() {
        return mainEndingEntry().isPresent();
    }

    public Optional<MKWorkspaceFamilyHorizontalExitDefinition> branchCapEntry() {
        return horizontalExits.stream()
                .filter(exit -> !exit.isVerticalAccess())
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY)
                .findFirst();
    }

    public boolean branchCap() {
        return branchCapEntry().isPresent();
    }

    public List<MKWorkspaceFamilyHorizontalExitDefinition> branchExits() {
        return horizontalExits.stream()
                .filter(exit -> !exit.isVerticalAccess())
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH)
                .toList();
    }

    public String horizontalExitSummary() {
        if (horizontalExits.isEmpty()) {
            return "none";
        }
        return horizontalExits.stream()
                .map(exit -> exit.direction().getSerializedName() + ":" + exit.pathKind().getSerializedName() + ":" +
                        exit.connectionMode().getSerializedName() + ":" + exit.openingProfileId() + ":" +
                        exit.sideOffset() + ":" + exit.verticalOffset())
                .collect(java.util.stream.Collectors.joining("|"));
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
                topologySlotId,
                verticalAccessGroupId,
                supportsVerticalAccess,
                roomWidth > 0 ? roomWidth : profile.roomWidth(),
                roomLength > 0 ? roomLength : profile.roomLength(),
                roomHeight > 0 ? roomHeight : profile.fullHeight(),
                horizontalExtrusionMode,
                horizontalExits,
                topVoidMargin,
                bottomVoidMargin,
                foundationPolicy,
                paletteOverride
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

    private static String defaultTopologySlotId(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case ENTRY -> "tower.entry";
            case FLOOR_MAIN -> "tower.main_floor";
            case TOP_CAP_APPROACH -> "tower.top_cap_approach";
            case TOP_CAP -> "tower.top_cap";
            case BASEMENT_ENTRY -> "tower.basement_entry";
            case BASEMENT_MAIN -> "tower.basement_floor";
            case BASEMENT_CAP_APPROACH -> "tower.basement_cap_approach";
            case BASEMENT_CAP -> "tower.basement_cap";
            case HALLWAY -> "tower.linear_run.branch";
        };
    }

    private static String defaultVerticalAccessGroupId(boolean supportsVerticalAccess) {
        return supportsVerticalAccess ? "tower.core" : "";
    }
}

