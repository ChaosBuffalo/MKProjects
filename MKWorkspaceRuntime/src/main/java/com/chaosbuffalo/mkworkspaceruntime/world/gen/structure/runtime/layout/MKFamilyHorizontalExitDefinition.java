package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceCodecs;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKFamilyHorizontalExitDefinition(
        Direction direction,
        MKHorizontalExitPathKind pathKind,
        String openingProfileId,
        MKWorkspaceHorizontalExitConnectionMode connectionMode,
        int sideOffset,
        int verticalOffset,
        @Nullable MKWorkspaceHorizontalExtrusionMode horizontalExtrusionModeOverride
) {
    public static final Codec<MKFamilyHorizontalExitDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.DIRECTION_CODEC.fieldOf("direction").forGetter(MKFamilyHorizontalExitDefinition::direction),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_PATH_KIND_CODEC.fieldOf("pathKind")
                    .forGetter(MKFamilyHorizontalExitDefinition::pathKind),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKFamilyHorizontalExitDefinition::openingProfileId),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_CONNECTION_MODE_CODEC.optionalFieldOf("connectionMode",
                            MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN)
                    .forGetter(MKFamilyHorizontalExitDefinition::connectionMode),
            Codec.INT.optionalFieldOf("sideOffset", 0).forGetter(MKFamilyHorizontalExitDefinition::sideOffset),
            Codec.INT.optionalFieldOf("verticalOffset", 0).forGetter(MKFamilyHorizontalExitDefinition::verticalOffset),
            MKWorkspaceCodecs.HORIZONTAL_EXTRUSION_MODE_CODEC.optionalFieldOf("horizontalExtrusionModeOverride")
                    .forGetter(exit -> Optional.ofNullable(exit.horizontalExtrusionModeOverride()))
    ).apply(instance, (direction, pathKind, openingProfileId, connectionMode, sideOffset, verticalOffset,
                       horizontalExtrusionModeOverride) -> new MKFamilyHorizontalExitDefinition(direction,
            pathKind, openingProfileId, connectionMode, sideOffset, verticalOffset,
            horizontalExtrusionModeOverride.orElse(null))));

    public MKFamilyHorizontalExitDefinition(Direction direction,
                                                     MKHorizontalExitPathKind pathKind,
                                                     String openingProfileId) {
        this(direction, pathKind, openingProfileId, MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN);
    }

    public MKFamilyHorizontalExitDefinition(Direction direction,
                                                     MKHorizontalExitPathKind pathKind,
                                                     String openingProfileId,
                                                     MKWorkspaceHorizontalExitConnectionMode connectionMode) {
        this(direction, pathKind, openingProfileId, connectionMode, 0, 0);
    }

    public MKFamilyHorizontalExitDefinition(Direction direction,
                                                     MKHorizontalExitPathKind pathKind,
                                                     String openingProfileId,
                                                     MKWorkspaceHorizontalExitConnectionMode connectionMode,
                                                     int sideOffset,
                                                     int verticalOffset) {
        this(direction, pathKind, openingProfileId, connectionMode, sideOffset, verticalOffset, null);
    }

    public static MKFamilyHorizontalExitDefinition verticalAccess(Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) {
            throw new IllegalArgumentException("vertical access exits must face up or down");
        }
        return new MKFamilyHorizontalExitDefinition(direction,
                MKHorizontalExitPathKind.VERTICAL_ACCESS,
                "",
                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION,
                0,
                0,
                null);
    }

    public boolean isVerticalAccess() {
        return direction.getAxis().isVertical() || pathKind == MKHorizontalExitPathKind.VERTICAL_ACCESS;
    }

    public static MKFamilyHorizontalExitDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace family horizontal exit definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace family horizontal exit definition");
    }
}
