package com.chaosbuffalo.mkweapons.capabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeaponDataProvider implements ICapabilitySerializable<CompoundTag> {

    private final WeaponDataHandler data;

    public WeaponDataProvider(ItemStack item){
        data = new WeaponDataHandler();
        data.attach(item);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return WeaponsCapabilities.WEAPON_DATA_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> data));
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }
}
