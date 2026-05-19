package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class MKTowerWorkspaceFamilyDefinition implements MKWorkspacePaletteFamily {
    private static final String PRIMARY_TOWER_STACK_ID = "tower.primary";

    public static final Codec<MKTowerWorkspaceFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("baseName").forGetter(MKTowerWorkspaceFamilyDefinition::baseName),
            MKWorkspaceTopologySlotMetadata.CODEC.fieldOf("slotMetadata")
                    .forGetter(MKTowerWorkspaceFamilyDefinition::slotMetadata),
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
            MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundationPolicy")
                    .forGetter(MKTowerWorkspaceFamilyDefinition::foundationPolicyOverrideOpt),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("paletteOverride")
                    .forGetter(MKTowerWorkspaceFamilyDefinition::paletteOverrideOpt)
    ).apply(instance, (baseName, slotMetadata, verticalAccessGroupId, supportsVerticalAccess,
                       roomWidth, roomLength, roomHeight,
                       horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin, foundationPolicyOverride,
                       paletteOverride) ->
            MKTowerWorkspaceFamilyDefinition.forTopologySlot(baseName, slotMetadata, verticalAccessGroupId,
                    supportsVerticalAccess, roomWidth, roomLength, roomHeight, horizontalExtrusionMode,
                    horizontalExits, topVoidMargin, bottomVoidMargin, foundationPolicyOverride.orElse(null),
                    paletteOverride.orElse(null))));

    private final String baseName;
    private final MKWorkspaceTopologySlotMetadata slotMetadata;
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
    @Nullable
    private final MKWorkspaceFoundationPolicy foundationPolicyOverride;
    @Nullable
    private final MKWorkspacePaletteOverride paletteOverride;

    private MKTowerWorkspaceFamilyDefinition(String baseName,
                                             MKWorkspaceTopologySlotMetadata slotMetadata,
                                             String topologySlotId,
                                             String verticalAccessGroupId, boolean supportsVerticalAccess,
                                             int roomWidth, int roomLength, int roomHeight,
                                             MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                             List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                             int topVoidMargin, int bottomVoidMargin,
                                             @Nullable MKWorkspaceFoundationPolicy foundationPolicy,
                                             @Nullable MKWorkspacePaletteOverride paletteOverride) {
        this.baseName = baseName;
        this.topologySlotId = topologySlotId;
        this.slotMetadata = slotMetadata;
        this.verticalAccessGroupId = verticalAccessGroupId == null || verticalAccessGroupId.isBlank() ?
                defaultVerticalAccessGroupId(supportsVerticalAccess) : verticalAccessGroupId;
        this.roomWidth = roomWidth;
        this.roomLength = roomLength;
        this.roomHeight = roomHeight;
        this.horizontalExtrusionMode = horizontalExtrusionMode;
        this.horizontalExits = normalizeFamilyExits(slotMetadata, supportsVerticalAccess, horizontalExits);
        this.supportsVerticalAccess = this.horizontalExits.stream()
                .anyMatch(MKWorkspaceFamilyHorizontalExitDefinition::isVerticalAccess);
        this.topVoidMargin = Math.max(0, topVoidMargin);
        this.bottomVoidMargin = Math.max(0, bottomVoidMargin);
        this.foundationPolicyOverride = foundationPolicy;
        this.paletteOverride = paletteOverride != null && !paletteOverride.isEmpty() ? paletteOverride : null;
    }

    public static MKTowerWorkspaceFamilyDefinition forTopologySlot(String baseName,
                                                                    String topologySlotId,
                                                                    String verticalAccessGroupId,
                                                                    boolean supportsVerticalAccess,
                                                                    int roomWidth,
                                                                    int roomLength,
                                                                    int roomHeight,
                                                                    MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                    List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                    int topVoidMargin,
                                                                    int bottomVoidMargin,
                                                                    @Nullable MKWorkspaceFoundationPolicy foundationPolicy,
                                                                    @Nullable MKWorkspacePaletteOverride paletteOverride) {
        return forTopologySlot(baseName,
                MKWorkspaceTopologySlotMetadata.fromTopologySlotId(topologySlotId),
                verticalAccessGroupId,
                supportsVerticalAccess,
                roomWidth,
                roomLength,
                roomHeight,
                horizontalExtrusionMode,
                horizontalExits,
                topVoidMargin,
                bottomVoidMargin,
                foundationPolicy,
                paletteOverride);
    }

    public static MKTowerWorkspaceFamilyDefinition forTopologySlot(String baseName,
                                                                    MKWorkspaceTopologySlotMetadata slotMetadata,
                                                                    String verticalAccessGroupId,
                                                                    boolean supportsVerticalAccess,
                                                                    int roomWidth,
                                                                    int roomLength,
                                                                    int roomHeight,
                                                                    MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                    List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                    int topVoidMargin,
                                                                    int bottomVoidMargin,
                                                                    @Nullable MKWorkspacePaletteOverride paletteOverride) {
        return forTopologySlot(baseName, slotMetadata, verticalAccessGroupId, supportsVerticalAccess, roomWidth,
                roomLength, roomHeight, horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin,
                null, paletteOverride);
    }

    public static MKTowerWorkspaceFamilyDefinition forTopologySlot(String baseName,
                                                                    MKWorkspaceTopologySlotMetadata slotMetadata,
                                                                    String verticalAccessGroupId,
                                                                    boolean supportsVerticalAccess,
                                                                    int roomWidth,
                                                                    int roomLength,
                                                                    int roomHeight,
                                                                    MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                    List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                    int topVoidMargin,
                                                                    int bottomVoidMargin,
                                                                    @Nullable MKWorkspaceFoundationPolicy foundationPolicy,
                                                                    @Nullable MKWorkspacePaletteOverride paletteOverride) {
        return new MKTowerWorkspaceFamilyDefinition(baseName, slotMetadata, slotMetadata.topologySlotId(),
                verticalAccessGroupId, supportsVerticalAccess, roomWidth, roomLength, roomHeight,
                horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin,
                foundationPolicy, paletteOverride);
    }

    public static MKTowerWorkspaceFamilyDefinition forTowerStackSlot(String baseName,
                                                                     MKTowerWorkspaceStackSlot slot,
                                                                     String stackId,
                                                                     boolean supportsVerticalAccess,
                                                                     int roomWidth,
                                                                     int roomLength,
                                                                     int roomHeight,
                                                                     MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                     List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                     int topVoidMargin,
                                                                     int bottomVoidMargin,
                                                                     @Nullable MKWorkspaceFoundationPolicy foundationPolicy,
                                                                     @Nullable MKWorkspacePaletteOverride paletteOverride) {
        return forTowerStackSlot(baseName, slot, stackId, stackId, supportsVerticalAccess, roomWidth, roomLength,
                roomHeight, horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin,
                foundationPolicy, paletteOverride);
    }

    public static MKTowerWorkspaceFamilyDefinition forTowerStackSlot(String baseName,
                                                                     MKTowerWorkspaceStackSlot slot,
                                                                     String stackId,
                                                                     String verticalAccessGroupId,
                                                                     boolean supportsVerticalAccess,
                                                                     int roomWidth,
                                                                     int roomLength,
                                                                     int roomHeight,
                                                                     MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
                                                                     List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits,
                                                                     int topVoidMargin,
                                                                     int bottomVoidMargin,
                                                                     @Nullable MKWorkspaceFoundationPolicy foundationPolicy,
                                                                     @Nullable MKWorkspacePaletteOverride paletteOverride) {
        return forTopologySlot(
                baseName,
                MKWorkspaceTopologySlotMetadata.fromTowerStackSlot(slot, stackId),
                verticalAccessGroupId,
                supportsVerticalAccess,
                roomWidth,
                roomLength,
                roomHeight,
                horizontalExtrusionMode,
                horizontalExits,
                topVoidMargin,
                bottomVoidMargin,
                foundationPolicy,
                paletteOverride);
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createDefaults() {
        return createDefaults(MKWorkspaceDimensions.defaultDimensions());
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createDefaults(MKWorkspaceDimensions dimensions) {
        return List.of(
                forTowerStackSlot("entry", MKTowerWorkspaceStackSlot.ENTRY, PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(new MKWorkspaceFamilyHorizontalExitDefinition(Direction.SOUTH,
                                MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY, "main_opening",
                                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION)),
                        0, 0, null, null),
                forTowerStackSlot("floor_main", MKTowerWorkspaceStackSlot.MAIN_FLOOR, PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null),
                forTowerStackSlot("top_cap_approach", MKTowerWorkspaceStackSlot.TOP_CAP_APPROACH,
                        PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null),
                forTowerStackSlot("top_cap", MKTowerWorkspaceStackSlot.TOP_CAP, PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null),
                forTowerStackSlot("basement_entry", MKTowerWorkspaceStackSlot.BASEMENT_ENTRY,
                        PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null),
                forTowerStackSlot("basement_main", MKTowerWorkspaceStackSlot.BASEMENT_FLOOR,
                        PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null),
                forTowerStackSlot("basement_cap_approach", MKTowerWorkspaceStackSlot.BASEMENT_CAP_APPROACH,
                        PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null),
                forTowerStackSlot("basement_cap", MKTowerWorkspaceStackSlot.BASEMENT_CAP,
                        PRIMARY_TOWER_STACK_ID, true,
                        0, 0, 0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                        null, null)
        );
    }

    public static List<MKTowerWorkspaceFamilyDefinition> createWalledKeepDefaults(MKWorkspaceDimensions dimensions) {
        int keepHeight = 7;
        int centerWidth = doubledOddFootprint(Math.max(9, dimensions.roomWidth()));
        int centerLength = doubledOddFootprint(Math.max(9, dimensions.roomLength()));
        int cornerFootprint = 7;
        ArrayList<MKTowerWorkspaceFamilyDefinition> families = new ArrayList<>();
        families.addAll(createKeepTowerStackDefaults("keep_center", "keep.center",
                centerWidth, centerLength, keepHeight));
        families.addAll(createKeepTowerStackDefaults("keep_corner_shared", "keep.corner.shared",
                cornerFootprint, cornerFootprint, keepHeight));
        families.add(forTopologySlot("keep_gate_main",
                MKWorkspaceTopologySlotMetadata.explicit("keep.gate.main", "entry", "room", false),
                "keep.gate", false,
                7, 5, keepHeight,
                MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION, List.of(), 0, 0,
                null, null));
        return List.copyOf(families);
    }

    private static List<MKTowerWorkspaceFamilyDefinition> createKeepTowerStackDefaults(String basePrefix,
                                                                                       String stackId,
                                                                                       int width,
                                                                                       int length,
                                                                                       int height) {
        return MKTowerWorkspaceStackSlot.familyDefaultOrder().stream()
                .map(slot -> forTowerStackSlot(
                        slot.baseName(basePrefix),
                        slot,
                        stackId,
                        true,
                        0,
                        0,
                        0,
                        MKWorkspaceHorizontalExtrusionMode.NO_EXTRUSION,
                        List.of(),
                        0,
                        0,
                        null,
                        null))
                .toList();
    }

    private static int doubledOddFootprint(int footprint) {
        int oddFootprint = footprint % 2 == 0 ? footprint + 1 : footprint;
        return Math.max(3, (oddFootprint * 2) - 1);
    }

    private static List<MKWorkspaceFamilyHorizontalExitDefinition> normalizeFamilyExits(
            MKWorkspaceTopologySlotMetadata slotMetadata, boolean supportsVerticalAccess,
            List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> normalized = new ArrayList<>(exits);
        boolean hasVerticalExit = normalized.stream()
                .anyMatch(MKWorkspaceFamilyHorizontalExitDefinition::isVerticalAccess);
        if (supportsVerticalAccess && !hasVerticalExit) {
            for (Direction direction : defaultVerticalAccessDirections(slotMetadata)) {
                normalized.add(MKWorkspaceFamilyHorizontalExitDefinition.verticalAccess(direction));
            }
        }
        return List.copyOf(normalized);
    }

    private static List<Direction> defaultVerticalAccessDirections(MKWorkspaceTopologySlotMetadata slotMetadata) {
        return switch (slotMetadata.roleKind()) {
            case "entry", "floor", "cap_approach" -> List.of(Direction.UP, Direction.DOWN);
            case "cap" -> "terminal_bottom".equals(slotMetadata.pieceKind()) ?
                    List.of(Direction.UP) : List.of(Direction.DOWN);
            default -> List.of();
        };
    }

    public static MKTowerWorkspaceFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "tower workspace family definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "tower workspace family definition");
    }

    public List<String> validate(List<MKTowerWorkspaceFamilyDefinition> allFamilies,
                                 int maxRoomHeight,
                                 MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                 MKWorkspaceResolvedFamilySettings resolvedFamily) {
        return validate(allFamilies, maxRoomHeight, verticalAccessSpec,
                resolvedFamily.roomWidth(), resolvedFamily.roomLength(), resolvedFamily.roomHeight(),
                resolvedFamily.slotMetadata());
    }

    private List<String> validate(List<MKTowerWorkspaceFamilyDefinition> allFamilies,
                                  int maxRoomHeight,
                                  MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                  int resolvedRoomWidth,
                                  int resolvedRoomLength,
                                  int resolvedRoomHeight,
                                  MKWorkspaceTopologySlotMetadata slotMetadata) {
        List<String> errors = new ArrayList<>();
        if (baseName.isBlank()) {
            errors.add("tower workspace family base name cannot be blank");
        }
        long duplicates = allFamilies.stream().filter(def -> def.baseName.equals(baseName)).count();
        if (duplicates > 1) {
            errors.add("tower workspace family base name must be unique: " + baseName);
        }
        if ("linear_run".equals(slotMetadata.roleKind())) {
            errors.add("tower workspace room families cannot use linear run role");
        }
        if (topologySlotId.isBlank()) {
            errors.add("family " + baseName + " topology slot id cannot be blank");
        }
        if (supportsVerticalAccess() && verticalAccessGroupId.isBlank()) {
            errors.add("family " + baseName + " shaft-enabled room must declare a vertical access group id");
        }
        validateOdd(errors, "family " + baseName + " room width", resolvedRoomWidth, 3);
        validateOdd(errors, "family " + baseName + " room length", resolvedRoomLength, 3);
        if (supportsVerticalAccess()) {
            if (resolvedRoomHeight < 3) {
                errors.add("family " + baseName + " shaft-enabled room height must be at least 3");
            }
            if (resolvedRoomWidth < verticalAccessSpec.shaftSize()) {
                errors.add("family " + baseName + " room width must be at least the shared shaft size");
            }
            if (resolvedRoomLength < verticalAccessSpec.shaftSize()) {
                errors.add("family " + baseName + " room length must be at least the shared shaft size");
            }
            if (resolvedRoomHeight != maxRoomHeight) {
                errors.add("family " + baseName + " shaft-enabled room height must match topology height " +
                        maxRoomHeight);
            }
        } else if (resolvedRoomHeight < MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT ||
                resolvedRoomHeight > maxRoomHeight) {
            errors.add("family " + baseName + " non-shaft room height must be within topology range " +
                    MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT + "-" + maxRoomHeight);
        }
        if (supportsVerticalAccess() && (topVoidMargin > 0 || bottomVoidMargin > 0)) {
            errors.add("family " + baseName + " shaft-enabled room cannot define top or bottom void margins");
        }
        if (!supportsVerticalAccess()) {
            int reducedRoomHeight = resolvedRoomHeight - topVoidMargin - bottomVoidMargin;
            if (reducedRoomHeight < MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT) {
                errors.add("family " + baseName + " non-shaft room height after void margins must be at least " +
                        MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
            }
        }
        if (foundationPolicyOverride != null) {
            errors.addAll(foundationPolicyOverride.validate("family " + baseName));
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
        LinkedHashSet<Direction> seenDirections = new LinkedHashSet<>();
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
            if (exit.openingProfileId().isBlank()) {
                errors.add("family " + baseName + " horizontal exit opening profile cannot be blank");
            }
        }
        return errors;
    }

    public static boolean defaultSupportsVerticalAccess(MKWorkspaceTopologySlotMetadata slotMetadata) {
        return !defaultVerticalAccessDirections(slotMetadata).isEmpty();
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

    public MKWorkspaceTopologySlotMetadata slotMetadata() {
        return slotMetadata;
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

    public OptionalInt roomWidthOverrideOpt() {
        return roomWidth > 0 ? OptionalInt.of(roomWidth) : OptionalInt.empty();
    }

    public int roomLength() {
        return roomLength;
    }

    public OptionalInt roomLengthOverrideOpt() {
        return roomLength > 0 ? OptionalInt.of(roomLength) : OptionalInt.empty();
    }

    public int roomHeight() {
        return roomHeight;
    }

    public OptionalInt roomHeightOverrideOpt() {
        return roomHeight > 0 ? OptionalInt.of(roomHeight) : OptionalInt.empty();
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
        return foundationPolicyOverride == null ? MKWorkspaceFoundationPolicy.none() : foundationPolicyOverride;
    }

    public Optional<MKWorkspaceFoundationPolicy> foundationPolicyOverrideOpt() {
        return Optional.ofNullable(foundationPolicyOverride);
    }

    @Nullable
    public MKWorkspaceFoundationPolicy foundationPolicyOverride() {
        return foundationPolicyOverride;
    }

    @Override
    public String paletteFamilyId() {
        return baseName;
    }

    @Override
    public Optional<String> paletteTopologyGroupIdOpt() {
        return Optional.of(slotMetadata.topologyGroupId());
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

    private static void validateOdd(List<String> errors, String label, int value, int min) {
        if (value < min) {
            errors.add(label + " must be at least " + min);
        }
        if (value % 2 == 0) {
            errors.add(label + " must be odd");
        }
    }

    private static String defaultVerticalAccessGroupId(boolean supportsVerticalAccess) {
        return supportsVerticalAccess ? "tower.core" : "";
    }
}

