package com.chaosbuffalo.mkweapons.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nullable;

public class BloodDripParticle extends TextureSheetParticle {

    private BloodDripParticle(ClientLevel world, double posX, double posY, double posZ) {
        super(world, posX, posY, posZ);
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    protected void expire() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }
    }

    protected void onUpdate(){
        if (this.onGround){
            this.remove();
        }
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.expire();
        if (!this.removed) {
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.onUpdate();
            if (!this.removed) {
                this.xd *= 0.98F;
                this.yd *= 0.98F;
                this.zd *= 0.98F;
            }
        }
    }

    public static class BloodDripFactory implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet spriteSet;

        public BloodDripFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BloodDripParticle drip = new BloodDripParticle(worldIn, x, y, z);
            drip.pickSprite(this.spriteSet);
            return drip;
        }
    }
}
