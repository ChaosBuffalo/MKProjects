package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseProjectileEntity extends Projectile implements IClientUpdatable, IEntityAdditionalSpawnData {

    @Nullable
    private BlockState inBlockState;
    protected boolean inGround;
    private int ownerNetworkId;

    public static final float ONE_DEGREE = 0.017453292F;
    public static final double MAX_INACCURACY = 0.0075;

    private int amplifier;
    private int ticksInGround;
    private int ticksInAir;
    private int deathTime;
    private int airProcTime;
    private boolean doAirProc;
    private int groundProcTime;
    private float skillLevel;
    private boolean doGroundProc;

    public BaseProjectileEntity(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.inBlockState = null;
        this.inGround = false;
        this.setDeathTime(100);
        this.setDoGroundProc(false);
        this.setGroundProcTime(20);
        this.setAirProcTime(20);
        this.setDoAirProc(false);
        this.setAmplifier(0);
        this.setSkillLevel(0.0f);
        setup();
    }

    @Override
    public void setOwner(@Nullable Entity p_37263_) {
        super.setOwner(p_37263_);
        if (p_37263_ != null) {
            ownerNetworkId = p_37263_.getId();
        }
    }

    @Nullable
    @Override
    public Entity getOwner() {
        //replacement for the old get owner on client logic
        Entity ret = super.getOwner();
        if (ret == null && ownerNetworkId != 0){
            return this.level.getEntity(ownerNetworkId);
        }
        return ret;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(ownerNetworkId);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        ownerNetworkId = additionalData.readInt();
    }

    public void setup() {

    }

    public void setSkillLevel(float skillLevel) {
        this.skillLevel = skillLevel;
    }

    public float getSkillLevel() {
        return skillLevel;
    }

    @Override
    protected void defineSynchedData() {

    }

    protected abstract TargetingContext getTargetContext();

    public int getAmplifier() {
        return this.amplifier;
    }


    public void setAmplifier(int newVal) {
        this.amplifier = newVal;
    }

    public boolean getDoGroundProc() {
        return this.doGroundProc;
    }

    public boolean getDoAirProc() {
        return this.doAirProc;
    }

    public int getTicksInAir() {
        return this.ticksInAir;
    }

    public int getTicksInGround() {
        return this.ticksInGround;
    }

    public void setTicksInAir(int newVal) {
        this.ticksInAir = newVal;
    }

    public void setTicksInGround(int newVal) {
        this.ticksInGround = newVal;
    }

    public void setDoAirProc(boolean newVal) {
        this.doAirProc = newVal;
    }

    public int getAirProcTime() {
        return this.airProcTime;
    }

    public void setAirProcTime(int newVal) {
        this.airProcTime = newVal;
    }

    public int getDeathTime() {
        return this.deathTime;
    }

    public void setDeathTime(int newVal) {
        this.deathTime = newVal;
    }

    public void setDoGroundProc(boolean newVal) {
        this.doGroundProc = newVal;
    }

    public int getGroundProcTime() {
        return this.groundProcTime;
    }

    public void setGroundProcTime(int newVal) {
        this.groundProcTime = newVal;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
        double edgeLength = this.getBoundingBox().getSize() * 10.0D;
        if (Double.isNaN(edgeLength)) {
            edgeLength = 1.0D;
        }

        edgeLength = edgeLength * 64.0D * getViewScale();
        return distance < edgeLength * edgeLength;
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void shoot(Entity source, float rotationPitchIn, float rotationYawIn,
                      float pitchOffset, float velocity, float inaccuracy) {
        float x = -Mth.sin(rotationYawIn * ONE_DEGREE) * Mth.cos(rotationPitchIn * ONE_DEGREE);
        float y = -Mth.sin((rotationPitchIn + pitchOffset) * ONE_DEGREE);
        float z = Mth.cos(rotationYawIn * ONE_DEGREE) * Mth.cos(rotationPitchIn * ONE_DEGREE);
        this.shoot(x, y, z, velocity, inaccuracy);
        this.setDeltaMovement(this.getDeltaMovement().add(source.getDeltaMovement().x, source.isOnGround() ? 0.0D : source.getDeltaMovement().y,
                source.getDeltaMovement().z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        double mag = Math.sqrt(x * x + y * y + z * z);
        double nX = x / mag;
        double nY = y / mag;
        double nZ = z / mag;
        nX = nX + this.random.nextGaussian() * MAX_INACCURACY * (double) inaccuracy;
        nY = nY + this.random.nextGaussian() * MAX_INACCURACY * (double) inaccuracy;
        nZ = nZ + this.random.nextGaussian() * MAX_INACCURACY * (double) inaccuracy;
        x = nX * (double) velocity;
        y = nY * (double) velocity;
        z = nZ * (double) velocity;
        this.setDeltaMovement(x, y, z);
        calculateOriginalPitchYaw(getDeltaMovement());
        this.ticksInGround = 0;
    }

    public boolean isInGround() {
        return inGround;
    }

    protected boolean checkIfInGround(BlockPos blockpos, BlockState blockstate) {
        if (!blockstate.isAir()) {
            VoxelShape voxelshape = blockstate.getBlockSupportShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 entityPos = this.position();
                for (AABB axisalignedbb : voxelshape.toAabbs()) {
                    if (axisalignedbb.move(blockpos).contains(entityPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static double horizontalMag(Vec3 vec) {
        return vec.x * vec.x + vec.z * vec.z;
    }

    protected boolean missingPrevPitchAndYaw() {
        return this.xRotO == 0.0F && this.yRotO == 0.0F;
    }

    protected void calculateOriginalPitchYaw(Vec3 motion) {

        double xyMag = Math.sqrt(horizontalMag(motion));
        this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(motion.y, xyMag) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    protected boolean onGroundProc(Entity caster, int amplifier) {
        return false;
    }

    protected boolean onImpact(Entity caster, HitResult result, int amplifier) {
        return false;
    }


    protected boolean onAirProc(Entity caster, int amplifier) {
        return false;
    }


    protected boolean isValidEntityTargetGeneric(Entity entity) {
        return entity != this && EntitySelector.NO_SPECTATORS.test(entity) && EntitySelector.ENTITY_STILL_ALIVE.test(entity);
    }


    protected boolean isValidEntityTarget(Entity entity) {
        Entity shooter = getOwner();
        if (entity instanceof LivingEntity && shooter != null) {
            return Targeting.isValidTarget(getTargetContext(), shooter, entity);
        }
        return isValidEntityTargetGeneric(entity);
    }

    // Real name canHitEntity
    @Override
    protected boolean canHitEntity(Entity entity) {
        // super will check if it has left the shooter
        return super.canHitEntity(entity) && isValidEntityTarget(entity);
    }

    private EntityHitResult rayTraceEntities(Vec3 traceStart, Vec3 traceEnd) {
        return ProjectileUtil.getEntityHitResult(level, this, traceStart, traceEnd,
                getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        super.onHitEntity(p_37259_);
    }

    protected boolean onMKHit(HitResult rayTraceResult) {
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockraytraceresult = (BlockHitResult) rayTraceResult;
            BlockState blockstate = this.level.getBlockState(blockraytraceresult.getBlockPos());
            this.inBlockState = blockstate;
            Vec3 vec3d = blockraytraceresult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(vec3d);
            Vec3 vec3d1 = vec3d.normalize().scale(0.05F);
            this.setPosRaw(this.getX() - vec3d1.x, this.getY() - vec3d1.y,
                    this.getZ() - vec3d1.z);
            this.inGround = true;
            blockstate.onProjectileHit(this.level, blockstate, blockraytraceresult, this);
        }
        return this.onImpact(getOwner(), rayTraceResult, getAmplifier());

    }

    public float getGravityVelocity() {
        return 0.03F;
    }

//    @Override
//    protected void writeAdditional(CompoundNBT compound) {
//        compound.putBoolean("doAirProc", this.getDoAirProc());
//        compound.putBoolean("doGroundProc", this.getDoGroundProc());
//        compound.putInt("airProcTime", this.getAirProcTime());
//        compound.putInt("groundProcTime", this.getGroundProcTime());
//        compound.putInt("deathTime", this.getDeathTime());
//        compound.putInt("amplifier", this.getAmplifier());
//        compound.putInt("ticksInGround", this.ticksInGround);
//        compound.putInt("ticksInAir", this.ticksInAir);
//        if (this.inBlockState != null) {
//            compound.put("inBlockState", NBTUtil.writeBlockState(this.inBlockState));
//        }
//
//        compound.putBoolean("inGround", this.inGround);
//        if (this.shootingEntity != null) {
//            compound.putUniqueId("OwnerUUID", this.shootingEntity);
//        }
//    }
//
//    @Override
//    protected void readAdditional(CompoundNBT compound) {
//        this.ticksInGround = compound.getInt("ticksInGround");
//        this.ticksInAir = compound.getInt("ticksInAir");
//        if (compound.contains("inBlockState", 10)) {
//            this.inBlockState = NBTUtil.readBlockState(compound.getCompound("inBlockState"));
//        }
//
//        this.inGround = compound.getBoolean("inGround");
//
//
//        if (compound.hasUniqueId("OwnerUUID")) {
//            this.shootingEntity = compound.getUniqueId("OwnerUUID");
//        }
//
//        this.setDoAirProc(compound.getBoolean("doAirProc"));
//        this.setDoGroundProc(compound.getBoolean("doGroundProc"));
//        this.setAirProcTime(compound.getInt("airProcTime"));
//        this.setGroundProcTime(compound.getInt("groundProcTime"));
//        this.setDeathTime(compound.getInt("deathTime"));
//        this.setAmplifier(compound.getInt("amplifier"));
//    }

    @Override
    public boolean save(CompoundTag compound) {
        return false;
    }

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        super.tick();
        if (!isAlive()) {
            return;
        }

        if (this.tickCount == this.getDeathTime()) {
            this.remove(RemovalReason.KILLED);
        }

        Vec3 motion = this.getDeltaMovement();

        if (missingPrevPitchAndYaw()) {
            calculateOriginalPitchYaw(motion);
        }

        BlockPos blockpos = blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        this.inGround = checkIfInGround(blockpos, blockstate);

//        if (world.isRemote && ticksExisted % graphicalEffectTickInterval == 0) {
//            clientGraphicalUpdate();
//        }

        if (this.inGround) {
            if (this.inBlockState != blockstate && this.level.noCollision(this.getBoundingBox().inflate(0.06D))) {
                this.inGround = false;
                this.setDeltaMovement(motion.multiply(this.random.nextFloat() * 0.2F,
                        this.random.nextFloat() * 0.2F,
                        this.random.nextFloat() * 0.2F));
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            } else {
                ++this.ticksInGround;
                if (this.getDoGroundProc() && this.ticksInGround > 0 &&
                        this.ticksInGround % this.getGroundProcTime() == 0) {
                    if (this.onGroundProc(this.getOwner(), this.getAmplifier())) {
                        this.remove(RemovalReason.KILLED);
                    }
                }
            }
        } else {
            this.ticksInGround = 0;
            ++this.ticksInAir;
            if (this.getDoAirProc() && this.ticksInAir % this.getAirProcTime() == 0) {
                if (this.onAirProc(this.getOwner(), this.getAmplifier())) {
                    this.remove(RemovalReason.KILLED);
                }
            }
            HitResult trace;
            Vec3 traceStart = this.position();
            Vec3 traceEnd = traceStart.add(motion);
            HitResult blockRayTrace = this.level.clip(new ClipContext(traceStart, traceEnd,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            trace = blockRayTrace;
            if (blockRayTrace.getType() != HitResult.Type.MISS) {
                traceEnd = blockRayTrace.getLocation();
            }

            EntityHitResult entityRayTrace = rayTraceEntities(traceStart, traceEnd);
            if (entityRayTrace != null) {
                trace = entityRayTrace;
            }

            if (trace.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, trace)) {
                if (this.onMKHit(trace)) {
                    this.remove(RemovalReason.KILLED);
                }
                this.hasImpulse = true;
            }
            motion = getDeltaMovement();
            double xyMag = Math.sqrt(horizontalMag(motion));
            setYRot((float) (Mth.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI)));
            setXRot((float) (Mth.atan2(motion.y, xyMag) * (double) (180F / (float) Math.PI)));

            while (this.getXRot() - this.xRotO < -180.0F) {
                this.xRotO -= 360.0F;
            }

            while (this.getYRot() - this.xRotO >= 180.0F) {
                this.xRotO += 360.0F;
            }

            while (this.getYRot() - this.yRotO < -180.0F) {
                this.yRotO -= 360.0F;
            }

            while (this.getYRot() - this.yRotO >= 180.0F) {
                this.yRotO += 360.0F;
            }

            setXRot(this.xRotO + (this.getXRot() - this.xRotO) * 0.2F);
            setYRot(this.yRotO + (this.getYRot() - this.yRotO) * 0.2F);
            if (!this.inGround) {
                setDeltaMovement(motion.subtract(new Vec3(0.0, getGravityVelocity(), 0.0)));
            }
            this.setPos(getX() + motion.x(), getY() + motion.y(), getZ() + motion.z());
        }
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpMotion(double x, double y, double z) {
        super.lerpMotion(x, y, z);
        if (missingPrevPitchAndYaw()) {
            calculateOriginalPitchYaw(getDeltaMovement());
            this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            this.ticksInGround = 0;
        }
    }
}
