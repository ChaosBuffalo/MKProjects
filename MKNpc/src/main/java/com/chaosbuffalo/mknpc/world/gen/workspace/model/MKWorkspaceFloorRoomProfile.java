package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKWorkspaceFloorRoomProfile(
        String id,
        String label,
        MKWorkspaceFloorRoomKind kind,
        int width,
        int length,
        int height,
        int weight,
        Optional<MKWorkspacePaletteOverride> paletteOverride,
        List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits
) {
    public static final String INHERITED_MAIN_OPENING_PROFILE_ID = "__inherited_main__";
    public static final String INHERITED_BRANCH_OPENING_PROFILE_ID = "__inherited_branch__";

    public static final Codec<MKWorkspaceFloorRoomProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(MKWorkspaceFloorRoomProfile::id),
            Codec.STRING.optionalFieldOf("label", "").forGetter(MKWorkspaceFloorRoomProfile::label),
            MKWorkspaceFloorRoomKind.CODEC.fieldOf("kind").forGetter(MKWorkspaceFloorRoomProfile::kind),
            Codec.INT.optionalFieldOf("width", 7).forGetter(MKWorkspaceFloorRoomProfile::width),
            Codec.INT.optionalFieldOf("length", 7).forGetter(MKWorkspaceFloorRoomProfile::length),
            Codec.INT.optionalFieldOf("height", 7).forGetter(MKWorkspaceFloorRoomProfile::height),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(MKWorkspaceFloorRoomProfile::weight),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                    .forGetter(MKWorkspaceFloorRoomProfile::paletteOverride),
            MKWorkspaceFamilyHorizontalExitDefinition.CODEC.listOf()
                    .optionalFieldOf("horizontal_exits", List.of())
                    .forGetter(MKWorkspaceFloorRoomProfile::horizontalExits)
    ).apply(instance, MKWorkspaceFloorRoomProfile::new));

    public MKWorkspaceFloorRoomProfile {
        id = id == null || id.isBlank() ? kind.getSerializedName() : id;
        label = label == null || label.isBlank() ? defaultLabel(kind) : label;
        width = oddAtLeast(3, width);
        length = oddAtLeast(3, length);
        height = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, height);
        weight = Math.max(1, weight);
        paletteOverride = paletteOverride == null ? Optional.empty() : paletteOverride;
        horizontalExits = normalizeExits(kind, horizontalExits);
    }

    public MKWorkspaceFloorRoomProfile(String id,
                                       String label,
                                       MKWorkspaceFloorRoomKind kind,
                                       int width,
                                       int length,
                                       int height,
                                       int weight,
                                       Optional<MKWorkspacePaletteOverride> paletteOverride) {
        this(id, label, kind, width, length, height, weight, paletteOverride, List.of());
    }

    public static MKWorkspaceFloorRoomProfile defaults(MKWorkspaceFloorRoomKind kind,
                                                       int width, int length, int height) {
        return new MKWorkspaceFloorRoomProfile(
                kind.getSerializedName(),
                defaultLabel(kind),
                kind,
                width,
                length,
                height,
                1,
                Optional.empty(),
                List.of()
        );
    }

    public MKWorkspaceFloorRoomProfile withWidth(int value) {
        return new MKWorkspaceFloorRoomProfile(id, label, kind, value, length, height, weight, paletteOverride,
                horizontalExits);
    }

    public MKWorkspaceFloorRoomProfile withLength(int value) {
        return new MKWorkspaceFloorRoomProfile(id, label, kind, width, value, height, weight, paletteOverride,
                horizontalExits);
    }

    public MKWorkspaceFloorRoomProfile withHeight(int value) {
        return new MKWorkspaceFloorRoomProfile(id, label, kind, width, length, value, weight, paletteOverride,
                horizontalExits);
    }

    public MKWorkspaceFloorRoomProfile withIdentity(String id, String label) {
        return new MKWorkspaceFloorRoomProfile(id, label, kind, width, length, height, weight, paletteOverride,
                horizontalExits);
    }

    public MKWorkspaceFloorRoomProfile withHorizontalExits(List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        return new MKWorkspaceFloorRoomProfile(id, label, kind, width, length, height, weight, paletteOverride, exits);
    }

    public boolean requiredExitDirection(Direction direction) {
        return switch (kind) {
            case MAIN_ROOM, MAIN_CAP_APPROACH, MAIN_CAP -> direction == Direction.SOUTH;
            case BRANCH_ROOM, BRANCH_CAP -> direction == Direction.SOUTH;
        };
    }

    public boolean optionalBranchExitDirection(Direction direction) {
        return direction.getAxis().isHorizontal() &&
                allowsOptionalBranchExits(kind) &&
                !requiredExitDirection(direction) &&
                (kind != MKWorkspaceFloorRoomKind.MAIN_ROOM || mainExitDirection().orElse(null) != direction);
    }

    public boolean mainExitDirection(Direction direction) {
        return kind.hasMainExit() &&
                direction.getAxis().isHorizontal() &&
                direction != Direction.SOUTH;
    }

    public Optional<Direction> mainExitDirection() {
        if (!kind.hasMainExit()) {
            return Optional.empty();
        }
        return horizontalExits.stream()
                .filter(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .map(MKWorkspaceFamilyHorizontalExitDefinition::direction)
                .findFirst();
    }

    public boolean terminalBranchRoom() {
        return kind == MKWorkspaceFloorRoomKind.BRANCH_CAP;
    }

    private static String defaultLabel(MKWorkspaceFloorRoomKind kind) {
        return switch (kind) {
            case MAIN_ROOM -> "Main Room";
            case BRANCH_ROOM -> "Branch Room";
            case BRANCH_CAP -> "Branch Cap";
            case MAIN_CAP_APPROACH -> "Main Cap Approach";
            case MAIN_CAP -> "Main Cap";
        };
    }

    private static List<MKWorkspaceFamilyHorizontalExitDefinition> normalizeExits(
            MKWorkspaceFloorRoomKind kind,
            List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        Map<Direction, MKWorkspaceFamilyHorizontalExitDefinition> byDirection = new EnumMap<>(Direction.class);
        Direction mainExitDirection = Direction.NORTH;
        if (exits != null) {
            for (MKWorkspaceFamilyHorizontalExitDefinition exit : exits) {
                if (exit == null || exit.direction().getAxis().isVertical()) {
                    continue;
                }
                if (kind.hasMainExit() &&
                        exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_EXIT &&
                        validMainExitDirection(kind, exit.direction())) {
                    mainExitDirection = exit.direction();
                }
                if (!requiredDirection(kind, exit.direction()) &&
                        allowsOptionalBranchExits(kind) &&
                        exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH) {
                    byDirection.put(exit.direction(), optionalBranchExit(exit.direction()));
                }
            }
        }
        if (kind.hasMainExit()) {
            byDirection.remove(mainExitDirection);
        }
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> resolved = new ArrayList<>();
        if (kind.usesMainPath()) {
            resolved.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                    Direction.SOUTH,
                    kind.isMainPathEnding() ?
                            MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY :
                            MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY,
                    INHERITED_MAIN_OPENING_PROFILE_ID,
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
            if (kind.hasMainExit()) {
                resolved.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                        mainExitDirection,
                        MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                        INHERITED_MAIN_OPENING_PROFILE_ID,
                        MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
                ));
            }
        } else {
            resolved.add(kind == MKWorkspaceFloorRoomKind.BRANCH_CAP ?
                    branchCapEntry(Direction.SOUTH) : optionalBranchExit(Direction.SOUTH));
        }
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            if (!requiredDirection(kind, direction) && byDirection.containsKey(direction)) {
                resolved.add(byDirection.get(direction));
            }
        }
        return List.copyOf(resolved);
    }

    private static boolean requiredDirection(MKWorkspaceFloorRoomKind kind, Direction direction) {
        return switch (kind) {
            case MAIN_ROOM, MAIN_CAP_APPROACH, MAIN_CAP -> direction == Direction.SOUTH;
            case BRANCH_ROOM, BRANCH_CAP -> direction == Direction.SOUTH;
        };
    }

    private static boolean validMainExitDirection(MKWorkspaceFloorRoomKind kind, Direction direction) {
        return kind.hasMainExit() &&
                direction.getAxis().isHorizontal() &&
                direction != Direction.SOUTH;
    }

    private static boolean allowsOptionalBranchExits(MKWorkspaceFloorRoomKind kind) {
        return kind != MKWorkspaceFloorRoomKind.BRANCH_CAP &&
                kind != MKWorkspaceFloorRoomKind.MAIN_CAP;
    }

    private static MKWorkspaceFamilyHorizontalExitDefinition optionalBranchExit(Direction direction) {
        return new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                MKWorkspaceHorizontalExitPathKind.BRANCH,
                INHERITED_BRANCH_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        );
    }

    private static MKWorkspaceFamilyHorizontalExitDefinition branchCapEntry(Direction direction) {
        return new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY,
                INHERITED_BRANCH_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        );
    }

    private static int oddAtLeast(int minimum, int value) {
        int normalized = Math.max(minimum, value);
        return normalized % 2 == 0 ? normalized + 1 : normalized;
    }
}
