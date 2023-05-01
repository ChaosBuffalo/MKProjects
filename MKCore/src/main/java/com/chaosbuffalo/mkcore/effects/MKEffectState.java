package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public abstract class MKEffectState {
    protected int maxStacks = -1;

    public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
        return instance.isTickReady();
    }

    protected int clampMaxStacks(int newValue) {
        if (maxStacks == -1)
            return newValue;
        return Math.min(maxStacks, newValue);
    }

    public void combine(MKActiveEffect existing, MKActiveEffect otherInstance) {
        MKCore.LOGGER.debug("MKEffectState.combine {} + {}", existing, otherInstance);
        if (otherInstance.getDuration() > existing.getDuration()) {
            existing.setDuration(otherInstance.getDuration());
        }
        int newStacks = clampMaxStacks(existing.getStackCount() + otherInstance.getStackCount());
        existing.setStackCount(newStacks);
        MKCore.LOGGER.debug("MKEffectState.combine result {}", existing);
    }

    public abstract boolean performEffect(IMKEntityData targetData, MKActiveEffect instance);

    protected boolean isEffectSource(Entity entity, MKActiveEffect activeEffect) {
        return entity.getUUID().equals(activeEffect.getSourceId());
    }

    public void setMaxStacks(int max) {
        this.maxStacks = max;
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
    }

    public void deserializeStorage(CompoundTag stateTag) {
        if (stateTag.contains("maxStacks")) {
            maxStacks = stateTag.getInt("maxStacks");
        }
    }
}
