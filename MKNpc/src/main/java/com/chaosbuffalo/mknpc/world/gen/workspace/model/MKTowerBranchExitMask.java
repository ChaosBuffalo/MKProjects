package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public record MKTowerBranchExitMask(int bits) {
    private static final int NORTH_BIT = 1;
    private static final int EAST_BIT = 2;
    private static final int SOUTH_BIT = 4;
    private static final int WEST_BIT = 8;
    public static final MKTowerBranchExitMask NONE = new MKTowerBranchExitMask(0);

    public MKTowerBranchExitMask {
        if ((bits & ~0xF) != 0) {
            throw new IllegalArgumentException("tower branch exit mask must fit in four cardinal bits");
        }
    }

    public static MKTowerBranchExitMask fromDirections(Iterable<Direction> directions) {
        int bits = 0;
        for (Direction direction : directions) {
            bits |= bitFor(direction);
        }
        return new MKTowerBranchExitMask(bits);
    }

    public static MKTowerBranchExitMask fromSerializedName(String value) {
        if (value == null || value.isBlank() || value.equals("none")) {
            return NONE;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);
        for (char ch : normalized.toCharArray()) {
            switch (ch) {
                case 'n' -> directions.add(Direction.NORTH);
                case 'e' -> directions.add(Direction.EAST);
                case 's' -> directions.add(Direction.SOUTH);
                case 'w' -> directions.add(Direction.WEST);
                default -> throw new IllegalArgumentException("Unknown tower branch exit mask token: " + ch);
            }
        }
        return fromDirections(directions);
    }

    public String getSerializedName() {
        if (bits == 0) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        if (has(Direction.NORTH)) {
            builder.append('n');
        }
        if (has(Direction.EAST)) {
            builder.append('e');
        }
        if (has(Direction.SOUTH)) {
            builder.append('s');
        }
        if (has(Direction.WEST)) {
            builder.append('w');
        }
        return builder.toString();
    }

    public boolean has(Direction direction) {
        return (bits & bitFor(direction)) != 0;
    }

    public List<Direction> directions() {
        List<Direction> result = new ArrayList<>();
        if (has(Direction.NORTH)) {
            result.add(Direction.NORTH);
        }
        if (has(Direction.EAST)) {
            result.add(Direction.EAST);
        }
        if (has(Direction.SOUTH)) {
            result.add(Direction.SOUTH);
        }
        if (has(Direction.WEST)) {
            result.add(Direction.WEST);
        }
        return result;
    }

    public boolean isEmpty() {
        return bits == 0;
    }

    public int exitCount() {
        return Integer.bitCount(bits);
    }

    private static int bitFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_BIT;
            case EAST -> EAST_BIT;
            case SOUTH -> SOUTH_BIT;
            case WEST -> WEST_BIT;
            default -> throw new IllegalArgumentException("Branch exits support cardinal directions only");
        };
    }
}
