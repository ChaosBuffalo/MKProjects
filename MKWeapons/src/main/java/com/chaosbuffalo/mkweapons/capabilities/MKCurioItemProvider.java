package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkcore.capabilities.SingleCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class MKCurioItemProvider extends SingleCapabilityProvider<ItemStack, ICurio> {
    private final MKCurioItemHandler ourHandler;

    public MKCurioItemProvider(ItemStack attached) {
        super(attached);
        ourHandler = (MKCurioItemHandler) data;
    }

    @Override
    protected ICurio makeData(ItemStack target) {
        return new MKCurioItemHandler(target);
    }

    @Override
    protected Capability<ICurio> getCapability() {
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