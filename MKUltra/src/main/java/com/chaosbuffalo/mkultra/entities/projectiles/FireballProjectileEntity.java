package com.chaosbuffalo.mkultra.entities.projectiles;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FireballProjectileEntity extends AbilityProjectileEntity {

    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "fireball_trail");


    public FireballProjectileEntity(EntityType<? extends Projectile> entityTypeIn,
                                    Level worldIn) {
        super(entityTypeIn, worldIn);
        setItem(new ItemStack(MKUItems.fireballProjectileItem.get()));
        setAbility(MKUAbilities.FIREBALL);
        setDeathTime(GameConstants.TICKS_PER_SECOND * 5);
        setTrailAnimation(TRAIL_PARTICLES);
    }
}
