package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MKFloorRoomProfile(
        String id,
        String label,
        MKFloorRoomKind kind,
        int width,
        int length,
        int height,
        int weight,
        Optional<MKWorkspacePaletteOverride> paletteOverride,
        boolean randomizeMainExit,
        List<MKFamilyHorizontalExitDefinition> horizontalExits
) {
    public static final String INHERITED_MAIN_OPENING_PROFILE_ID = "__inherited_main__";
    public static final String INHERITED_BRANCH_OPENING_PROFILE_ID = "__inherited_branch__";
    public static final String INHERITED_LINK_OPENING_PROFILE_ID = "__inherited_link__";

    public static final Codec<MKFloorRoomProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(MKFloorRoomProfile::id),
            Codec.STRING.optionalFieldOf("label", "").forGetter(MKFloorRoomProfile::label),
            MKFloorRoomKind.CODEC.fieldOf("kind").forGetter(MKFloorRoomProfile::kind),
            Codec.INT.optionalFieldOf("width", 7).forGetter(MKFloorRoomProfile::width),
            Codec.INT.optionalFieldOf("length", 7).forGetter(MKFloorRoomProfile::length),
            Codec.INT.optionalFieldOf("height", 7).forGetter(MKFloorRoomProfile::height),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(MKFloorRoomProfile::weight),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                    .forGetter(MKFloorRoomProfile::paletteOverride),
            Codec.BOOL.optionalFieldOf("randomize_main_exit", false)
                    .forGetter(MKFloorRoomProfile::randomizeMainExit),
            MKFamilyHorizontalExitDefinition.CODEC.listOf()
                    .optionalFieldOf("horizontal_exits", List.of())
                    .forGetter(MKFloorRoomProfile::horizontalExits)
    ).apply(instance, MKFloorRoomProfile::new));

    public MKFloorRoomProfile {
        id = id == null || id.isBlank() ? kind.getSerializedName() : id;
        label = label == null || label.isBlank() ? defaultLabel(kind) : label;
        width = oddAtLeast(3, width);
        length = oddAtLeast(3, length);
        height = Math.max(MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT, height);
        weight = Math.max(1, weight);
        paletteOverride = paletteOverride == null ? Optional.empty() : paletteOverride;
        randomizeMainExit = kind == MKFloorRoomKind.MAIN_ROOM && randomizeMainExit;
        horizontalExits = normalizeExits(kind, horizontalExits);
    }

    public MKFloorRoomProfile(String id,
                                       String label,
                                       MKFloorRoomKind kind,
                                       int width,
                                       int length,
                                       int height,
                                       int weight,
                                       Optional<MKWorkspacePaletteOverride> paletteOverride,
                                       List<MKFamilyHorizontalExitDefinition> horizontalExits) {
        this(id, label, kind, width, length, height, weight, paletteOverride, false, horizontalExits);
    }

    public MKFloorRoomProfile(String id,
                                       String label,
                                       MKFloorRoomKind kind,
                                       int width,
                                       int length,
                                       int height,
                                       int weight,
                                       Optional<MKWorkspacePaletteOverride> paletteOverride) {
        this(id, label, kind, width, length, height, weight, paletteOverride, false, List.of());
    }

    public static MKFloorRoomProfile defaults(MKFloorRoomKind kind,
                                                       int width, int length, int height) {
        return new MKFloorRoomProfile(
                kind.getSerializedName(),
                defaultLabel(kind),
                kind,
                width,
                length,
                height,
                1,
                Optional.empty(),
                false,
                defaultHorizontalExits(kind)
        );
    }

    public MKFloorRoomProfile withWidth(int value) {
        return new MKFloorRoomProfile(id, label, kind, value, length, height, weight, paletteOverride,
                randomizeMainExit, horizontalExits);
    }

    public MKFloorRoomProfile withLength(int value) {
        return new MKFloorRoomProfile(id, label, kind, width, value, height, weight, paletteOverride,
                randomizeMainExit, horizontalExits);
    }

    public MKFloorRoomProfile withHeight(int value) {
        return new MKFloorRoomProfile(id, label, kind, width, length, value, weight, paletteOverride,
                randomizeMainExit, horizontalExits);
    }

    public MKFloorRoomProfile withIdentity(String id, String label) {
        return new MKFloorRoomProfile(id, label, kind, width, length, height, weight, paletteOverride,
                randomizeMainExit, horizontalExits);
    }

    public MKFloorRoomProfile withHorizontalExits(List<MKFamilyHorizontalExitDefinition> exits) {
        return new MKFloorRoomProfile(id, label, kind, width, length, height, weight, paletteOverride,
                randomizeMainExit, exits);
    }

    public MKFloorRoomProfile withRandomizeMainExit(boolean value) {
        return new MKFloorRoomProfile(id, label, kind, width, length, height, weight, paletteOverride,
                value, horizontalExits);
    }

    public MKFloorRoomProfile withPaletteOverride(Optional<MKWorkspacePaletteOverride> value) {
        return new MKFloorRoomProfile(id, label, kind, width, length, height, weight,
                value == null ? Optional.empty() : value, randomizeMainExit, horizontalExits);
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
                (kind != MKFloorRoomKind.MAIN_ROOM || mainExitDirection().orElse(null) != direction);
    }

    public boolean linkCandidateExitDirection(Direction direction) {
        return direction.getAxis().isHorizontal() &&
                !requiredExitDirection(direction) &&
                (kind != MKFloorRoomKind.MAIN_ROOM || mainExitDirection().orElse(null) != direction);
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
                .filter(exit -> exit.pathKind() == MKHorizontalExitPathKind.MAIN_EXIT)
                .map(MKFamilyHorizontalExitDefinition::direction)
                .findFirst();
    }

    public List<Direction> randomizedMainExitCandidates() {
        if (!randomizeMainExit || kind != MKFloorRoomKind.MAIN_ROOM) {
            return mainExitDirection()
                    .map(List::of)
                    .orElse(List.of());
        }
        ArrayList<Direction> directions = new ArrayList<>();
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.WEST)) {
            boolean enabled = horizontalExits.stream().anyMatch(exit -> exit.direction() == direction &&
                    (exit.pathKind() == MKHorizontalExitPathKind.MAIN_EXIT ||
                            exit.pathKind() == MKHorizontalExitPathKind.BRANCH));
            if (enabled) {
                directions.add(direction);
            }
        }
        return List.copyOf(directions);
    }

    public MKFloorRoomProfile withResolvedRandomMainExit(Direction direction) {
        if (!randomizeMainExit || kind != MKFloorRoomKind.MAIN_ROOM || !mainExitDirection(direction)) {
            return this;
        }
        ArrayList<MKFamilyHorizontalExitDefinition> exits = new ArrayList<>();
        for (MKFamilyHorizontalExitDefinition exit : horizontalExits) {
            if (exit.pathKind() != MKHorizontalExitPathKind.MAIN_EXIT &&
                    exit.pathKind() != MKHorizontalExitPathKind.BRANCH &&
                    exit.pathKind() != MKHorizontalExitPathKind.LINK_CANDIDATE) {
                exits.add(exit);
            }
        }
        exits.add(new MKFamilyHorizontalExitDefinition(
                direction,
                MKHorizontalExitPathKind.MAIN_EXIT,
                INHERITED_MAIN_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        ));
        for (Direction candidate : randomizedMainExitCandidates()) {
            if (candidate != direction) {
                exits.add(optionalBranchExit(candidate));
            }
        }
        for (MKFamilyHorizontalExitDefinition exit : horizontalExits) {
            if (exit.pathKind() == MKHorizontalExitPathKind.LINK_CANDIDATE) {
                exits.add(linkCandidateExit(exit.direction()));
            }
        }
        return new MKFloorRoomProfile(id, label, kind, width, length, height, weight, paletteOverride,
                false, exits);
    }

    public boolean terminalBranchRoom() {
        return kind == MKFloorRoomKind.BRANCH_CAP;
    }

    private static String defaultLabel(MKFloorRoomKind kind) {
        return switch (kind) {
            case MAIN_ROOM -> "Main Room";
            case BRANCH_ROOM -> "Branch Room";
            case BRANCH_CAP -> "Branch Cap";
            case MAIN_CAP_APPROACH -> "Main Cap Approach";
            case MAIN_CAP -> "Main Cap";
        };
    }

    private static List<MKFamilyHorizontalExitDefinition> defaultHorizontalExits(
            MKFloorRoomKind kind) {
        if (kind != MKFloorRoomKind.BRANCH_CAP) {
            return List.of();
        }
        return List.of(
                linkCandidateExit(Direction.NORTH),
                linkCandidateExit(Direction.EAST),
                linkCandidateExit(Direction.WEST)
        );
    }

    private static List<MKFamilyHorizontalExitDefinition> normalizeExits(
            MKFloorRoomKind kind,
            List<MKFamilyHorizontalExitDefinition> exits) {
        Map<Direction, MKFamilyHorizontalExitDefinition> byDirection = new EnumMap<>(Direction.class);
        Map<Direction, MKFamilyHorizontalExitDefinition> linkByDirection = new EnumMap<>(Direction.class);
        Direction mainExitDirection = Direction.NORTH;
        if (exits != null) {
            for (MKFamilyHorizontalExitDefinition exit : exits) {
                if (exit == null || exit.direction().getAxis().isVertical()) {
                    continue;
                }
                if (kind.hasMainExit() &&
                        exit.pathKind() == MKHorizontalExitPathKind.MAIN_EXIT &&
                        validMainExitDirection(kind, exit.direction())) {
                    mainExitDirection = exit.direction();
                }
                if (!requiredDirection(kind, exit.direction()) &&
                        allowsOptionalBranchExits(kind) &&
                        exit.pathKind() == MKHorizontalExitPathKind.BRANCH) {
                    byDirection.put(exit.direction(), optionalBranchExit(exit.direction()));
                }
                if (!requiredDirection(kind, exit.direction()) &&
                        exit.pathKind() == MKHorizontalExitPathKind.LINK_CANDIDATE) {
                    linkByDirection.put(exit.direction(), linkCandidateExit(exit.direction()));
                }
            }
        }
        if (kind.hasMainExit()) {
            byDirection.remove(mainExitDirection);
            linkByDirection.remove(mainExitDirection);
        }
        byDirection.keySet().forEach(linkByDirection::remove);
        ArrayList<MKFamilyHorizontalExitDefinition> resolved = new ArrayList<>();
        if (kind.usesMainPath()) {
            resolved.add(new MKFamilyHorizontalExitDefinition(
                    Direction.SOUTH,
                    kind.isMainPathEnding() ?
                            MKHorizontalExitPathKind.MAIN_ENDING_ENTRY :
                            MKHorizontalExitPathKind.MAIN_ENTRY,
                    INHERITED_MAIN_OPENING_PROFILE_ID,
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
            if (kind.hasMainExit()) {
                resolved.add(new MKFamilyHorizontalExitDefinition(
                        mainExitDirection,
                        MKHorizontalExitPathKind.MAIN_EXIT,
                        INHERITED_MAIN_OPENING_PROFILE_ID,
                        MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
                ));
            }
        } else {
            resolved.add(kind == MKFloorRoomKind.BRANCH_CAP ?
                    branchCapEntry(Direction.SOUTH) : optionalBranchExit(Direction.SOUTH));
        }
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            if (!requiredDirection(kind, direction) && byDirection.containsKey(direction)) {
                resolved.add(byDirection.get(direction));
            }
        }
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            if (!requiredDirection(kind, direction) && linkByDirection.containsKey(direction)) {
                resolved.add(linkByDirection.get(direction));
            }
        }
        return List.copyOf(resolved);
    }

    private static boolean requiredDirection(MKFloorRoomKind kind, Direction direction) {
        return switch (kind) {
            case MAIN_ROOM, MAIN_CAP_APPROACH, MAIN_CAP -> direction == Direction.SOUTH;
            case BRANCH_ROOM, BRANCH_CAP -> direction == Direction.SOUTH;
        };
    }

    private static boolean validMainExitDirection(MKFloorRoomKind kind, Direction direction) {
        return kind.hasMainExit() &&
                direction.getAxis().isHorizontal() &&
                direction != Direction.SOUTH;
    }

    private static boolean allowsOptionalBranchExits(MKFloorRoomKind kind) {
        return kind != MKFloorRoomKind.BRANCH_CAP &&
                kind != MKFloorRoomKind.MAIN_CAP;
    }

    private static MKFamilyHorizontalExitDefinition optionalBranchExit(Direction direction) {
        return new MKFamilyHorizontalExitDefinition(
                direction,
                MKHorizontalExitPathKind.BRANCH,
                INHERITED_BRANCH_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        );
    }

    private static MKFamilyHorizontalExitDefinition branchCapEntry(Direction direction) {
        return new MKFamilyHorizontalExitDefinition(
                direction,
                MKHorizontalExitPathKind.BRANCH_CAP_ENTRY,
                INHERITED_BRANCH_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        );
    }

    private static MKFamilyHorizontalExitDefinition linkCandidateExit(Direction direction) {
        return new MKFamilyHorizontalExitDefinition(
                direction,
                MKHorizontalExitPathKind.LINK_CANDIDATE,
                INHERITED_LINK_OPENING_PROFILE_ID,
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        );
    }

    private static int oddAtLeast(int minimum, int value) {
        int normalized = Math.max(minimum, value);
        return normalized % 2 == 0 ? normalized + 1 : normalized;
    }
}
