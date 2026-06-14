package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKWorkspaceFamilyHorizontalExitDefinition(
        Direction direction,
        MKWorkspaceHorizontalExitPathKind pathKind,
        String openingProfileId,
        MKWorkspaceHorizontalExitConnectionMode connectionMode,
        int sideOffset,
        int verticalOffset,
        @Nullable MKWorkspaceHorizontalExtrusionMode horizontalExtrusionModeOverride
) {
    public static final Codec<MKWorkspaceFamilyHorizontalExitDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.DIRECTION_CODEC.fieldOf("direction").forGetter(MKWorkspaceFamilyHorizontalExitDefinition::direction),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_PATH_KIND_CODEC.fieldOf("pathKind")
                    .forGetter(MKWorkspaceFamilyHorizontalExitDefinition::pathKind),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKWorkspaceFamilyHorizontalExitDefinition::openingProfileId),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_CONNECTION_MODE_CODEC.optionalFieldOf("connectionMode",
                            MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN)
                    .forGetter(MKWorkspaceFamilyHorizontalExitDefinition::connectionMode),
            Codec.INT.optionalFieldOf("sideOffset", 0).forGetter(MKWorkspaceFamilyHorizontalExitDefinition::sideOffset),
            Codec.INT.optionalFieldOf("verticalOffset", 0).forGetter(MKWorkspaceFamilyHorizontalExitDefinition::verticalOffset),
            MKWorkspaceCodecs.HORIZONTAL_EXTRUSION_MODE_CODEC.optionalFieldOf("horizontalExtrusionModeOverride")
                    .forGetter(exit -> Optional.ofNullable(exit.horizontalExtrusionModeOverride()))
    ).apply(instance, (direction, pathKind, openingProfileId, connectionMode, sideOffset, verticalOffset,
                       horizontalExtrusionModeOverride) -> new MKWorkspaceFamilyHorizontalExitDefinition(direction,
            pathKind, openingProfileId, connectionMode, sideOffset, verticalOffset,
            horizontalExtrusionModeOverride.orElse(null))));

    public MKWorkspaceFamilyHorizontalExitDefinition(Direction direction,
                                                     MKWorkspaceHorizontalExitPathKind pathKind,
                                                     String openingProfileId) {
        this(direction, pathKind, openingProfileId, MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN);
    }

    public MKWorkspaceFamilyHorizontalExitDefinition(Direction direction,
                                                     MKWorkspaceHorizontalExitPathKind pathKind,
                                                     String openingProfileId,
                                                     MKWorkspaceHorizontalExitConnectionMode connectionMode) {
        this(direction, pathKind, openingProfileId, connectionMode, 0, 0);
    }

    public MKWorkspaceFamilyHorizontalExitDefinition(Direction direction,
                                                     MKWorkspaceHorizontalExitPathKind pathKind,
                                                     String openingProfileId,
                                                     MKWorkspaceHorizontalExitConnectionMode connectionMode,
                                                     int sideOffset,
                                                     int verticalOffset) {
        this(direction, pathKind, openingProfileId, connectionMode, sideOffset, verticalOffset, null);
    }

    public static MKWorkspaceFamilyHorizontalExitDefinition verticalAccess(Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) {
            throw new IllegalArgumentException("vertical access exits must face up or down");
        }
        return new MKWorkspaceFamilyHorizontalExitDefinition(direction,
                MKWorkspaceHorizontalExitPathKind.VERTICAL_ACCESS,
                "",
                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION,
                0,
                0,
                null);
    }

    public boolean isVerticalAccess() {
        return direction.getAxis().isVertical() || pathKind == MKWorkspaceHorizontalExitPathKind.VERTICAL_ACCESS;
    }

    public static MKWorkspaceFamilyHorizontalExitDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace family horizontal exit definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace family horizontal exit definition");
    }
}
