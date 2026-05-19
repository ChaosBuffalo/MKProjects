package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.IntStream;

public final class MKWorkspaceCodecs {
    public static final Codec<UUID> UUID_CODEC = Codec.INT_STREAM.comapFlatMap(
            stream -> {
                int[] ints = stream.toArray();
                if (ints.length != 4) {
                    return DataResult.error(() -> "expected four ints for UUID, got " + ints.length);
                }
                return DataResult.success(uuidFromIntArray(ints));
            },
            uuid -> Arrays.stream(uuidToIntArray(uuid))
    );
    public static final Codec<BlockPos> BLOCK_POS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(BlockPos::getX),
            Codec.INT.fieldOf("y").forGetter(BlockPos::getY),
            Codec.INT.fieldOf("z").forGetter(BlockPos::getZ)
    ).apply(instance, BlockPos::new));
    public static final Codec<BoundingBox> BOUNDING_BOX_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("minX").forGetter(BoundingBox::minX),
            Codec.INT.fieldOf("minY").forGetter(BoundingBox::minY),
            Codec.INT.fieldOf("minZ").forGetter(BoundingBox::minZ),
            Codec.INT.fieldOf("maxX").forGetter(BoundingBox::maxX),
            Codec.INT.fieldOf("maxY").forGetter(BoundingBox::maxY),
            Codec.INT.fieldOf("maxZ").forGetter(BoundingBox::maxZ)
    ).apply(instance, BoundingBox::new));
    public static final Codec<Direction> DIRECTION_CODEC = Codec.STRING.comapFlatMap(
            value -> {
                Direction direction = Direction.byName(value);
                return direction != null ? DataResult.success(direction) :
                        DataResult.error(() -> "unknown direction: " + value);
            },
            Direction::getSerializedName
    );
    public static final Codec<MKStructureFamilyType> FAMILY_TYPE_CODEC = Codec.STRING.xmap(
            MKStructureFamilyType::fromSerializedName, MKStructureFamilyType::getSerializedName
    );
    public static final Codec<MKVerticalAccessPlacement> VERTICAL_ACCESS_PLACEMENT_CODEC = Codec.STRING.xmap(
            MKVerticalAccessPlacement::fromSerializedName, MKVerticalAccessPlacement::getSerializedName
    );
    public static final Codec<MKWorkspaceStairMode> STAIR_MODE_CODEC = Codec.STRING.xmap(
            MKWorkspaceStairMode::fromSerializedName, MKWorkspaceStairMode::getSerializedName
    );
    public static final Codec<MKWorkspaceStairRiseType> STAIR_RISE_TYPE_CODEC = Codec.STRING.xmap(
            MKWorkspaceStairRiseType::fromSerializedName, MKWorkspaceStairRiseType::getSerializedName
    );
    public static final Codec<MKWorkspaceHorizontalExitPathKind> HORIZONTAL_EXIT_PATH_KIND_CODEC = Codec.STRING.xmap(
            MKWorkspaceHorizontalExitPathKind::fromSerializedName, MKWorkspaceHorizontalExitPathKind::getSerializedName
    );
    public static final Codec<MKWorkspaceHorizontalExitConnectionMode> HORIZONTAL_EXIT_CONNECTION_MODE_CODEC = Codec.STRING.xmap(
            MKWorkspaceHorizontalExitConnectionMode::fromSerializedName,
            MKWorkspaceHorizontalExitConnectionMode::getSerializedName
    );
    public static final Codec<MKWorkspaceHorizontalExtrusionMode> HORIZONTAL_EXTRUSION_MODE_CODEC = Codec.STRING.xmap(
            MKWorkspaceHorizontalExtrusionMode::fromSerializedName, MKWorkspaceHorizontalExtrusionMode::getSerializedName
    );
    public static final Codec<MKWorkspaceFoundationMode> FOUNDATION_MODE_CODEC = Codec.STRING.xmap(
            MKWorkspaceFoundationMode::fromSerializedName, MKWorkspaceFoundationMode::getSerializedName
    );
    public static final Codec<MKWorkspaceLinearRunKind> LINEAR_RUN_KIND_CODEC = Codec.STRING.xmap(
            MKWorkspaceLinearRunKind::fromSerializedName, MKWorkspaceLinearRunKind::getSerializedName
    );
    public static final Codec<MKWorkspaceLinearRunProjection> LINEAR_RUN_PROJECTION_CODEC = Codec.STRING.xmap(
            MKWorkspaceLinearRunProjection::fromSerializedName, MKWorkspaceLinearRunProjection::getSerializedName
    );
    public static final Codec<MKWorkspaceLinearRunPieceShape> LINEAR_RUN_SHAPE_CODEC = Codec.STRING.xmap(
            MKWorkspaceLinearRunPieceShape::fromSerializedName, MKWorkspaceLinearRunPieceShape::getSerializedName
    );
    public static final Codec<MKConnectorRole> CONNECTOR_ROLE_CODEC = Codec.STRING.xmap(
            MKConnectorRole::fromSerializedName, MKConnectorRole::getSerializedName
    );
    private MKWorkspaceCodecs() {
    }

    public static <T> CompoundTag encodeNbt(Codec<T> codec, T value, String label) {
        Tag tag = codec.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(error -> MKNpc.LOGGER.error("Failed to encode {}: {}", label, error))
                .orElseThrow(() -> new IllegalStateException("Failed to encode " + label));
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        throw new IllegalStateException("Expected compound tag when encoding " + label + " but got " +
                tag.getClass().getSimpleName());
    }

    public static <T> T parseNbt(Codec<T> codec, CompoundTag tag, String label) {
        return codec.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(error -> MKNpc.LOGGER.error("Failed to parse {}: {}", label, error))
                .orElseThrow(() -> new IllegalStateException("Failed to parse " + label));
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[]{
                (int) (most >> 32),
                (int) most,
                (int) (least >> 32),
                (int) least
        };
    }

    private static UUID uuidFromIntArray(int[] ints) {
        long most = ((long) ints[0] << 32) | (ints[1] & 0xFFFFFFFFL);
        long least = ((long) ints[2] << 32) | (ints[3] & 0xFFFFFFFFL);
        return new UUID(most, least);
    }
}
