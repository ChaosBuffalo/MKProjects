package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class TrailProjectileEntity extends BaseProjectileEntity {
    protected ResourceLocation trailAnimation;

    public TrailProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public void clientGraphicalUpdate(float partialTicks) {
        ParticleAnimation trailAnimation = getTrailAnimation();
        if (trailAnimation != null) {
            double x = Mth.lerp(partialTicks, this.xo, this.getX());
            double y = Mth.lerp(partialTicks, this.yo, this.getY());
            double z = Mth.lerp(partialTicks, this.zo, this.getZ());
            double scale = Math.min((float) (tickCount) / preFireTicks, 1.0);
            trailAnimation.spawn(getLevel(), new Vec3(x, y, z), new Vec3(scale, scale, scale), null);
        }
    }

    public void setTrailAnimation(ResourceLocation trailAnimation) {
        this.trailAnimation = trailAnimation;
    }

    @Nullable
    public ParticleAnimation getTrailAnimation() {
        return ParticleAnimationManager.getAnimation(trailAnimation);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        boolean hasTrail = additionalData.readBoolean();
        if (hasTrail) {
            setTrailAnimation(additionalData.readResourceLocation());
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        if (getTrailAnimation() != null) {
            buffer.writeBoolean(true);
            buffer.writeResourceLocation(trailAnimation);
        } else {
            buffer.writeBoolean(false);
        }
    }
}
