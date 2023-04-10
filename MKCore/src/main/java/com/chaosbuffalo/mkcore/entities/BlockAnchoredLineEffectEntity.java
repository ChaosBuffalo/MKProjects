package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.player.SyncComponent;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.sync.EntityUpdateEngine;
import com.chaosbuffalo.mkcore.sync.SyncEntity;
import com.chaosbuffalo.mkcore.sync.SyncVec3;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockAnchoredLineEffectEntity extends BaseEffectEntity{

    private BlockPos startPos;
    private Supplier<Block> block;
    private TargetingContext context;

    private static final EntityDataAccessor<Float> RANGE = SynchedEntityData.defineId(
            BlockAnchoredLineEffectEntity.class, EntityDataSerializers.FLOAT);
    private float beamSpeed;
    private final EntityUpdateEngine engine;
    private final SyncComponent targeting = new SyncComponent("targeting");
    protected final SyncEntity<LivingEntity> target = new SyncEntity<>("target", null, LivingEntity.class);
    protected final SyncVec3 startPoint = new SyncVec3("start_point", Vec3.ZERO);
    protected final SyncVec3 endPoint = new SyncVec3("end_point", Vec3.ZERO);
    public BlockAnchoredLineEffectEntity(EntityType<? extends BlockAnchoredLineEffectEntity> entityType, Level world) {
        super(entityType, world);
        engine = new EntityUpdateEngine(this);
        targeting.attach(engine);
        targeting.addPublic(target);
        targeting.addPublic(startPoint);
        targeting.addPublic(endPoint);
    }

    public BlockAnchoredLineEffectEntity(Level level, Vec3 pos) {
        this(CoreEntities.BLOCK_ANCHORED_LINE_EFFECT.get(), level);
        setPos(pos);
        setStartPoint(pos);
        startPos = BlockPos.containing(pos);
        setEndPoint(Vec3.atCenterOf(startPos.below()));
        beamSpeed = 2.5f;
    }

    public void setTargetContext(TargetingContext context) {
        this.context = context;
    }

    public void setBlock(Supplier<Block> block) {
        this.block = block;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (RANGE.equals(key)) {
            this.refreshDimensions();
            this.setBoundingBox(this.dimensions.makeBoundingBox(getX(), getY(), getZ()));
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(RANGE, 10.0F);
    }

    @Override
    protected void clientUpdate() {
        if (!target.isValid()) {
            return;
        }
        super.clientUpdate();
    }

    public void setStartPoint(Vec3 value){
        startPoint.set(value);
    }

    public void setEndPoint(Vec3 value) {
        endPoint.set(value);
    }

    public BlockAnchoredLineEffectEntity setBeamSpeed(float beamSpeed) {
        this.beamSpeed = beamSpeed;
        return this;
    }

    @Override
    protected boolean serverUpdate() {
        if (!(getLevel().getBlockState(startPos).getBlock() == block.get())){
            return true;
        }
        if (!target.isValid()) {
            lookForTarget();
        }
        return target.target().map(tar -> {
            if (tar.distanceToSqr(startPoint.get()) > getRange() * getRange()) {
                target.set(null);
                return false;
            } else {
                Vec3 dir = endPoint.get().subtract(tar.position()).normalize();
                setEndPoint(endPoint.get().add(dir.scale(beamSpeed / GameConstants.FTICKS_PER_SECOND)));
                return super.serverUpdate();
            }
        }).orElse(false);
    }

    protected void lookForTarget() {
        getPotentialTargets().stream().min(Comparator.comparingDouble(
                ent -> ent.distanceToSqr(startPoint.get()))).ifPresent(target::set);
    }

    @Override
    protected Collection<LivingEntity> getEntitiesInBounds() {
        return RayTraceUtils.rayTraceAllEntities(LivingEntity.class, getCommandSenderWorld(),
                        startPoint.get(), endPoint.get(), Vec3.ZERO,
                        1.5f, 0.0f, this::entityCheck).getEntities().stream().map(x -> x.entity)
                .collect(Collectors.toList());
    }


    protected Collection<LivingEntity> getPotentialTargets() {
        return level.getEntitiesOfClass(LivingEntity.class, getBoundingBox(), this::potentialTargetCheck);
    }


    protected boolean potentialTargetCheck(LivingEntity e) {
        return super.entityCheck(e) && Targeting.isValidTarget(context, getOwner(), e);
    }

    @Nonnull
    @Override
    public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
        return EntityDimensions.scalable(getRange() * 2.0F, getRange() * 2.0F);
    }

    @Override
    protected void spawnClientParticles(ParticleDisplay display) {
        ParticleAnimation anim = ParticleAnimationManager.getAnimation(display.getParticles());
        if (anim != null) {
            anim.spawn(getCommandSenderWorld(), startPoint.get(), Collections.singletonList(endPoint.get()));
        }
    }

    public TargetingContext getTargetContext() {
        return context;
    }

    public BlockAnchoredLineEffectEntity setRange(float newValue) {
        if (!level.isClientSide) {
            getEntityData().set(RANGE, newValue);
        }
        return this;
    }

    public float getRange() {
        return getEntityData().get(RANGE);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBlockPos(startPos);
        ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(block.get());
        buffer.writeBoolean(blockKey != null);
        if (blockKey != null) {
            buffer.writeResourceLocation(blockKey);
        }

    }

    public BlockPos getStartPos() {
        return startPos;
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        startPos = additionalData.readBlockPos();
        boolean hasKey = additionalData.readBoolean();
        if (hasKey) {
            ResourceLocation blockKey = additionalData.readResourceLocation();
            block = Lazy.of(() -> ForgeRegistries.BLOCKS.getValue(blockKey));
        }
    }
}
