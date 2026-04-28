package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public record MKWorkspaceFamilyHorizontalExitDefinition(
        Direction direction,
        MKWorkspaceHorizontalExitPathKind pathKind,
        String openingProfileId,
        MKWorkspaceHorizontalExitConnectionMode connectionMode,
        int sideOffset,
        int verticalOffset
) {
    public static final Codec<MKWorkspaceFamilyHorizontalExitDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.DIRECTION_CODEC.fieldOf("direction").forGetter(MKWorkspaceFamilyHorizontalExitDefinition::direction),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_PATH_KIND_CODEC.fieldOf("pathKind")
                    .forGetter(MKWorkspaceFamilyHorizontalExitDefinition::pathKind),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKWorkspaceFamilyHorizontalExitDefinition::openingProfileId),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_CONNECTION_MODE_CODEC.optionalFieldOf("connectionMode",
                            MKWorkspaceHorizontalExitConnectionMode.HALLWAY)
                    .forGetter(MKWorkspaceFamilyHorizontalExitDefinition::connectionMode),
            Codec.INT.optionalFieldOf("sideOffset", 0).forGetter(MKWorkspaceFamilyHorizontalExitDefinition::sideOffset),
            Codec.INT.optionalFieldOf("verticalOffset", 0).forGetter(MKWorkspaceFamilyHorizontalExitDefinition::verticalOffset)
    ).apply(instance, MKWorkspaceFamilyHorizontalExitDefinition::new));

    public MKWorkspaceFamilyHorizontalExitDefinition(Direction direction,
                                                     MKWorkspaceHorizontalExitPathKind pathKind,
                                                     String openingProfileId) {
        this(direction, pathKind, openingProfileId, MKWorkspaceHorizontalExitConnectionMode.HALLWAY);
    }

    public MKWorkspaceFamilyHorizontalExitDefinition(Direction direction,
                                                     MKWorkspaceHorizontalExitPathKind pathKind,
                                                     String openingProfileId,
                                                     MKWorkspaceHorizontalExitConnectionMode connectionMode) {
        this(direction, pathKind, openingProfileId, connectionMode, 0, 0);
    }

    public static MKWorkspaceFamilyHorizontalExitDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace family horizontal exit definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace family horizontal exit definition");
    }
}
