package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class MKWorkspaceConnectorDefinition {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    public static final Codec<MKWorkspaceConnectorDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.CONNECTOR_ROLE_CODEC.fieldOf("role").forGetter(MKWorkspaceConnectorDefinition::role),
            MKWorkspaceCodecs.DIRECTION_CODEC.fieldOf("facing").forGetter(MKWorkspaceConnectorDefinition::facing),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("relativePos").forGetter(MKWorkspaceConnectorDefinition::relativePos),
            Codec.INT.fieldOf("openingWidth").forGetter(MKWorkspaceConnectorDefinition::openingWidth),
            Codec.INT.fieldOf("openingHeight").forGetter(MKWorkspaceConnectorDefinition::openingHeight),
            Codec.INT.optionalFieldOf("lateralOffset", 0).forGetter(MKWorkspaceConnectorDefinition::lateralOffset),
            Codec.INT.optionalFieldOf("verticalOffset", 0).forGetter(MKWorkspaceConnectorDefinition::verticalOffset),
            ResourceLocation.CODEC.fieldOf("jigsawName").forGetter(MKWorkspaceConnectorDefinition::jigsawName),
            ResourceLocation.CODEC.fieldOf("jigsawTarget").forGetter(MKWorkspaceConnectorDefinition::jigsawTarget),
            ResourceLocation.CODEC.fieldOf("targetPool").forGetter(MKWorkspaceConnectorDefinition::targetPool),
            ResourceLocation.CODEC.optionalFieldOf("incomingPool", EMPTY_POOL).forGetter(MKWorkspaceConnectorDefinition::incomingPool)
    ).apply(instance, MKWorkspaceConnectorDefinition::new));

    private final MKConnectorRole role;
    private final Direction facing;
    private final BlockPos relativePos;
    private final int openingWidth;
    private final int openingHeight;
    private final int lateralOffset;
    private final int verticalOffset;
    private final ResourceLocation jigsawName;
    private final ResourceLocation jigsawTarget;
    private final ResourceLocation targetPool;
    private final ResourceLocation incomingPool;

    public MKWorkspaceConnectorDefinition(MKConnectorRole role, Direction facing, BlockPos relativePos, int openingWidth,
                                          int openingHeight, int lateralOffset, int verticalOffset,
                                          ResourceLocation jigsawName, ResourceLocation jigsawTarget,
                                          ResourceLocation targetPool, ResourceLocation incomingPool) {
        this.role = role;
        this.facing = facing;
        this.relativePos = relativePos;
        this.openingWidth = openingWidth;
        this.openingHeight = openingHeight;
        this.lateralOffset = lateralOffset;
        this.verticalOffset = verticalOffset;
        this.jigsawName = jigsawName;
        this.jigsawTarget = jigsawTarget;
        this.targetPool = targetPool;
        this.incomingPool = incomingPool;
    }

    public static MKWorkspaceConnectorDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace connector definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace connector definition");
    }

    public MKConnectorRole role() {
        return role;
    }

    public Direction facing() {
        return facing;
    }

    public BlockPos relativePos() {
        return relativePos;
    }

    public int openingWidth() {
        return openingWidth;
    }

    public int openingHeight() {
        return openingHeight;
    }

    public int lateralOffset() {
        return lateralOffset;
    }

    public int verticalOffset() {
        return verticalOffset;
    }

    public ResourceLocation jigsawName() {
        return jigsawName;
    }

    public ResourceLocation jigsawTarget() {
        return jigsawTarget;
    }

    public ResourceLocation targetPool() {
        return targetPool;
    }

    public ResourceLocation incomingPool() {
        return incomingPool;
    }
}
