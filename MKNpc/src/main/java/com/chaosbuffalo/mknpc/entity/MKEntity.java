package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkchat.dialogue.DialogueUtils;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.capabilities.CoreCapabilities;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.pets.IMKPet;
import com.chaosbuffalo.mkcore.core.pets.PetNonCombatBehavior;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.entities.ISyncControllerProvider;
import com.chaosbuffalo.mkcore.sync.controllers.EntitySyncController;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.ItemUtils;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.NpcCapabilities;
import com.chaosbuffalo.mknpc.entity.ai.controller.MovementStrategyController;
import com.chaosbuffalo.mknpc.entity.ai.goal.*;
import com.chaosbuffalo.mknpc.entity.ai.memory.MKMemoryModuleTypes;
import com.chaosbuffalo.mknpc.entity.ai.memory.ThreatMapEntry;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.FollowMovementStrategy;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class MKEntity extends PathfinderMob implements IModelLookProvider, RangedAttackMob, ISyncControllerProvider, IMKPet, ITargetingOwner {
    private static final EntityDataAccessor<String> LOOK_STYLE = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_GHOST = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> GHOST_TRANSLUCENCY = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private final PlayerSyncComponent animSync = new PlayerSyncComponent("anim");
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

    private int blockDelay;
    private int blockHold;
    private int blockCooldown;

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

    public enum NonCombatMoveType {
        STATIONARY,
        RANDOM_WANDER
    }

    public enum VisualCastState {
        NONE,
        CASTING,
        RELEASE,
    }

    public void setGhost(boolean ghost) {
        getEntityData().set(IS_GHOST, ghost);
    }

    public boolean isGhost() {
        return getEntityData().get(IS_GHOST);
    }

    public void setGhostTranslucency(float ghostTranslucency) {
        getEntityData().set(GHOST_TRANSLUCENCY, ghostTranslucency);
    }

    @Nullable
    @Override
    public Entity getTargetingOwner() {
        return entityDataCap.getPets().getOwner();
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
        visualCastState = VisualCastState.NONE;
        castingAbility = null;
        battlecry = null;
        lungeSpeed = .25;
        blockCooldown = GameConstants.TICKS_PER_SECOND * 2;
        blockDelay = GameConstants.TICKS_PER_SECOND / 2;
        blockHold = GameConstants.TICKS_PER_SECOND * 2;
        syncController = new EntitySyncController(this);
        animSync.attach(syncController);
        particleEffectTracker = ParticleEffectInstanceTracker.getTracker(this);
        animSync.addPublic(particleEffectTracker);
        nonCombatMoveType = NonCombatMoveType.RANDOM_WANDER;
        combatMoveType = CombatMoveType.MELEE;

        entityDataCap = getCapability(CoreCapabilities.ENTITY_CAPABILITY).orElseThrow(IllegalStateException::new);
        entityDataCap.attachUpdateEngine(syncController);
        entityDataCap.getAbilityExecutor().setStartCastCallback(this::startCast);
        entityDataCap.getAbilityExecutor().setCompleteAbilityCallback(this::endCast);
        entityDataCap.setInstanceTracker(particleEffectTracker);
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

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide()) {
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

    protected void setupDifficulty(Difficulty difficulty) {
        AttributeInstance inst = getAttribute(MKAttributes.CASTING_SPEED);
        if (inst != null) {
            inst.addTransientModifier(new AttributeModifier("difficulty",
                    getCastingSpeedForDifficulty(difficulty), AttributeModifier.Operation.MULTIPLY_TOTAL));
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
                inst.addTransientModifier(new AttributeModifier("heal_scaling",
                        adjustForBase, AttributeModifier.Operation.ADDITION));
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
                .add(MKNpcAttributes.AGGRO_RANGE.get(), 6)
                .add(ForgeMod.ENTITY_REACH.get())
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LOOK_STYLE, "default");
        this.entityData.define(SCALE, 1.0f);
        this.entityData.define(IS_GHOST, false);
        this.entityData.define(GHOST_TRANSLUCENCY, 1.0f);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        attackEntityWithRangedAttack(target, distanceFactor, 1.6f);
    }

    public double getEntityReach() {
        return getAttributeValue(ForgeMod.ENTITY_REACH.get());
    }

    @Override
    protected void dropExperience() {
        super.dropExperience();
    }

    public void attackEntityWithRangedAttack(LivingEntity target, float launchPower, float launchVelocity) {
        ItemStack arrowStack = this.getProjectile(this.getItemInHand(InteractionHand.MAIN_HAND));
        AbstractArrow arrowEntity = ProjectileUtil.getMobArrow(this, arrowStack, launchPower);
        if (getMainHandItem().getItem() instanceof BowItem bow) {
            arrowEntity = bow.customArrow(arrowEntity);
        }
        EntityUtils.shootArrow(this, arrowEntity, target, launchPower * launchVelocity);
        this.playSound(getShootSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(arrowEntity);
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
        this.goalSelector.addGoal(priority++, new UseAbilityGoal(this));
        this.goalSelector.addGoal(priority++, new MKBowAttackGoal(this, 5, 15.0f));
        this.goalSelector.addGoal(priority++, new MKBlockGoal(this));
        this.meleeAttackGoal = new MKMeleeAttackGoal(this);
        this.goalSelector.addGoal(priority++, meleeAttackGoal);
        this.goalSelector.addGoal(priority++, new LookAtThreatTargetGoal(this));
        this.targetSelector.addGoal(3, new MKTargetGoal(this, true, true));

    }

    public boolean avoidsWater() {
        return true;
    }

    private void handleCombatMovementDetect(ItemStack stack) {
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
        getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY).ifPresent(faction -> {
            if (faction.hasFaction()) {
                MKCore.getEntityData(target).ifPresent(entityData -> {
                    if (entityData.getStats().getTimer(faction.getBattlecryName()) <= 0) {
                        DialogueUtils.sendMessageToAllAround(this,
                                DialogueUtils.formatSpeakerMessage(this, battlecry));
                        entityData.getStats().setTimer(faction.getBattlecryName(), BATTLECRY_COOLDOWN);
                    }
                });
            }
        });
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
    public boolean wasKilled(ServerLevel world, LivingEntity killedEntity) {
        super.wasKilled(world, killedEntity);
        enterNonCombatMovementState();
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
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return super.getStandingEyeHeight(poseIn, sizeIn) * entityData.get(SCALE);
    }

    @Override
    public String getCurrentModelLook() {
        return entityData.get(LOOK_STYLE);
    }

    @Override
    public void setCurrentModelLook(String group) {
        entityData.set(LOOK_STYLE, group);
    }

    public MovementStrategy getMovementStrategy(AbilityTargetingDecision decision) {
        MKAbility ability = decision.getAbility();
        if (ability == null) {
            return StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY;
        }
        switch (decision.getMovementSuggestion()) {
            case KITE:
                return new KiteMovementStrategy(Math.max(ability.getDistance(this) * .5, 8));
            case FOLLOW:
                return new FollowMovementStrategy(1.0f, Math.round(ability.getDistance(this) / 2.0f));
            case MELEE:
                return new FollowMovementStrategy(1.0f, 1);
            case STATIONARY:
            default:
                return StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY;
        }
    }

    public void returnToSpawnTick() {
        boolean isReturningToPlayer = entityDataCap.getPets().isPet() && entityDataCap.getPets().getOwner() instanceof Player;
        if (!isReturningToPlayer) {
            setHealth(Math.min(getHealth() + getMaxHealth() * .2f * 1.0f / GameConstants.TICKS_PER_SECOND,
                    getMaxHealth()));
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        MKNpc.LOGGER.info("In initial spawn for {}", this);
        SpawnGroupData entityData = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        this.getCapability(NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY).ifPresent((cap) -> {
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
        Map<LivingEntity, ThreatMapEntry> newMap = brain.getMemory(MKMemoryModuleTypes.THREAT_MAP.get())
                .orElseGet(HashMap::new);
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).addThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP.get(), newMap);
        if (propagate) {
            if (entityDataCap.getPets().hasPet()) {
                entityDataCap.getPets().addThreatToPets(entity, value, false);
            }
        }
    }

    @Override
    public void setNoncombatBehavior(PetNonCombatBehavior petNonCombatBehavior) {
        nonCombatBehavior = petNonCombatBehavior;
        enterNonCombatMovementState();
    }

    protected void updateEntityCastState() {
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
        updateEntityCastState();
        attackStrengthTicker++;
        super.aiStep();
        if (nonCombatBehavior != null && !hasThreatTarget()) {
            nonCombatBehavior.getEntity().ifPresent(x -> getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT.get(), x.blockPosition()));
        }
    }

    public void resetSwing() {
        attackStrengthTicker = 0;
    }

    public void subtractFromTicksSinceLastSwing(int toSubtract) {
        attackStrengthTicker -= toSubtract;
    }

    public int getTicksSinceLastSwing() {
        return attackStrengthTicker;
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

    public void startCast(MKAbility ability) {
        visualCastState = VisualCastState.CASTING;
        castingAbility = ability;
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
            case RANGE -> MovementStrategyController.enterCastingMode(this, 6.0);
            default -> MovementStrategyController.enterMeleeMode(this, 1);
        }
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
                case RANDOM_WANDER -> MovementStrategyController.enterRandomWander(this);
                default -> MovementStrategyController.enterStationary(this);
            }
        }
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
        return attackSpeed / getBaseAttackSpeedValueWithItem();
    }

    public double getBaseAttackSpeedValueWithItem() {
        ItemStack itemInHand = getMainHandItem();
        double baseValue = getAttributeBaseValue(Attributes.ATTACK_SPEED);
        if (!itemInHand.isEmpty()) {
            var modifiers = itemInHand.getAttributeModifiers(EquipmentSlot.MAINHAND);
            if (modifiers.containsKey(Attributes.ATTACK_SPEED)) {
                Collection<AttributeModifier> itemAttackSpeed = modifiers.get(Attributes.ATTACK_SPEED);
                double attackSpeed = 4.0;
                for (AttributeModifier mod : itemAttackSpeed) {
                    if (mod.getOperation().equals(AttributeModifier.Operation.ADDITION)) {
                        attackSpeed += mod.getAmount();
                    }
                }
                baseValue = attackSpeed;
            }
        }
        return baseValue;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.level.getProfiler().push("brain");
        this.getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();
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
        if (hand.equals(InteractionHand.MAIN_HAND) && getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .map((cap) -> cap.getRelationToEntity(player) != Targeting.TargetRelation.ENEMY).orElse(false)) {
            if (!player.level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                if (player.isShiftKeyDown()) {
                    player.openMenu(entityTradeContainer);
                } else {
                    getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY)
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
                        MKAbilityMemories.ABILITY_POSITION_TARGET.get()
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

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
