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
            case MAIN_ROOM -> direction == Direction.WEST || direction == Direction.EAST;
            case BRANCH_ROOM -> direction == Direction.SOUTH;
        };
    }

    public boolean optionalBranchExitDirection(Direction direction) {
        return direction.getAxis().isHorizontal() && !requiredExitDirection(direction);
    }

    private static String defaultLabel(MKWorkspaceFloorRoomKind kind) {
        return switch (kind) {
            case MAIN_ROOM -> "Main Room";
            case BRANCH_ROOM -> "Branch Room";
        };
    }

    private static List<MKWorkspaceFamilyHorizontalExitDefinition> normalizeExits(
            MKWorkspaceFloorRoomKind kind,
            List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        Map<Direction, MKWorkspaceFamilyHorizontalExitDefinition> byDirection = new EnumMap<>(Direction.class);
        if (exits != null) {
            for (MKWorkspaceFamilyHorizontalExitDefinition exit : exits) {
                if (exit == null || exit.direction().getAxis().isVertical()) {
                    continue;
                }
                if (requiredDirection(kind, exit.direction())) {
                    continue;
                }
                byDirection.put(exit.direction(), optionalBranchExit(exit.direction()));
            }
        }
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> resolved = new ArrayList<>();
        if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM) {
            resolved.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                    Direction.WEST,
                    MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY,
                    "main_opening",
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
            resolved.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                    Direction.EAST,
                    MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                    "main_opening",
                    MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
            ));
        } else {
            resolved.add(optionalBranchExit(Direction.SOUTH));
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
            case MAIN_ROOM -> direction == Direction.WEST || direction == Direction.EAST;
            case BRANCH_ROOM -> direction == Direction.SOUTH;
        };
    }

    private static MKWorkspaceFamilyHorizontalExitDefinition optionalBranchExit(Direction direction) {
        return new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                MKWorkspaceHorizontalExitPathKind.BRANCH,
                "branch_opening",
                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN
        );
    }

    private static int oddAtLeast(int minimum, int value) {
        int normalized = Math.max(minimum, value);
        return normalized % 2 == 0 ? normalized + 1 : normalized;
    }
}
