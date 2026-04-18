package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

public class MKFireElementalEntity extends MKEntity {
    public static final String DEFAULT = "default";

    public MKFireElementalEntity(EntityType<? extends MKFireElementalEntity> type, Level worldIn) {
        super(type, worldIn);
        setCurrentModelLook(MKEntity.makeLookId(type, "charged"));
        setPathfindingMalus(PathType.WATER, -1.0F);
        setPathfindingMalus(PathType.LAVA, 8.0F);
        setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
        setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return MKEntity.registerAttributes(attackDamage, movementSpeed)
                .add(MKAttributes.FIRE_RESISTANCE, 0.75)
                .add(MKAttributes.FROST_RESISTANCE, -0.5)
                .add(MKAttributes.NATURE_RESISTANCE, -0.25);
    }

    @Override
    public void aiStep() {
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.85D, 1.0D));
        }

        if (this.level().isClientSide) {
            RandomSource random = getRandom();
            if (random.nextInt(28) == 0 && !this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY() + 0.45D, this.getZ(),
                        SoundEvents.BLAZE_BURN, this.getSoundSource(), 0.8F,
                        0.8F + random.nextFloat() * 0.4F, false);
            }

            for (int i = 0; i < 3; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.15D + random.nextDouble() * 0.45D;
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + Math.cos(angle) * radius,
                        this.getY(random.nextDouble() * 0.15D),
                        this.getZ() + Math.sin(angle) * radius,
                        Math.cos(angle) * 0.015D, 0.025D + random.nextDouble() * 0.02D,
                        Math.sin(angle) * 0.015D);
            }
            double smokeAngle = random.nextDouble() * Math.PI * 2.0D;
            double smokeRadius = 0.05D + random.nextDouble() * 0.3D;
            this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + Math.cos(smokeAngle) * smokeRadius,
                    this.getY(random.nextDouble() * 0.2D),
                    this.getZ() + Math.sin(smokeAngle) * smokeRadius,
                    Math.cos(smokeAngle) * 0.01D, 0.015D, Math.sin(smokeAngle) * 0.01D);
        }

        super.aiStep();
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected void handleCombatMovementDetect(ItemStack stack) {
        setCombatMoveType(CombatMoveType.MELEE);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.BLAZE_BURN;
    }
}
