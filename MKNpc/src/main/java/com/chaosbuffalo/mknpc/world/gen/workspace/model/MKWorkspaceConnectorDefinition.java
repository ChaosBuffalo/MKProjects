package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class MKWorkspaceConnectorDefinition {
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");

    private final MKConnectorRole role;
    private final Direction facing;
    private final BlockPos relativePos;
    private final int openingWidth;
    private final int openingHeight;
    private final ResourceLocation jigsawName;
    private final ResourceLocation jigsawTarget;
    private final ResourceLocation targetPool;
    private final ResourceLocation incomingPool;

    public MKWorkspaceConnectorDefinition(MKConnectorRole role, Direction facing, BlockPos relativePos, int openingWidth,
                                          int openingHeight, ResourceLocation jigsawName, ResourceLocation jigsawTarget,
                                          ResourceLocation targetPool, ResourceLocation incomingPool) {
        this.role = role;
        this.facing = facing;
        this.relativePos = relativePos;
        this.openingWidth = openingWidth;
        this.openingHeight = openingHeight;
        this.jigsawName = jigsawName;
        this.jigsawTarget = jigsawTarget;
        this.targetPool = targetPool;
        this.incomingPool = incomingPool;
    }

    public static MKWorkspaceConnectorDefinition fromTag(CompoundTag tag) {
        return new MKWorkspaceConnectorDefinition(
                MKConnectorRole.fromSerializedName(tag.getString("role")),
                Direction.byName(tag.getString("facing")),
                MKWorkspaceNbtUtil.blockPosFromTag(tag.getCompound("relativePos")),
                tag.getInt("openingWidth"),
                tag.getInt("openingHeight"),
                ResourceLocation.parse(tag.getString("jigsawName")),
                ResourceLocation.parse(tag.getString("jigsawTarget")),
                ResourceLocation.parse(tag.getString("targetPool")),
                tag.contains("incomingPool") ? ResourceLocation.parse(tag.getString("incomingPool")) : EMPTY_POOL
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("role", role.getSerializedName());
        tag.putString("facing", facing.getSerializedName());
        tag.put("relativePos", MKWorkspaceNbtUtil.blockPosToTag(relativePos));
        tag.putInt("openingWidth", openingWidth);
        tag.putInt("openingHeight", openingHeight);
        tag.putString("jigsawName", jigsawName.toString());
        tag.putString("jigsawTarget", jigsawTarget.toString());
        tag.putString("targetPool", targetPool.toString());
        tag.putString("incomingPool", incomingPool.toString());
        return tag;
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
