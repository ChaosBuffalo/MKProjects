package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class MKEffectState {
    public enum StackCombinePolicy {
        ADD_STACKS,
        REFRESH_DURATION,
        REPLACE_STACKS
    }

    protected int maxStacks = -1;
    protected StackCombinePolicy stackCombinePolicy = StackCombinePolicy.ADD_STACKS;

    public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
        return instance.getBehaviour().isReady();
    }

    protected int clampMaxStacks(int newValue) {
        if (maxStacks == -1)
            return newValue;
        return Math.min(maxStacks, newValue);
    }

    public void combine(MKActiveEffect existing, MKActiveEffect otherInstance) {
        if (otherInstance.getDuration() > existing.getDuration()) {
            existing.setDuration(otherInstance.getDuration());
        }
        int newStacks = switch (stackCombinePolicy) {
            case ADD_STACKS -> clampMaxStacks(existing.getStackCount() + otherInstance.getStackCount());
            case REFRESH_DURATION -> clampMaxStacks(existing.getStackCount());
            case REPLACE_STACKS -> clampMaxStacks(otherInstance.getStackCount());
        };
        existing.setStackCount(newStacks);
    }

    public abstract boolean performEffect(IMKEntityData targetData, MKActiveEffect instance);

    protected boolean isEffectSource(Entity entity, MKActiveEffect activeEffect) {
        return entity.getUUID().equals(activeEffect.getSourceId());
    }

    public void setMaxStacks(int max) {
        this.maxStacks = max;
    }

    public int getMaxStacks() {
        return maxStacks;
    }

    public void setStackCombinePolicy(StackCombinePolicy stackCombinePolicy) {
        this.stackCombinePolicy = stackCombinePolicy;
    }

    public StackCombinePolicy getStackCombinePolicy() {
        return stackCombinePolicy;
    }

    @Deprecated
    @Nullable
    protected Entity findEntity(Entity entity, UUID entityId, Level world) {
        if (entity != null)
            return entity;
        if (!world.isClientSide()) {
            return ((ServerLevel) world).getEntity(entityId);
        }
        return null;
    }

    @Deprecated
    @Nullable
    protected Entity findEntity(Entity entity, UUID entityId, IMKEntityData targetData) {
        return findEntity(entity, entityId, targetData.getEntity().getCommandSenderWorld());
    }

    public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
        return true;
    }

    public boolean validateOnLoad(MKActiveEffect activeEffect) {
        return true;
    }

    public void serializeStorage(CompoundTag stateTag) {
        if (maxStacks != -1) {
            stateTag.putInt("maxStacks", maxStacks);
        }
        if (stackCombinePolicy != StackCombinePolicy.ADD_STACKS) {
            stateTag.putString("stackCombinePolicy", stackCombinePolicy.name());
        }
    }

    public void deserializeStorage(CompoundTag stateTag) {
        if (stateTag.contains("maxStacks")) {
            maxStacks = stateTag.getInt("maxStacks");
        }
        if (stateTag.contains("stackCombinePolicy")) {
            stackCombinePolicy = StackCombinePolicy.valueOf(stateTag.getString("stackCombinePolicy"));
        }
    }
}
