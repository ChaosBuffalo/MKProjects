package com.chaosbuffalo.mkultra.entities.projectiles;

import com.chaosbuffalo.mkultra.entities.IMKRenderAsItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class SpriteTrailProjectileEntity extends TrailProjectileEntity implements IMKRenderAsItem {

    private final ItemStack stack;

    public SpriteTrailProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn, ItemStack stack) {
        super(entityTypeIn, worldIn);
        this.stack = stack;
    }

    @Override
    public ItemStack getItem() {
        return stack;
    }
}
