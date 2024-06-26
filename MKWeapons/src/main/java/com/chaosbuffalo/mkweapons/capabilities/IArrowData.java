package com.chaosbuffalo.mkweapons.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public interface IArrowData extends INBTSerializable<CompoundTag> {
    ItemStack getShootingWeapon();

    AbstractArrow getArrow();

    void setShootingWeapon(ItemStack shootingWeapon);
}
