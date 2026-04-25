package com.chaosbuffalo.mknpc.block_entities;

import com.chaosbuffalo.mknpc.init.MKNpcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class MKWorkspaceDevBlockEntity extends BlockEntity {
    @Nullable
    private UUID workspaceId;

    public MKWorkspaceDevBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MKNpcBlockEntityTypes.MK_WORKSPACE_DEV_BLOCK_ENTITY_TYPE.get(), blockPos, blockState);
    }

    @Nullable
    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(@Nullable UUID workspaceId) {
        this.workspaceId = workspaceId;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (workspaceId != null) {
            tag.putUUID("workspaceId", workspaceId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        workspaceId = tag.hasUUID("workspaceId") ? tag.getUUID("workspaceId") : null;
    }
}
