package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventProvenance;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AbilityProjectileEntity extends SpriteTrailProjectileEntity{
    protected Supplier<? extends ProjectileAbility> abilitySupplier;
    protected float gravityVelocity;
    protected int castTime;
    @Nullable
    protected ResourceLocation explicitAbilityId;
    @Nullable
    protected AbilityEventProvenance eventProvenance;

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

    public @Nullable ResourceLocation getAbilityId() {
        ProjectileAbility ability = abilitySupplier != null ? abilitySupplier.get() : null;
        return ability != null ? ability.getAbilityId() : explicitAbilityId;
    }

    public void setAbilityId(@Nullable ResourceLocation explicitAbilityId) {
        this.explicitAbilityId = explicitAbilityId;
    }

    public void setEventProvenance(@Nullable AbilityEventProvenance eventProvenance) {
        this.eventProvenance = eventProvenance;
    }

    public @Nullable AbilityEventProvenance getEventProvenance() {
        return eventProvenance;
    }

    public void setGravityVelocity(float gravityVelocity) {
        this.gravityVelocity = gravityVelocity;
    }

    @Override
    protected boolean onImpact(Entity caster, HitResult result, int amplifier) {
        if (!this.level().isClientSide && caster instanceof LivingEntity casterLiving
                && MKCore.getAbilityRuntimeService().handleProjectileImpact(this, casterLiving, result)) {
            return true;
        }
        if (abilitySupplier != null && !this.level().isClientSide && caster instanceof LivingEntity casterLiving) {
            ProjectileAbility ability = abilitySupplier.get();
            if (ability != null) {
                return ability.onImpact(this, casterLiving, result, amplifier);
            }
        }
        return false;
    }

    @Override
    protected boolean onAirProc(Entity caster, int amplifier) {
        if (!this.level().isClientSide && caster instanceof LivingEntity casterLiving
                && MKCore.getAbilityRuntimeService().handleProjectileAirTick(this, casterLiving)) {
            return true;
        }
        if (abilitySupplier != null && !this.level().isClientSide && caster instanceof LivingEntity casterLiving)
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
        if (!this.level().isClientSide && caster instanceof LivingEntity casterLiving
                && MKCore.getAbilityRuntimeService().handleProjectileGroundTick(this, casterLiving)) {
            return true;
        }
        if (abilitySupplier != null && !this.level().isClientSide && caster instanceof LivingEntity casterLiving)
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
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(getGravityVelocity());
        buffer.writeInt(castTime);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        setGravityVelocity(additionalData.readFloat());
        setCastTime(additionalData.readInt());
    }
}
