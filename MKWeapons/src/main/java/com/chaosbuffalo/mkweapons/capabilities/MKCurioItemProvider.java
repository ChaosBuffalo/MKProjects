package com.chaosbuffalo.mkweapons.capabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class MKCurioItemProvider extends ThirdPartyCapProvider<ItemStack, ICurio> {
    private final MKCurioItemHandler ourHandler;

    public MKCurioItemProvider(ItemStack attached) {
        super(attached);
        ourHandler = (MKCurioItemHandler) data;
    }

    @Override
    ICurio makeData(ItemStack var1) {
        return new MKCurioItemHandler(var1);
    }

    @Override
    Capability<ICurio> getCapability() {
        return CuriosCapability.ITEM;
    }

    @Override
    public CompoundTag serializeNBT() {
        return ourHandler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ourHandler.deserializeNBT(nbt);
    }
}