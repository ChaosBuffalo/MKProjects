package com.chaosbuffalo.mkultra.entities.projectiles;

import com.chaosbuffalo.mkultra.abilities.misc.ProjectileAbility;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

public class AbilityProjectileEntity extends SpriteTrailProjectileEntity{
    protected Supplier<? extends ProjectileAbility> abilitySupplier;

    public AbilityProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void setAbility(Supplier<? extends ProjectileAbility> abilitySupplier) {
        this.abilitySupplier = abilitySupplier;
    }

    @Override
    protected boolean onImpact(Entity caster, HitResult result, int amplifier) {
        if (abilitySupplier != null && !this.level.isClientSide && caster instanceof LivingEntity casterLiving) {
            ProjectileAbility ability = abilitySupplier.get();
            if (ability != null) {
                return ability.onImpact(this, casterLiving, result, amplifier);
            }
        }
        return false;
    }

    @Override
    public float getGravityVelocity() {
        return 0.0f;
    }

    @Override
    protected TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }
}
