package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceInsertAttachmentFace implements StringRepresentable {
    NORTH("north", Direction.NORTH),
    SOUTH("south", Direction.SOUTH),
    WEST("west", Direction.WEST),
    EAST("east", Direction.EAST),
    BOTTOM("bottom", Direction.DOWN),
    TOP("top", Direction.UP);

    public static final Codec<MKWorkspaceInsertAttachmentFace> CODEC = StringRepresentable.fromEnum(
            MKWorkspaceInsertAttachmentFace::values);

    private final String serializedName;
    private final Direction direction;

    MKWorkspaceInsertAttachmentFace(String serializedName, Direction direction) {
        this.serializedName = serializedName;
        this.direction = direction;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public Direction direction() {
        return direction;
    }

    public static MKWorkspaceInsertAttachmentFace fromDirection(Direction direction) {
        for (MKWorkspaceInsertAttachmentFace face : values()) {
            if (face.direction == direction) {
                return face;
            }
        }
        throw new IllegalArgumentException("Unsupported insert attachment direction: " + direction);
    }

    public boolean isHorizontal() {
        return direction.getAxis().isHorizontal();
    }

    public boolean isVertical() {
        return direction.getAxis().isVertical();
    }
}
