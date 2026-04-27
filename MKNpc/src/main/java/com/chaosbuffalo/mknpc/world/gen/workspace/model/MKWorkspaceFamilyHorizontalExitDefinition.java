package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public record MKWorkspaceFamilyHorizontalExitDefinition(
        Direction direction,
        MKWorkspaceHorizontalExitPathKind pathKind,
        String openingProfileId,
        MKWorkspaceHorizontalExitConnectionMode connectionMode
) {
    public static final Codec<MKWorkspaceFamilyHorizontalExitDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.DIRECTION_CODEC.fieldOf("direction").forGetter(MKWorkspaceFamilyHorizontalExitDefinition::direction),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_PATH_KIND_CODEC.fieldOf("pathKind")
                    .forGetter(MKWorkspaceFamilyHorizontalExitDefinition::pathKind),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKWorkspaceFamilyHorizontalExitDefinition::openingProfileId),
            MKWorkspaceCodecs.HORIZONTAL_EXIT_CONNECTION_MODE_CODEC.optionalFieldOf("connectionMode",
                            MKWorkspaceHorizontalExitConnectionMode.HALLWAY)
                    .forGetter(MKWorkspaceFamilyHorizontalExitDefinition::connectionMode)
    ).apply(instance, MKWorkspaceFamilyHorizontalExitDefinition::new));

    public MKWorkspaceFamilyHorizontalExitDefinition(Direction direction,
                                                     MKWorkspaceHorizontalExitPathKind pathKind,
                                                     String openingProfileId) {
        this(direction, pathKind, openingProfileId, MKWorkspaceHorizontalExitConnectionMode.HALLWAY);
    }

    public static MKWorkspaceFamilyHorizontalExitDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace family horizontal exit definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace family horizontal exit definition");
    }
}
