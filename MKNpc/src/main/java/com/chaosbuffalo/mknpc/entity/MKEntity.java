package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkchat.capabilities.INpcDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueUtils;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.combat.IVisualMeleeAttackEntity;
import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mkcore.core.combat.VisualMeleeAttackSequence;
import com.chaosbuffalo.mkcore.core.pets.IMKPet;
import com.chaosbuffalo.mkcore.core.pets.PetNonCombatBehavior;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.entities.ISyncControllerProvider;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import com.chaosbuffalo.mkcore.sync.controllers.EntitySyncController;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import com.chaosbuffalo.mkfaction.capabilities.IMobFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.entity.ai.controller.MovementStrategyController;
import com.chaosbuffalo.mknpc.entity.ai.goal.*;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.entity.ai.memory.ThreatMapEntry;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.FollowMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.FlyingFollowMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.KiteMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.MovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.StationaryMovementStrategy;
import com.chaosbuffalo.mknpc.entity.ai.sensor.MKSensorTypes;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.init.MKNpcAttributes;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.utils.NpcConstants;
import com.chaosbuffalo.targeting_api.ITargetingOwner;
import com.chaosbuffalo.targeting_api.Targeting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableDouble;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class MKEntity extends PathfinderMob implements IModelLookProvider, RangedAttackMob, ISyncControllerProvider, IMKPet, ITargetingOwner, IVisualMeleeAttackEntity {
    private static final EntityDataAccessor<String> LOOK_STYLE = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_GHOST = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> GHOST_TRANSLUCENCY = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_GHOST_ARMOR = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> GHOST_ARMOR_TRANSLUCENCY = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private final SyncGroup animSync = new SyncGroup();
    private int castAnimTimer;
    private VisualCastState visualCastState;
    private MKAbility castingAbility;
    private double lungeSpeed;
    private NonCombatMoveType nonCombatMoveType;
    private CombatMoveType combatMoveType;
    private MKMeleeAttackGoal meleeAttackGoal;
    private int comboCountDefault;
    private int comboCooldownDefault;
    private int comboCount;
    private int comboCooldown;
    private final EntitySyncController syncController;
    private final MKEntityData entityDataCap;
    private final ParticleEffectInstanceTracker particleEffectTracker;
    private final EntityTradeContainer entityTradeContainer;
    private final List<BossStage> bossStages = new ArrayList<>();
    private int currentStage;
    private boolean canFly;

    private int blockDelay;
    private int blockHold;
    private int blockCooldown;

    private int castTicks;
    private int currentCastTicks;
    private double rangedCastingDistance;
    private boolean wasSwingingLastTick;
    private final VisualMeleeHandState mainHandVisualMeleeState = new VisualMeleeHandState();
    private final VisualMeleeHandState offHandVisualMeleeState = new VisualMeleeHandState();

    @Nullable
    protected Component battlecry;
    @Nullable
    private PetNonCombatBehavior nonCombatBehavior;

    protected static final int BATTLECRY_COOLDOWN = GameConstants.TICKS_PER_SECOND * 60;


    public enum CombatMoveType {
        MELEE,
        RANGE,
        STATIONARY
    }

    public enum NonCombatMoveType implements StringRepresentable {
        STATIONARY,
        RANDOM_WANDER;

        public static final Codec<NonCombatMoveType> CODEC = StringRepresentable.fromEnum(NonCombatMoveType::values);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public enum VisualCastState {
        NONE,
        CASTING,
        RELEASE,
    }

    private static final class VisualMeleeHandState {
        private final VisualMeleeAttackSequence attackSequence = new VisualMeleeAttackSequence();
        private int localSwingVariant;
        private int visualAttackBaseVariant;
        private int windupVariant;
        private int nextWindupVariant;
        private int windupTicks;
        private int windupRecoveryTicks;
    }

    public void setGhost(boolean ghost) {
        getEntityData().set(IS_GHOST, ghost);
    }

    public boolean isGhost() {
        return getEntityData().get(IS_GHOST);
    }

    public boolean hasGhostArmor() {
        return getEntityData().get(HAS_GHOST_ARMOR);
    }

    public void setGhostArmor(boolean ghost) {
        getEntityData().set(HAS_GHOST_ARMOR, ghost);
    }

    public void setGhostArmorTranslucency(float translucency) {
        getEntityData().set(GHOST_ARMOR_TRANSLUCENCY, translucency);
    }

    public void setGhostTranslucency(float ghostTranslucency) {
        getEntityData().set(GHOST_TRANSLUCENCY, ghostTranslucency);
    }

    public float getGhostArmorTranslucency() {
        return getEntityData().get(GHOST_ARMOR_TRANSLUCENCY);
    }

    @Nullable
    @Override
    public Entity getTargetingOwner() {
        return getEntityDataCap().getPets().getOwner();
    }

    public float getGhostTranslucency() {
        return getEntityData().get(GHOST_TRANSLUCENCY);
    }

    protected MKEntity(EntityType<? extends PathfinderMob> type, Level worldIn) {
        super(type, worldIn);
        if (!worldIn.isClientSide()) {
            setAttackComboStatsAndDefault(1, GameConstants.TICKS_PER_SECOND);
            setupDifficulty(worldIn.getDifficulty());
        }
        entityTradeContainer = new EntityTradeContainer(this);
        castAnimTimer = 0;
        currentStage = 0;
        castTicks = 0;
        currentCastTicks = 0;
        visualCastState = VisualCastState.NONE;
        castingAbility = null;
        wasSwingingLastTick = false;
        battlecry = null;
        lungeSpeed = .25;
        rangedCastingDistance = 6.0;
        blockCooldown = GameConstants.TICKS_PER_SECOND * 2;
        blockDelay = GameConstants.TICKS_PER_SECOND / 2;
        blockHold = GameConstants.TICKS_PER_SECOND * 2;
        syncController = new EntitySyncController(this);
        syncController.addChild("anim", animSync);
        particleEffectTracker = ParticleEffectInstanceTracker.getTracker(this);
        animSync.addPublic("particles", particleEffectTracker);
        nonCombatMoveType = NonCombatMoveType.RANDOM_WANDER;
        combatMoveType = CombatMoveType.MELEE;
        canFly = false;

        entityDataCap = MKCore.getEntitySpecificData(this).orElseThrow(IllegalStateException::new);
        entityDataCap.attachUpdateEngine(syncController);
        entityDataCap.getAbilityExecutor().setStartCastCallback(this::startCast);
        entityDataCap.getAbilityExecutor().setCompleteAbilityCallback(this::endCast);
        entityDataCap.getAbilityExecutor().setInterruptCastCallback(this::interruptCast);
        entityDataCap.setInstanceTracker(particleEffectTracker);
        // Install the attachment manually. Note that this needs a custom attachment serializer to avoid creating dupes.
        setData(CoreAttachments.ENTITY_DATA_ATTACHMENT, entityDataCap);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!level().isClientSide) {
            syncController.onJoinLevel();
        }
    }

    public MKEntityData getEntityDataCap() {
        return entityDataCap;
    }

    public boolean hasBossStages() {
        return !bossStages.isEmpty();
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void addBossStage(BossStage stage) {
        if (!hasBossStages()) {
            stage.apply(this);
        }
        bossStages.add(stage);
    }

    public int getBlockDelay() {
        return blockDelay;
    }

    public int getBlockHold() {
        return blockHold;
    }

    public int getBlockCooldown() {
        return blockCooldown;
    }

    public void setBlockDelay(int blockDelay) {
        this.blockDelay = blockDelay;
    }

    public void setBlockHold(int blockHold) {
        this.blockHold = blockHold;
    }

    public void setBlockCooldown(int blockCooldown) {
        this.blockCooldown = blockCooldown;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public boolean canFly() {
        return canFly;
    }

    @Override
    public void tick() {
        super.tick();
        updateEntityCastState();
        if (!this.level().isClientSide()) {
            syncController.syncUpdates();
        }
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return !isGhost() && super.isInvisibleTo(player);
    }

    @Override
    public boolean isInvisible() {
        return isGhost() || super.isInvisible();
    }

    public boolean hasNextStage() {
        return bossStages.size() > getCurrentStage() + 1;
    }

    public BossStage getNextStage() {
        return bossStages.get(getCurrentStage() + 1);
    }

    protected double getCastingSpeedForDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case NORMAL:
                return 0.5;
            case HARD:
                return 0.75;
            case EASY:
            default:
                return 0.25;
        }
    }

    public int getCastTicks() {
        return castTicks;
    }

    public int getCurrentCastTicks() {
        return currentCastTicks;
    }

    public float getCastRatio(){
        if (castTicks == 0) {
            return 0.f;
        }
        return Math.min((float) (currentCastTicks) / castTicks, 1.0f);
    }

    static final ResourceLocation CAST_SPEED_MOD_ID = MKNpc.id("npc.difficulty.mod");
    protected void setupDifficulty(Difficulty difficulty) {
        AttributeInstance inst = getAttribute(MKAttributes.CASTING_SPEED);
        if (inst != null) {
            inst.addTransientModifier(new AttributeModifier(CAST_SPEED_MOD_ID,
                    getCastingSpeedForDifficulty(difficulty), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }


    public float getTranslucency() {
        // vanilla value is 0.15f
        return isGhost() ? getGhostTranslucency() : 0.15f;
    }

    public void postDefinitionApply(NpcDefinition definition) {
        float maxHealth = getMaxHealth();
        if (maxHealth > 100.0f) {
            float ratio = maxHealth / 100.0f;
            float adjustForBase = ratio - 1.0f;
            AttributeInstance inst = getAttribute(MKAttributes.HEAL_EFFICIENCY);
            if (inst != null) {
                inst.addTransientModifier(new AttributeModifier(MKNpc.id("heal_scaling"),
                        adjustForBase, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }


    public ParticleEffectInstanceTracker getParticleEffectTracker() {
        return particleEffectTracker;
    }

    @Override
    public EntitySyncController getSyncController() {
        return syncController;
    }

    public double getLungeSpeed() {
        return lungeSpeed * getAttackSpeedMultiplier();
    }

    public void setLungeSpeed(double lungeSpeed) {
        this.lungeSpeed = lungeSpeed;
    }

    public static AttributeSupplier.Builder registerAttributes(double attackDamage, double movementSpeed) {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.MOVEMENT_SPEED, movementSpeed)
                .add(MKNpcAttributes.AGGRO_RANGE, 6)
                .add(Attributes.ENTITY_INTERACTION_RANGE)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LOOK_STYLE, "");
        builder.define(SCALE, 1.0f);
        builder.define(IS_GHOST, false);
        builder.define(GHOST_TRANSLUCENCY, 1.0f);
        builder.define(HAS_GHOST_ARMOR, false);
        builder.define(GHOST_ARMOR_TRANSLUCENCY, 1.0f);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        attackEntityWithRangedAttack(target, distanceFactor, 1.6f);
    }

    public double getEntityReach() {
        return getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
    }

    public double getMeleeApproachDistanceMultiplier() {
        return 0.5;
    }

    public void attackEntityWithRangedAttack(LivingEntity target, float launchPower, float launchVelocity) {
        ItemStack weaponStack = getMainHandItem();
        ItemStack arrowStack = this.getProjectile(this.getItemInHand(InteractionHand.MAIN_HAND));
        AbstractArrow arrowEntity = ProjectileUtil.getMobArrow(this, arrowStack, launchPower, weaponStack);
        if (weaponStack.getItem() instanceof BowItem bow) {
            arrowEntity = bow.customArrow(arrowEntity, arrowStack, weaponStack);
        }
        EntityUtils.shootArrow(this, arrowEntity, target, launchPower * launchVelocity);
        this.playSound(getShootSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrowEntity);
    }

    protected SoundEvent getShootSound() {
        return SoundEvents.ARROW_SHOOT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_VILLAGER_STEP;
    }

    @Override
    public float getScale() {
        return entityData.get(SCALE);
    }

    public void setRenderScale(float newScale) {
        entityData.set(SCALE, newScale);
    }

    @Override
    protected void registerGoals() {
        int priority = 0;
        this.goalSelector.addGoal(priority++, new ReturnToSpawnGoal(this));
        this.goalSelector.addGoal(priority++, new FloatGoal(this));
        this.goalSelector.addGoal(priority++, new MovementGoal(this));
        this.goalSelector.addGoal(priority++, createUseAbilityGoal());
        this.goalSelector.addGoal(priority++, new MKBowAttackGoal(this, 5, 15.0f));
        this.goalSelector.addGoal(priority++, new MKBlockGoal(this));
        this.meleeAttackGoal = new MKMeleeAttackGoal(this);
        this.goalSelector.addGoal(priority++, meleeAttackGoal);
        this.goalSelector.addGoal(priority++, new LookAtThreatTargetGoal(this));
        this.targetSelector.addGoal(3, new MKTargetGoal(this, true, true));

    }

    protected UseAbilityGoal createUseAbilityGoal() {
        return new UseAbilityGoal(this, false);
    }

    public boolean avoidsWater() {
        return true;
    }

    protected void handleCombatMovementDetect(ItemStack stack) {
        if (ItemUtils.isRangedWeapon(stack)) {
            setCombatMoveType(CombatMoveType.RANGE);
        } else {
            setCombatMoveType(CombatMoveType.MELEE);
        }
    }

    public void setBattlecry(@Nullable Component battlecry) {
        this.battlecry = battlecry;
    }

    protected void maybeDoBattlecry(LivingEntity target) {
        if (getServer() == null || battlecry == null) {
            return;
        }

        var faction = IMobFaction.getMobOrThrow(this).getFaction();
        if (faction != null) {
            MKCore.getEntityData(target).ifPresent(entityData -> {
                ResourceLocation timerId = faction.unwrapKey()
                        .map(ResourceKey::location)
                        .map(r -> r.withPrefix("battlecry"))
                        .orElseThrow();
                if (entityData.getStats().getTimer(timerId) <= 0) {
                    DialogueUtils.sendMessageToAllAround(this,
                            DialogueUtils.formatSpeakerMessage(this, battlecry));
                    entityData.getStats().setTimer(timerId, BATTLECRY_COOLDOWN);
                }
            });
        }
    }

    public void callForHelp(LivingEntity entity, float threatVal) {
        maybeDoBattlecry(entity);
        brain.getMemory(MKMemoryModuleTypes.ALLIES.get()).ifPresent(x -> {
            x.forEach(ent -> {
                if (ent.distanceToSqr(this) < 9.0) {
                    if (ent instanceof MKEntity mkEntity) {
                        mkEntity.addThreat(entity, threatVal, true);
                    }
                }
            });
        });
    }

    @Override
    public void setItemInHand(InteractionHand hand, ItemStack stack) {
        super.setItemInHand(hand, stack);
        if (hand == InteractionHand.MAIN_HAND) {
            handleCombatMovementDetect(stack);
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (slotIn == EquipmentSlot.MAINHAND) {
            handleCombatMovementDetect(stack);
        }
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        super.killedEntity(level, victim);
        returnToDefaultMovementState();
        return true;
    }

    public MKMeleeAttackGoal getMeleeAttackGoal() {
        return meleeAttackGoal;
    }

    public void setComboDefaults(int count, int cooldown) {
        comboCountDefault = count;
        comboCooldownDefault = cooldown;
    }

    public void setAttackComboStatsAndDefault(int count, int cooldown) {
        setComboDefaults(count, cooldown);
        restoreComboDefaults();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public void restoreComboDefaults() {
        setAttackComboCount(comboCountDefault);
        setAttackComboCooldown(comboCooldownDefault);
    }

    public void setAttackComboCount(int count) {
        comboCount = count;
    }

    public int getAttackComboCount() {
        return comboCount;
    }

    public void setAttackComboCooldown(int ticks) {
        comboCooldown = ticks;
    }

    public int getAttackComboCooldown() {
        return comboCooldown;
    }

    @Override
    public void clearThreat() {
        getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_MAP.get());
        getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_TARGET.get());
        getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_LIST.get());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(SCALE)) {
            refreshDimensions();
        }
    }

    @Override
    public double getEyeY() {
        return position().y + getEyeHeight() * entityData.get(SCALE);
    }

    @Override
    public ResourceLocation getCurrentModelLook() {
        String rawId = entityData.get(LOOK_STYLE);
        ResourceLocation lookId = ResourceLocation.tryParse(rawId);
        if (lookId != null) {
            return lookId;
        }
        return makeLookId(this.getType(), "default");
    }

    @Override
    public void setCurrentModelLook(ResourceLocation lookId) {
        entityData.set(LOOK_STYLE, lookId.toString());
    }

    public static ResourceLocation makeLookId(EntityType<?> entityType, String lookName) {
        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return ResourceLocation.fromNamespaceAndPath(entityTypeId.getNamespace(),
                "%s/%s".formatted(entityTypeId.getPath(), lookName));
    }

    public MovementStrategy getMovementStrategy(AbilityTargetingDecision decision) {
        MKAbility ability = decision.getAbility();
        if (ability == null) {
            return StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY;
        }
        if (!Targeting.isValidEnemy(this, decision.getTargetEntity())) {
            if (getNonCombatMoveType() == NonCombatMoveType.STATIONARY) {
                return StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY;
            }
        }
        switch (decision.getMovementSuggestion()) {
            case KITE:
                return new KiteMovementStrategy(Math.max(ability.getDistance(this) * .50, getMinimumRangedCastingDistance()), canFly());
            case FOLLOW:
                return canFly() ?
                        new FlyingFollowMovementStrategy(1.0f, Math.round(ability.getDistance(this) / 2.0f)) :
                        new FollowMovementStrategy(1.0f, Math.round(ability.getDistance(this) / 2.0f));
            case MELEE:
                return canFly() ? new FlyingFollowMovementStrategy(1.0f, 1) : new FollowMovementStrategy(1.0f, 1);
            case STATIONARY:
            default:
                return StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY;
        }
    }

    public void returnToSpawnTick() {
        boolean isReturningToPlayer = getEntityDataCap().getPets().isPet() && getEntityDataCap().getPets().getOwner() instanceof Player;
        if (!isReturningToPlayer) {
            setHealth(Math.min(getHealth() + getMaxHealth() * .2f * 1.0f / GameConstants.TICKS_PER_SECOND,
                    getMaxHealth()));
        }
    }

    @Override
    public @org.jetbrains.annotations.Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @org.jetbrains.annotations.Nullable SpawnGroupData spawnGroupData) {
//        MKNpc.LOGGER.info("In initial spawn for {}", this);
        SpawnGroupData entityData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        IEntityNpcData.get(this).ifPresent((cap) -> {
            if (cap.wasMKSpawned()) {
                getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT.get(), cap.getSpawnPos());
            }
        });
        enterNonCombatMovementState();
        return entityData;
    }

    @Override
    public boolean shouldBeSaved() {
        return !MKNpc.getNpcData(this).map(IEntityNpcData::wasMKSpawned).orElse(false);
    }

    @Override
    public void addThreat(LivingEntity entity, float value, boolean propagate) {
        if (Targeting.isValidFriendly(this, entity)) {
            return;
        }
        Map<LivingEntity, ThreatMapEntry> newMap = brain.getMemory(MKMemoryModuleTypes.THREAT_MAP.get())
                .orElseGet(HashMap::new);
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).addThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP.get(), newMap);
        if (propagate) {
            if (getEntityDataCap().getPets().hasPet()) {
                getEntityDataCap().getPets().addThreatToPets(entity, value, false);
            }
        }
    }

    @Override
    public void setNoncombatBehavior(PetNonCombatBehavior petNonCombatBehavior) {
        nonCombatBehavior = petNonCombatBehavior;
        enterNonCombatMovementState();
    }

    protected void updateEntityCastState() {
        if (visualCastState == VisualCastState.CASTING) {
            currentCastTicks++;
        }
        if (castAnimTimer > 0) {
            castAnimTimer--;
            if (castAnimTimer == 0) {
                castingAbility = null;
                visualCastState = VisualCastState.NONE;
            }
        }
    }


    @Override
    public void aiStep() {
        updateSwingTime();
        if (level().isClientSide) {
            getVisualMeleeState(InteractionHand.MAIN_HAND).attackSequence.tick();
            getVisualMeleeState(InteractionHand.OFF_HAND).attackSequence.tick();
            boolean mainVisualSwingStarted = getVisualMeleeState(InteractionHand.MAIN_HAND).attackSequence.consumeSwingStartedThisTick();
            boolean offVisualSwingStarted = getVisualMeleeState(InteractionHand.OFF_HAND).attackSequence.consumeSwingStartedThisTick();
            if (mainVisualSwingStarted) {
                incrementLocalSwingVariant(InteractionHand.MAIN_HAND);
                resetVisualMeleeWindup(InteractionHand.MAIN_HAND);
            }
            if (offVisualSwingStarted) {
                incrementLocalSwingVariant(InteractionHand.OFF_HAND);
                resetVisualMeleeWindup(InteractionHand.OFF_HAND);
            }
            if (!(mainVisualSwingStarted || offVisualSwingStarted) && swinging) {
                if (!wasSwingingLastTick) {
                    if (swingingArm == InteractionHand.OFF_HAND) {
                        incrementLocalSwingVariant(InteractionHand.OFF_HAND);
                    } else {
                        incrementLocalSwingVariant(InteractionHand.MAIN_HAND);
                    }
                    resetSwing(swingingArm == null ? InteractionHand.MAIN_HAND : swingingArm);
                    resetVisualMeleeWindup(swingingArm == null ? InteractionHand.MAIN_HAND : swingingArm);
                }
            }
            tickVisualMeleeWindupState(InteractionHand.MAIN_HAND);
            tickVisualMeleeWindupState(InteractionHand.OFF_HAND);
            wasSwingingLastTick = swinging;
        }
        getEntityDataCap().getCombatExtension().tickAttackStrengthTicks();
        attackStrengthTicker = getEntityDataCap().getCombatExtension().getAttackStrengthTicks(InteractionHand.MAIN_HAND);
        super.aiStep();
        if (nonCombatBehavior != null && !hasThreatTarget()) {
            nonCombatBehavior.getEntity().ifPresent(x -> getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT.get(), x.blockPosition()));
        }
    }

    public void resetSwing(InteractionHand hand) {
        getEntityDataCap().getCombatExtension().setAttackStrengthTicks(hand, 0);
        if (hand == InteractionHand.MAIN_HAND) {
            attackStrengthTicker = 0;
        }
    }

    public void subtractFromTicksSinceLastSwing(InteractionHand hand, int toSubtract) {
        getEntityDataCap().getCombatExtension().increaseAttackStrengthTicks(hand, -toSubtract);
        attackStrengthTicker = getEntityDataCap().getCombatExtension().getAttackStrengthTicks(InteractionHand.MAIN_HAND);
    }

    public int getTicksSinceLastSwing(InteractionHand hand) {
        return getEntityDataCap().getCombatExtension().getAttackStrengthTicks(hand);
    }

    public int getMeleeWindupTicks(InteractionHand hand) {
        return Mth.clamp(Mth.ceil(getMeleeCooldownPeriod(hand) * 0.3), 3, 16);
    }

    public int getMeleeWindupRecoveryTicks(InteractionHand hand) {
        return Mth.clamp(Mth.ceil(getMeleeCooldownPeriod(hand) * 0.45), 4, 24);
    }

    protected double getVisualMeleeWindupRangeMultiplier() {
        return 1.15;
    }

    protected double getVisualMeleeWindupRangeSqr(LivingEntity target) {
        double range = getEntityReach() * getScale() * getVisualMeleeWindupRangeMultiplier();
        return range * range;
    }

    public boolean shouldShowMeleeWindup(InteractionHand hand) {
        if (getCombatMoveType() != CombatMoveType.MELEE || getVisualCastState() != VisualCastState.NONE) {
            return false;
        }
        if (isUsingItem()) {
            return false;
        }
        if (hand == InteractionHand.OFF_HAND && !MKMeleeManager.canUseForAttack(this, InteractionHand.OFF_HAND)) {
            return false;
        }
        return isAggressive() || (swinging && swingingArm == hand) || getVisualMeleeWindupTicks(hand) > 0;
    }

    public float getMeleeWindupProgress(InteractionHand hand, float partialTicks) {
        int windupTicks = getMeleeWindupTicks(hand);
        if (windupTicks <= 0 || getVisualMeleeWindupRecoveryTicks(hand) > 0 || !shouldShowMeleeWindup(hand)) {
            return 0.0F;
        }
        return Mth.clamp((getVisualMeleeWindupTicks(hand) + partialTicks) / windupTicks, 0.0F, 1.0F);
    }

    public int getCurrentLocalSwingVariant(InteractionHand hand) {
        VisualMeleeAttackSequence sequence = getVisualMeleeAttackSequence(hand);
        if (sequence.hasSequence()) {
            return sequence.getLocalSwingVariant();
        }
        return getVisualMeleeState(hand).localSwingVariant;
    }
    
    public int getCurrentStrikePoseIndex(InteractionHand hand) {
        VisualMeleeAttackSequence sequence = getVisualMeleeAttackSequence(hand);
        if (sequence.hasSequence() && sequence.getActiveSwingIndex() >= 0) {
            return getVisualMeleeAttackBaseVariant(hand) + sequence.getActiveSwingIndex();
        }
        return getCurrentLocalSwingVariant(hand) - 1;
    }

    public int getCurrentMeleeWindupVariant(InteractionHand hand) {
        return getVisualMeleeState(hand).windupVariant;
    }

    @Override
    public void startVisualMeleeAttackSequence(InteractionHand hand, int[] swingStartTicks, int[] swingDurationTicks) {
        VisualMeleeAttackSequence sequence = getVisualMeleeAttackSequence(hand);
        setVisualMeleeAttackBaseVariant(hand, resolveVisualMeleeAttackBaseVariant(hand, sequence));
        sequence.start(swingStartTicks, swingDurationTicks);
    }

    @Override
    public float getVisualMeleeAttackAnim(InteractionHand hand, float partialTicks) {
        VisualMeleeAttackSequence sequence = getVisualMeleeAttackSequence(hand);
        float visualAttack = sequence.getAttackAnim(partialTicks);
        return sequence.isActiveSwing(partialTicks) ? visualAttack : getAttackAnim(partialTicks);
    }

    @Override
    public boolean hasVisualMeleeAttackSequence(InteractionHand hand) {
        return getVisualMeleeAttackSequence(hand).hasSequence();
    }

    @Override
    public boolean hasActiveVisualMeleeAttack(InteractionHand hand, float partialTicks) {
        return getVisualMeleeAttackSequence(hand).isActiveSwing(partialTicks);
    }

    private VisualMeleeAttackSequence getVisualMeleeAttackSequence(InteractionHand hand) {
        return getVisualMeleeState(hand).attackSequence;
    }

    private int resolveVisualMeleeAttackBaseVariant(InteractionHand hand, VisualMeleeAttackSequence sequence) {
        if (getVisualMeleeWindupTicks(hand) > 0) {
            return getCurrentMeleeWindupVariant(hand);
        }
        return sequence.getLocalSwingVariant();
    }

    private void tickVisualMeleeWindupState(InteractionHand hand) {
        if (getVisualMeleeWindupRecoveryTicks(hand) > 0) {
            setVisualMeleeWindupRecoveryTicks(hand, getVisualMeleeWindupRecoveryTicks(hand) - 1);
            setVisualMeleeWindupTicks(hand, 0);
            return;
        }
        if (shouldShowMeleeWindup(hand)) {
            if (getVisualMeleeWindupTicks(hand) == 0) {
                setCurrentMeleeWindupVariant(hand, getNextVisualMeleeWindupVariant(hand));
                incrementNextVisualMeleeWindupVariant(hand);
            }
            setVisualMeleeWindupTicks(hand, Math.min(getVisualMeleeWindupTicks(hand) + 1, getMeleeWindupTicks(hand)));
            return;
        }
        setVisualMeleeWindupTicks(hand, 0);
    }

    private void resetVisualMeleeWindup(InteractionHand hand) {
        setVisualMeleeWindupTicks(hand, 0);
        setVisualMeleeWindupRecoveryTicks(hand, getMeleeWindupRecoveryTicks(hand));
    }

    private int getVisualMeleeWindupTicks(InteractionHand hand) {
        return getVisualMeleeState(hand).windupTicks;
    }

    private void setVisualMeleeWindupTicks(InteractionHand hand, int value) {
        getVisualMeleeState(hand).windupTicks = value;
    }

    private int getVisualMeleeWindupRecoveryTicks(InteractionHand hand) {
        return getVisualMeleeState(hand).windupRecoveryTicks;
    }

    private void setVisualMeleeWindupRecoveryTicks(InteractionHand hand, int value) {
        getVisualMeleeState(hand).windupRecoveryTicks = value;
    }

    private void setCurrentMeleeWindupVariant(InteractionHand hand, int value) {
        getVisualMeleeState(hand).windupVariant = value;
    }

    private int getNextVisualMeleeWindupVariant(InteractionHand hand) {
        return getVisualMeleeState(hand).nextWindupVariant;
    }

    private void incrementNextVisualMeleeWindupVariant(InteractionHand hand) {
        getVisualMeleeState(hand).nextWindupVariant++;
    }

    private int getVisualMeleeAttackBaseVariant(InteractionHand hand) {
        return getVisualMeleeState(hand).visualAttackBaseVariant;
    }

    private void setVisualMeleeAttackBaseVariant(InteractionHand hand, int variant) {
        getVisualMeleeState(hand).visualAttackBaseVariant = variant;
    }

    private void incrementLocalSwingVariant(InteractionHand hand) {
        getVisualMeleeState(hand).localSwingVariant++;
    }

    private VisualMeleeHandState getVisualMeleeState(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? offHandVisualMeleeState : mainHandVisualMeleeState;
    }

    public VisualCastState getVisualCastState() {
        return visualCastState;
    }

    public int getCastAnimTimer() {
        return castAnimTimer;
    }

    public MKAbility getCastingAbility() {
        return castingAbility;
    }

    public void startCast(MKAbility ability, int totalTicks) {
        visualCastState = VisualCastState.CASTING;
        castingAbility = ability;
        castTicks = totalTicks;
        currentCastTicks = 0;
    }

    public void interruptCast(MKAbility ability, CastInterruptReason reason) {
        castingAbility = null;
        castAnimTimer = 0;
        visualCastState = VisualCastState.NONE;
        currentCastTicks = 0;
        castTicks = 0;
    }

    public void returnToDefaultMovementState() {
        LivingEntity target = getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET.get()).orElse(null);
        if (target != null) {
            enterCombatMovementState(target);
        } else {
            enterNonCombatMovementState();
        }
    }

    public void endCast(MKAbility ability) {
        castingAbility = ability;
        visualCastState = VisualCastState.RELEASE;
        castAnimTimer = 15;
        currentCastTicks = 0;
        castTicks = 0;
    }

    public void setCombatMoveType(CombatMoveType combatMoveType) {
        this.combatMoveType = combatMoveType;
    }

    public void setNonCombatMoveType(NonCombatMoveType nonCombatMoveType) {
        this.nonCombatMoveType = nonCombatMoveType;
    }


    public NonCombatMoveType getNonCombatMoveType() {
        return nonCombatMoveType;
    }

    public CombatMoveType getCombatMoveType() {
        return combatMoveType;
    }

    public int getWanderRange() {
        return 10;
    }

    @Override
    public void enterCombatMovementState(LivingEntity target) {
        getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET.get(), target);
        switch (getCombatMoveType()) {
            case STATIONARY -> MovementStrategyController.enterStationary(this);
            case RANGE -> MovementStrategyController.enterCastingMode(this, getMinimumRangedCastingDistance(), canFly());
            default -> MovementStrategyController.enterMeleeMode(this, 1);
        }
    }

    public double getMinimumRangedCastingDistance(){
        return rangedCastingDistance;
    }

    public void setMinimumRangedCastingDistance(double rangedCastingDistance) {
        this.rangedCastingDistance = rangedCastingDistance;
    }

    @Override
    public void enterNonCombatMovementState() {
        if (nonCombatBehavior != null) {
            if (nonCombatBehavior.getBehaviorType() == PetNonCombatBehavior.Behavior.FOLLOW) {
                nonCombatBehavior.getEntity().ifPresent(x -> MovementStrategyController.enterFollowMode(this, 2, x));
            } else if (nonCombatBehavior.getBehaviorType() == PetNonCombatBehavior.Behavior.GUARD) {
                nonCombatBehavior.getPos().ifPresent(x -> getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT.get(), BlockPos.containing(x)));
            }
        } else {
            switch (getNonCombatMoveType()) {
                case RANDOM_WANDER -> enterWanderState();
                default -> MovementStrategyController.enterStationary(this);
            }
        }
    }

    protected void enterWanderState() {
        MovementStrategyController.enterRandomWander(this);
    }

    public boolean hasThreatTarget() {
        return getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET.get()).isPresent();
    }

    public void reduceThreat(LivingEntity entity, float value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP.get());
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).subtractThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP.get(), newMap);
    }

    @Override
    public void setTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setTarget(entitylivingbaseIn);
    }

    public double getAttackSpeedMultiplier() {
        double attackSpeed = getAttributeValue(Attributes.ATTACK_SPEED);
        return attackSpeed / Math.max(getBaseAttackSpeedValueWithItem(InteractionHand.MAIN_HAND), 0.001D);
    }

    public double getMeleeCooldownPeriod(InteractionHand hand) {
        if (getItemInHand(hand).isEmpty()) {
            return GameConstants.TICKS_PER_SECOND;
        }
        double effectiveAttackSpeed = getProjectedAttackSpeed(hand);
        return GameConstants.TICKS_PER_SECOND / Math.max(effectiveAttackSpeed, 0.001D);
    }

    public int getMeleeSwingDurationTicks(InteractionHand hand) {
        double projectedMultiplier = getProjectedAttackSpeed(hand) / Math.max(getBaseAttackSpeedValueWithItem(hand), 0.001D);
        return Mth.clamp(Mth.ceil(6.0D / Math.max(projectedMultiplier, 0.001D)), 2, 24);
    }

    @Override
    protected void updateSwingTime() {
        InteractionHand hand = swingingArm == null ? InteractionHand.MAIN_HAND : swingingArm;
        int duration = getMeleeSwingDurationTicks(hand);
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= duration) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }

        this.attackAnim = (float) this.swingTime / (float) duration;
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        ItemStack stack = getItemInHand(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(this)) {
            return;
        }
        int duration = getMeleeSwingDurationTicks(hand);
        if (!this.swinging || this.swingTime >= duration / 2 || this.swingTime < 0) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = hand;
            if (this.level() instanceof ServerLevel serverLevel) {
                ClientboundAnimatePacket packet = new ClientboundAnimatePacket(this, hand == InteractionHand.MAIN_HAND ? 0 : 3);
                ServerChunkCache chunkSource = serverLevel.getChunkSource();
                if (updateSelf) {
                    chunkSource.broadcastAndSend(this, packet);
                } else {
                    chunkSource.broadcast(this, packet);
                }
            }
        }
    }

    public double getBaseAttackSpeedValueWithItem(InteractionHand hand) {
        ItemStack itemInHand = getItemInHand(hand);
        double baseValue = getAttributeBaseValue(Attributes.ATTACK_SPEED);
        if (!itemInHand.isEmpty()) {
            var modifiers = itemInHand.getAttributeModifiers();
            double attackSpeed = 4.0;
            MutableDouble modifiedBase = new MutableDouble(attackSpeed);
            modifiers.forEach(EquipmentSlot.MAINHAND, (a, b) -> {
                if (a == Attributes.ATTACK_SPEED && b.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    modifiedBase.add(b.amount());
                }
            });
            baseValue = modifiedBase.doubleValue();
        }
        return baseValue;
    }

    public double getProjectedAttackSpeed(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return getAttributeValue(Attributes.ATTACK_SPEED);
        }
        double currentAttackSpeed = getAttributeValue(Attributes.ATTACK_SPEED);
        double mainHandAttackSpeed = getItemAddValueModifier(getMainHandItem(), Attributes.ATTACK_SPEED);
        double selectedHandAttackSpeed = getItemAddValueModifier(getItemInHand(hand), Attributes.ATTACK_SPEED);
        return currentAttackSpeed - mainHandAttackSpeed + selectedHandAttackSpeed;
    }

    public double getProjectedAttackDamage(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        double currentAttackDamage = getAttributeValue(Attributes.ATTACK_DAMAGE);
        double mainHandAttackDamage = getItemAddValueModifier(getMainHandItem(), Attributes.ATTACK_DAMAGE);
        double selectedHandAttackDamage = getItemAddValueModifier(getItemInHand(hand), Attributes.ATTACK_DAMAGE);
        return currentAttackDamage - mainHandAttackDamage + selectedHandAttackDamage;
    }

    public double getProjectedAttackKnockback(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        }
        double currentAttackKnockback = getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double mainHandAttackKnockback = getItemAddValueModifier(getMainHandItem(), Attributes.ATTACK_KNOCKBACK);
        double selectedHandAttackKnockback = getItemAddValueModifier(getItemInHand(hand), Attributes.ATTACK_KNOCKBACK);
        return currentAttackKnockback - mainHandAttackKnockback + selectedHandAttackKnockback;
    }

    @Override
    public ItemStack getWeaponItem() {
        return getItemInHand(getEntityDataCap().getCombatExtension().getActiveAttackHand());
    }

    @Override
    public boolean canDisableShield() {
        ItemStack weaponItem = getWeaponItem();
        return weaponItem.canDisableShield(this.useItem, this, this);
    }

    private static double getItemAddValueModifier(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        final double[] total = {0.0D};
        stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (holder, modifier) -> {
            if (holder.equals(attribute) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.level().getProfiler().push("brain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
    }

    @Override
    public void absMoveTo(double x, double y, double z, float yaw, float pitch) {
        super.absMoveTo(x, y, z, yaw, pitch);
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
        this.setYHeadRot(yaw);
        this.yHeadRotO = yaw;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
        return -worldIn.getPathfindingCostFromLightLevels(pos);
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity livingEntity) {
            addThreat(livingEntity, amount * NpcConstants.DAMAGE_THREAT_MULTIPLIER, true);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        if (hasNextStage()) {
            BossStage next = getNextStage();
            next.apply(this);
            next.transition(this);
            setHealth(getMaxHealth());
            currentStage++;
            return;
        }
        super.die(cause);
    }

    public boolean hasThreatWithTarget(LivingEntity target) {
        return getBrain().getMemory(MKMemoryModuleTypes.THREAT_MAP.get()).map(x -> x.containsKey(target)).orElse(false);
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity target) {
        super.setLastHurtByMob(target);
        if (target != null) {
            addThreat(target, NpcConstants.INITIAL_THREAT, true);
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (hand.equals(InteractionHand.MAIN_HAND) && IMobFaction.get(this)
                .map((cap) -> cap.getRelationToEntity(player) != Targeting.TargetRelation.ENEMY).orElse(false)) {
            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                if (player.isShiftKeyDown()) {
                    player.openMenu(entityTradeContainer);
                } else {
                    IEntityNpcData.get(this).ifPresent(cap -> cap.receiveInteract(player, vec, hand));
                    INpcDialogue.get(this)
                            .ifPresent(cap -> cap.hail(serverPlayer));
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getHighestThreat() {
        return getBrain().getMemory(MKMemoryModuleTypes.THREAT_MAP.get()).map(x -> {
            List<ThreatMapEntry> sorted = x.values().stream()
                    .sorted(Comparator.comparingDouble(ThreatMapEntry::getCurrentThreat))
                    .toList();
            if (sorted.isEmpty()) {
                return 0f;
            }
            return sorted.get(sorted.size() - 1).getCurrentThreat();
        }).orElse(0f);
    }


    @Override
    public Brain<MKEntity> getBrain() {
        return (Brain<MKEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(
                ImmutableList.of(
                        MKMemoryModuleTypes.ALLIES.get(),
                        MKMemoryModuleTypes.ENEMIES.get(),
                        MKMemoryModuleTypes.THREAT_LIST.get(),
                        MKMemoryModuleTypes.THREAT_MAP.get(),
                        MKMemoryModuleTypes.VISIBLE_ENEMIES.get(),
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.PATH,
                        MKMemoryModuleTypes.MOVEMENT_STRATEGY.get(),
                        MKMemoryModuleTypes.MOVEMENT_TARGET.get(),
                        MKMemoryModuleTypes.CURRENT_ABILITY.get(),
                        MKAbilityMemories.ABILITY_TARGET.get(),
                        MKMemoryModuleTypes.SPAWN_POINT.get(),
                        MKMemoryModuleTypes.IS_RETURNING.get(),
                        MKMemoryModuleTypes.ABILITY_TIMEOUT.get(),
                        MKAbilityMemories.ABILITY_POSITION_TARGET.get(),
                        MKAbilityMemories.CURRENT_PROJECTILES.get(),
                        MKAbilityMemories.CURRENT_AREA_EFFECTS.get()
                ),
                ImmutableList.of(
                        MKSensorTypes.ENTITIES_SENSOR.get(),
                        MKSensorTypes.THREAT_SENSOR.get(),
                        MKSensorTypes.DESTINATION_SENSOR.get(),
                        MKSensorTypes.ABILITY_SENSOR.get()
                ));
    }

    @Override
    public ItemStack getProjectile(ItemStack shootable) {
        if (shootable.getItem() instanceof ProjectileWeaponItem projectileWeaponItem) {
            Predicate<ItemStack> predicate = projectileWeaponItem.getSupportedHeldProjectiles();
            ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
