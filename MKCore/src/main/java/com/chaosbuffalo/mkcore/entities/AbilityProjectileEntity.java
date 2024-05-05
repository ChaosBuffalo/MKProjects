package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

public class AbilityProjectileEntity extends SpriteTrailProjectileEntity{
    protected Supplier<? extends ProjectileAbility> abilitySupplier;
    protected float gravityVelocity;
    protected int castTime;

    public AbilityProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        gravityVelocity = 0.0f;
        castTime = 0;
    }

    public void setCastTime(int castTime) {
        this.castTime = castTime;
        setPreFireTicks(castTime);
    }

    @Override
    public float getScale() {
        return Math.min((float)(tickCount) / castTime, 1.0f);
    }

    public void setAbility(Supplier<? extends ProjectileAbility> abilitySupplier) {
        this.abilitySupplier = abilitySupplier;
    }

    public void setGravityVelocity(float gravityVelocity) {
        this.gravityVelocity = gravityVelocity;
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
    protected boolean onAirProc(Entity caster, int amplifier) {
        if (abilitySupplier != null && !this.level.isClientSide && caster instanceof LivingEntity casterLiving)
        {
            ProjectileAbility ability = abilitySupplier.get();
            if (ability != null) {
                return ability.onAirProc(this, casterLiving, amplifier);
            }
        }
        return false;

    }

    @Override
    protected boolean onGroundProc(Entity caster, int amplifier) {
        if (abilitySupplier != null && !this.level.isClientSide && caster instanceof LivingEntity casterLiving)
        {
            ProjectileAbility ability = abilitySupplier.get();
            if (ability != null) {
                return ability.onGroundProc(this, casterLiving, amplifier);
            }
        }
        return false;
    }

    @Override
    public float getGravityVelocity() {
        return gravityVelocity;
    }

    @Override
    protected TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(getGravityVelocity());
        buffer.writeInt(castTime);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        setGravityVelocity(additionalData.readFloat());
        setCastTime(additionalData.readInt());
    }
}
