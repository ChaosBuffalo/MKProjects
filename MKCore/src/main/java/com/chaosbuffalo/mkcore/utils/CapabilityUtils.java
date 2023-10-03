package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.capabilities.SingleSerializableCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Function;

public class CapabilityUtils {

    public static <T, C extends INBTSerializable<CompoundTag>> ICapabilitySerializable<CompoundTag> provider(
            Capability<C> capability, Function<T, C> capFactory, T attached) {
        return new SingleSerializableCapabilityProvider<T, C>(attached) {

            @Override
            protected C makeData(T attached) {
                return capFactory.apply(attached);
            }

            @Override
            protected Capability<C> getCapability() {
                return capability;
            }
        };
    }
}
