package com.chaosbuffalo.mkcore.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SingleCapabilityProvider<CapTarget, CapType> implements ICapabilitySerializable<CompoundTag> {
    protected final CapType data;
    private final LazyOptional<CapType> capOpt;

    public SingleCapabilityProvider(CapTarget attached) {
        data = makeData(attached);
        capOpt = LazyOptional.of(() -> data);
    }

    protected abstract CapType makeData(CapTarget target);

    protected abstract Capability<CapType> getCapability();

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability().orEmpty(cap, capOpt);
    }

    public void invalidate() {
        capOpt.invalidate();
    }
}
