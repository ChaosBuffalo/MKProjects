package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;



public class MKGolemEntity extends MKEntity  {
    public static final String DEFAULT = "default";
    public MKGolemEntity(EntityType<? extends MKGolemEntity> type, Level worldIn) {
        super(type, worldIn);
        setCurrentModelLook(DEFAULT);
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return MKEntity.registerAttributes(attackDamage, movementSpeed)
                .add(MKAttributes.SHADOW_RESISTANCE, 0.25)
                .add(MKAttributes.BLEED_RESISTANCE, 0.25)
                .add(MKAttributes.RANGED_RESISTANCE, 0.25)
                .add(MKAttributes.POISON_RESISTANCE, 0.75)
                .add(MKAttributes.HOLY_RESISTANCE, 0.50)
                .add(MKAttributes.FIRE_RESISTANCE, 0.25)
                .add(MKAttributes.ARCANE_RESISTANCE, 0.25)
                .add(MKAttributes.FROST_RESISTANCE, 0.25)
                .add(MKAttributes.NATURE_RESISTANCE, 0.25)
                .add(Attributes.ARMOR, 10);
    }



    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }


    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getShootSound() {
        return SoundEvents.SKELETON_SHOOT;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

}
