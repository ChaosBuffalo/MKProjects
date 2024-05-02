package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public abstract class ParticleEffectState extends MKEffectState {
    private static final Vec3 YP = new Vec3(0.0, 1.0, 0.0);
    @Nullable
    protected ResourceLocation particles = null;

    public void setEffectParticles(ResourceLocation particle) {
        this.particles = particle;
    }
    @Nullable
    public ResourceLocation getParticles() {
        return particles;
    }

    protected void sendEffectParticles(Entity target) {
        if (particles != null) {
            MKParticles.spawn(target, YP, particles);
        }
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        if (particles != null) {
            MKNBTUtil.writeResourceLocation(stateTag, "particles", particles);
        }
    }

    @Override
    public void deserializeStorage(CompoundTag stateTag) {
        super.deserializeStorage(stateTag);
        if (stateTag.contains("particles")) {
            particles = MKNBTUtil.readResourceLocation(stateTag, "particles");
        }
    }
}
