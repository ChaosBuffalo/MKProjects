package com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.UUID;

public record MKWorkspaceInsertSocketChangePayload(
        UUID hostPieceId,
        BlockPos socketWorldPos,
        Direction socketFacing,
        boolean createFamily,
        String familyId,
        int width,
        int height,
        int depth,
        int faceUOffset,
        int faceVOffset,
        String hostFinalState,
        String templateJigsawFinalState
) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    public static final Codec<MKWorkspaceInsertSocketChangePayload> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUID_CODEC.fieldOf("hostPieceId").forGetter(MKWorkspaceInsertSocketChangePayload::hostPieceId),
                    BlockPos.CODEC.fieldOf("socketWorldPos").forGetter(MKWorkspaceInsertSocketChangePayload::socketWorldPos),
                    Direction.CODEC.fieldOf("socketFacing").forGetter(MKWorkspaceInsertSocketChangePayload::socketFacing),
                    Codec.BOOL.fieldOf("createFamily").forGetter(MKWorkspaceInsertSocketChangePayload::createFamily),
                    Codec.STRING.fieldOf("familyId").forGetter(MKWorkspaceInsertSocketChangePayload::familyId),
                    Codec.INT.optionalFieldOf("width", 1).forGetter(MKWorkspaceInsertSocketChangePayload::width),
                    Codec.INT.optionalFieldOf("height", 1).forGetter(MKWorkspaceInsertSocketChangePayload::height),
                    Codec.INT.optionalFieldOf("depth", 1).forGetter(MKWorkspaceInsertSocketChangePayload::depth),
                    Codec.INT.optionalFieldOf("faceUOffset", 0).forGetter(MKWorkspaceInsertSocketChangePayload::faceUOffset),
                    Codec.INT.optionalFieldOf("faceVOffset", 0).forGetter(MKWorkspaceInsertSocketChangePayload::faceVOffset),
                    Codec.STRING.optionalFieldOf("hostFinalState", "minecraft:air")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::hostFinalState),
                    Codec.STRING.optionalFieldOf("templateJigsawFinalState", "minecraft:air")
                            .forGetter(MKWorkspaceInsertSocketChangePayload::templateJigsawFinalState)
            ).apply(instance, MKWorkspaceInsertSocketChangePayload::new));
}
