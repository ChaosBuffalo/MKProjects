package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.Items;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

public class MKZombifiedPiglinEntity extends MKAbstractPiglinEntity{
    private static final EntityDataAccessor<Boolean> CHARGING_CROSSBOW = SynchedEntityData.defineId(MKZombifiedPiglinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DANCING = SynchedEntityData.defineId(MKZombifiedPiglinEntity.class, EntityDataSerializers.BOOLEAN);

    public MKZombifiedPiglinEntity(EntityType<? extends MKZombifiedPiglinEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public PiglinArmPose getPiglinAction() {
        if (isDancing()) {
            return PiglinArmPose.DANCING;
        } else if (isAggressive() && isHoldingMeleeWeapon()) {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        } else if (isChargingCrossbow()) {
            return PiglinArmPose.CROSSBOW_CHARGE;
        } else {
            return isAggressive() && isHolding(Items.CROSSBOW) ? PiglinArmPose.CROSSBOW_HOLD : PiglinArmPose.DEFAULT;
        }
    }

    public void setDancing(boolean isDancing) {
        this.entityData.set(DANCING, isDancing);
    }

    public boolean isDancing(){
        return this.entityData.get(DANCING);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGING_CROSSBOW, false);
        this.entityData.define(DANCING, false);
    }

    public void setChargingCrossbow(boolean isCharging){
        this.entityData.set(CHARGING_CROSSBOW, isCharging);
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(CHARGING_CROSSBOW);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasThreatTarget() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return MKEntity.registerAttributes(attackDamage, movementSpeed)
                .add(MKAttributes.SHADOW_RESISTANCE, 0.25)
                .add(MKAttributes.FIRE_RESISTANCE, 0.25)
                .add(MKAttributes.HOLY_RESISTANCE, -0.25);
    }
}
