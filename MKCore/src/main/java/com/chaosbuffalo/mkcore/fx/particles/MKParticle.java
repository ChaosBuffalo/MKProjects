package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class MKParticle extends TextureSheetParticle {
    private final boolean expireOnGround;
    private final Vec3 origin;
    protected ParticleKeyFrame currentFrame;
    private Consumer<MKParticle> onExpire;
    private final ParticleAnimation particleAnimation;
    private final Map<ParticleDataKey, Float> floatData;
    private final Map<ParticleDataKey, Vec3> vector3dData;
    private final Map<ParticleDataKey, Vector3f> vector3fData;
    private final ParticleRenderType renderType;
    private float mkMinU;
    private float mkMinV;
    private float mkMaxU;
    private float mkMaxV;
    private int ticksSinceRender;
    @Nullable
    private final Entity source;
    private static final Vec3 EMPTY_VECTOR_3D = new Vec3(0.0, 0.0, 0.0);
    private static final Vector3f EMPTY_VECTOR_3F = new Vector3f(0.0f, 0.0f, 0.0f);

    public static class ParticleDataKey {
        private final ParticleAnimationTrack animation;
        private final int index;

        public ParticleDataKey(ParticleAnimationTrack animation, int i) {
            this.animation = animation;
            this.index = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParticleDataKey that = (ParticleDataKey) o;
            return index == that.index && animation.equals(that.animation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(animation, index);
        }
    }


    private MKParticle(ClientLevel world, double posX, double posY, double posZ,
                       float gravity,
                       float particleWidth, float particleHeight,
                       int maxAge, boolean expireOnGround, ParticleAnimation animation,
                       Vec3 origin, Entity source, ParticleRenderType renderType) {
        super(world, posX, posY, posZ);
        this.origin = origin;
        this.setSize(particleWidth, particleHeight);
        this.gravity = gravity;
        this.lifetime = maxAge;
        this.expireOnGround = expireOnGround;
        this.onExpire = null;
        this.age = 0;
        this.source = source;
        this.ticksSinceRender = 0;
        this.hasPhysics = false;
        this.currentFrame = new ParticleKeyFrame();
        this.particleAnimation = animation;
        this.floatData = new HashMap<>();
        this.vector3dData = new HashMap<>();
        this.vector3fData = new HashMap<>();
        this.lifetime = animation.getTickLength();
        this.renderType = renderType;
        animation.tick(this);
        animation.tickAnimation(this, 0.0f);
    }

    public void fixUV() {
        // there is not enough padding in between particles in the particle texture atlas, if we blur them
        // sometimes you'll get pixels from an adjacent particle, lets reduce our uvs by 10% to avoid this
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();
        float diffU = (maxU - minU) * .1f;
        float diffV = (maxV - minV) * .1f;
        mkMinU = minU + diffU;
        mkMaxU = maxU - diffU;
        mkMinV = minV + diffV;
        mkMaxV = maxV - diffV;

    }

    public boolean hasSource() {
        return source != null;
    }


    public Optional<Entity> getSource() {
        return source != null ? Optional.of(source) : Optional.empty();
    }

    @Override
    protected float getU1() {
        return mkMaxU;
    }

    @Override
    protected float getV1() {
        return mkMaxV;
    }

    @Override
    protected float getU0() {
        return mkMinU;
    }

    @Override
    protected float getV0() {
        return mkMinV;
    }

    public Random getRand() {
        return random;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        particleAnimation.tickAnimation(this, partialTicks);
//        Vector3d particlePos = new Vector3d(posX, posY, posZ);
//        if (renderInfo.pos.squareDistanceTo(particlePos) < 1.0){
//            return;
//        }
        ticksSinceRender = 0;
        super.render(buffer, renderInfo, partialTicks);
    }

    public Vec3 getOrigin() {
        return getSource().map(ent -> origin.add(ent.position())).orElse(origin);
    }

    public Vec3 getMotion() {
        return new Vec3(xd, yd, zd);
    }

    public Vec3 getPosition() {
        return new Vec3(x, y, z);
    }

    public Vec3 getInterpolatedPosition(float partialTicks) {
        return new Vec3(MathUtils.lerpDouble(xo, x, partialTicks),
                MathUtils.lerpDouble(yo, y, partialTicks),
                MathUtils.lerpDouble(zo, z, partialTicks));
    }

    public void setTrackFloatData(ParticleDataKey key, float value) {
        floatData.put(key, value);
    }

    public float getTrackFloatData(ParticleDataKey key) {
        return floatData.getOrDefault(key, 0.0f);
    }

    public void setTrackVector3dData(ParticleDataKey key, Vec3 vec) {
        vector3dData.put(key, vec);
    }

    public Vec3 getTrackVector3dData(ParticleDataKey key) {
        return vector3dData.getOrDefault(key, EMPTY_VECTOR_3D);
    }

    public void setTrackVector3fData(ParticleDataKey key, Vector3f vec) {
        vector3fData.put(key, vec);
    }

    public Vector3f getTrackVector3fData(ParticleDataKey key) {
        return vector3fData.getOrDefault(key, EMPTY_VECTOR_3F);
    }

    public ParticleKeyFrame getCurrentFrame() {
        return currentFrame;
    }

    public void setScale(float scale) {
        this.quadSize = scale;
    }

    public int getAge() {
        return age;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.pack(15, 15);
    }

    public void setOnExpire(Consumer<MKParticle> onExpire) {
        this.onExpire = onExpire;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return renderType;
    }

    protected void expire() {
        if (this.age++ >= lifetime) {
            if (onExpire != null) {
                onExpire.accept(this);
            }
            this.remove();
        }
    }

    protected void onUpdate() {
        if (this.onGround && expireOnGround) {
            this.remove();
        }
    }

    public void setMotion(double x, double y, double z) {
        this.xd = x;
        this.yd = y;
        this.zd = z;
    }

    public double getMotionX() {
        return xd;
    }

    public double getMotionY() {
        return yd;
    }

    public double getMotionZ() {
        return zd;
    }

    public float getParticleGravity() {
        return this.gravity;
    }


    public void tick() {
        particleAnimation.tick(this);
        ticksSinceRender++;
        if (ticksSinceRender > 1) {
            particleAnimation.tickAnimation(this, 0.0f);
        }
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.expire();
        if (!this.removed) {
//            this.motionY -= this.particleGravity;
            this.move(this.xd, this.yd, this.zd);
            this.onUpdate();
        }
    }

    public static class MKParticleFactory implements ParticleProvider<MKParticleData> {
        protected final SpriteSet spriteSet;
        private final float gravity;
        private final float particleWidth;
        private final float particleHeight;
        private final int maxAge;
        private final boolean expireOnGround;
        private final ParticleRenderType renderType;
        private final Consumer<MKParticle> onExpire;

        public MKParticleFactory(SpriteSet spriteSet,
                                 float gravity, float particleWidth, float particleHeight, int maxAge,
                                 boolean expireOnGround, ParticleRenderType renderType, Consumer<MKParticle> onExpire) {
            this.spriteSet = spriteSet;
            this.maxAge = maxAge;
            this.gravity = gravity;
            this.particleHeight = particleHeight;
            this.particleWidth = particleWidth;
            this.expireOnGround = expireOnGround;
            this.onExpire = onExpire;
            this.renderType = renderType;
        }

        public MKParticleFactory(SpriteSet spriteSet,
                                 float gravity, float particleWidth, float particleHeight, int maxAge,
                                 boolean expireOnGround, Consumer<MKParticle> onExpire) {
            this(spriteSet, gravity, particleWidth, particleHeight, maxAge, expireOnGround,
                    ParticleRenderTypes.MAGIC_RENDERER, onExpire);
        }


        @Nullable
        @Override
        public Particle createParticle(MKParticleData typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            MKParticle particle = new MKParticle(worldIn, x, y, z,
                    gravity, particleWidth, particleHeight, maxAge, expireOnGround, typeIn.animation, typeIn.origin,
                    typeIn.hasSource() ? worldIn.getEntity(typeIn.getEntityId()) : null, renderType);
            particle.setMotion(xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.spriteSet);
            particle.fixUV();
            particle.setOnExpire(onExpire);
            return particle;
        }
    }
}
