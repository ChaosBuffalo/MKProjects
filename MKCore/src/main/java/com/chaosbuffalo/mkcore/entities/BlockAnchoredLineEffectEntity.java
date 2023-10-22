package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.sync.*;
import com.chaosbuffalo.mkcore.sync.controllers.EntitySyncController;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;
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
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockAnchoredLineEffectEntity extends BaseEffectEntity implements ISyncControllerProvider {

    private BlockPos startPos;
    private Supplier<Block> block;
    private TargetingContext context;

    private static final EntityDataAccessor<Float> RANGE = SynchedEntityData.defineId(
            BlockAnchoredLineEffectEntity.class, EntityDataSerializers.FLOAT);
    private final EntitySyncController engine;
    private final PlayerSyncComponent targeting = new PlayerSyncComponent("targeting");

    @Nullable
    protected LivingEntity target;

    protected final SyncBool hasEntity = new SyncBool("has_entity", false);
    protected final SyncVec3 startPoint = new SyncVec3("start_point", Vec3.ZERO);
    protected final SyncVec3 endPoint = new SyncVec3("end_point", Vec3.ZERO);

    protected final SyncFloat beamSpeed = new SyncFloat("beam_speed", 2.5f);

    protected Vec3 prevEndPoint;
    protected int lastTickReceive;

    public BlockAnchoredLineEffectEntity(EntityType<? extends BlockAnchoredLineEffectEntity> entityType, Level world) {
        super(entityType, world);
        engine = new EntitySyncController(this);
        targeting.attach(engine);
        targeting.addPublic(hasEntity);
        targeting.addPublic(startPoint);
        targeting.addPublic(endPoint);
        endPoint.setCallback(this::onEndPointUpdate);
    }

    public BlockAnchoredLineEffectEntity(Level level, Vec3 pos) {
        this(CoreEntities.BLOCK_ANCHORED_LINE_EFFECT.get(), level);
        setPos(pos);
        setStartPoint(pos);
        startPos = BlockPos.containing(pos);
        setEndPoint(Vec3.atCenterOf(startPos.below()));
        prevEndPoint = endPoint.get();
    }

    protected void onEndPointUpdate(Vec3 prev) {
        prevEndPoint = prev;
        lastTickReceive = tickCount;
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
            this.setBoundingBox(this.dimensions.makeBoundingBox(getX(), getY() - getRange(), getZ()));
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void setPos(double p_20210_, double p_20211_, double p_20212_) {
        // we're going to skip setting the bounding box here as our entity is stationary and the base update logic
        // sets an entities position every 60 ticks
        this.setPosRaw(p_20210_, p_20211_, p_20212_);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(RANGE, 0.0F);
    }

    @Override
    protected void clientUpdate() {
        if (!hasEntity.get()) {
            return;
        }
        super.clientUpdate();
    }

    public void setStartPoint(Vec3 value) {
        startPoint.set(value);
    }

    public void setEndPoint(Vec3 value) {
        endPoint.set(value);
    }

    public BlockAnchoredLineEffectEntity setBeamSpeed(float beamSpeed) {
        this.beamSpeed.set(beamSpeed);
        return this;
    }

    @Override
    protected boolean serverUpdate() {
        if (!(getLevel().getBlockState(startPos).getBlock() == block.get())) {
            onDeath(DeathReason.KILLED);
            return true;
        }
        if (target == null) {
            lookForTarget();
        }
        engine.syncUpdates();
        if (target != null) {
            if (!getBoundingBox().intersects(target.getBoundingBox())) {
                target = null;
                hasEntity.set(false);
                return false;
            } else {
                Vec3 dir = target.position().subtract(endPoint.get()).normalize();
                setEndPoint(endPoint.get().add(dir.scale(beamSpeed.get() / GameConstants.FTICKS_PER_SECOND)));
                return super.serverUpdate();
            }
        }
        return false;
    }

    protected void lookForTarget() {
        getPotentialTargets().stream().min(Comparator.comparingDouble(
                ent -> ent.distanceToSqr(startPoint.get()))).ifPresent(tar -> {
            target = tar;
            hasEntity.set(true);
        });
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
        return Targeting.isValidTarget(context, getOwner(), e);
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
            anim.spawn(getCommandSenderWorld(), startPoint.get(),
                    Collections.singletonList(prevEndPoint.lerp(endPoint.get(), Math.min((tickCount - lastTickReceive) / 10.0f, 1.0f))));
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

    @Override
    public SyncController getSyncController() {
        return engine;
    }
}
