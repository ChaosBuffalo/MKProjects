package com.chaosbuffalo.mkweapons.capabilities;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class ArrowDataHandler implements IArrowData {

    private final AbstractArrow arrow;
    private ItemStack shootingWeapon;

    public ArrowDataHandler(AbstractArrow arrow) {
        this.arrow = arrow;
        shootingWeapon = ItemStack.EMPTY;
    }

    @Override
    public ItemStack getShootingWeapon() {
        return shootingWeapon;
    }

    @Override
    public void setShootingWeapon(ItemStack shootingWeapon) {
        this.shootingWeapon = shootingWeapon;
    }

    @Override
    public AbstractArrow getArrow() {
        return arrow;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, NonNullList.of(shootingWeapon));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, itemStacks);
        shootingWeapon = itemStacks.get(0);
    }
}
