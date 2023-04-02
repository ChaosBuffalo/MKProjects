package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

public class MKSkeletonEntity extends MKEntity  {
    public static final String DEFAULT = "default";

    public MKSkeletonEntity(EntityType<? extends MKSkeletonEntity> type, Level worldIn) {
        super(type, worldIn);
        setCurrentModelLook(DEFAULT);
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return MKEntity.registerAttributes(attackDamage, movementSpeed)
                .add(MKAttributes.SHADOW_RESISTANCE, 0.25)
                .add(MKAttributes.HOLY_RESISTANCE, -0.25);
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }


    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected SoundEvent getShootSound() {
        return SoundEvents.SKELETON_SHOOT;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

}
