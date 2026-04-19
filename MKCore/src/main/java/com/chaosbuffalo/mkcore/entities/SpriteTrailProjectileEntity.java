package com.chaosbuffalo.mkcore.entities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class SpriteTrailProjectileEntity extends TrailProjectileEntity implements IMKRenderAsItem {

    private ItemStack stack;

    public SpriteTrailProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn, ItemStack stack) {
        super(entityTypeIn, worldIn);
        this.stack = stack;
    }

    public SpriteTrailProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        this(entityTypeIn, worldIn, ItemStack.EMPTY);
    }

    public void setItem(ItemStack item) {
        stack = item == null ? ItemStack.EMPTY : item;
    }

    @Override
    public ItemStack getItem() {
        return stack;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        ItemStack.STREAM_CODEC.encode(buffer, stack);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        stack = additionalData.readBoolean()
                ? ItemStack.STREAM_CODEC.decode(additionalData)
                : ItemStack.EMPTY;
    }
}
