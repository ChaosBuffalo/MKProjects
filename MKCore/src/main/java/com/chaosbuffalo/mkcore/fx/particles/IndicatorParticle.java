package com.chaosbuffalo.mkcore.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class IndicatorParticle extends TextureSheetParticle {
    private Camera renderInfo;

    private IndicatorParticle(ClientLevel world, double posX, double posY, double posZ) {
        super(world, posX, posY, posZ);
        this.setSize(0.05f, 0.05f);
        this.gravity = 0.00F;
        this.lifetime = 1;
        this.renderInfo = null;
        this.quadSize = 1.0f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderTypes.ALWAYS_VISIBLE_RENDERER;
    }

    protected void expire() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        this.renderInfo = renderInfo;
        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.pack(15, 15);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        if (renderInfo != null) {
            double dist = renderInfo.getPosition().distanceTo(new Vec3(x, y, z));
            if (dist > 75) {
                return (float) (dist / 75.0f) * quadSize;
            }
        }
        return quadSize;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.expire();
    }

    public static class IndicatorFactory implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet spriteSet;

        public IndicatorFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            IndicatorParticle particle = new IndicatorParticle(worldIn, x, y, z);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}