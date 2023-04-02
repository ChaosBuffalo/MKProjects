package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mkchat.capabilities.ChatCapabilities;
import com.chaosbuffalo.mkcore.CoreCapabilities;
import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ai.AbilityTargetingDecision;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.pets.IMKPet;
import com.chaosbuffalo.mkcore.core.pets.PetNonCombatBehavior;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.core.player.SyncComponent;
import com.chaosbuffalo.mkcore.entities.IUpdateEngineProvider;
import com.chaosbuffalo.mkcore.sync.EntityUpdateEngine;
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
import com.chaosbuffalo.mknpc.entity.attributes.NpcAttributes;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.inventories.QuestGiverInventoryContainer;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.utils.NpcConstants;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.targeting_api.ITargetingOwner;
import com.chaosbuffalo.targeting_api.Targeting;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.*;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

@SuppressWarnings("EntityConstructor")
public abstract class MKEntity extends PathfinderMob implements IModelLookProvider, RangedAttackMob, IUpdateEngineProvider, IMKPet, ITargetingOwner {
    private static final EntityDataAccessor<String> LOOK_STYLE = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_GHOST = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> GHOST_TRANSLUCENCY = SynchedEntityData.defineId(MKEntity.class, EntityDataSerializers.FLOAT);
    private final SyncComponent animSync = new SyncComponent("anim");
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
    private final EntityUpdateEngine updateEngine;
    private final ParticleEffectInstanceTracker particleEffectTracker;
    private final EntityTradeContainer entityTradeContainer;
    private final List<BossStage> bossStages = new ArrayList<>();
    private int currentStage;
    @Nullable
    private PetNonCombatBehavior nonCombatBehavior;


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
        IMKEntityData data = MKCore.getEntityDataOrNull(this);
        if (data != null) {
            return data.getPets().getOwner();
        } else {
            return null;
        }
    }

    public float getGhostTranslucency() {
        return getEntityData().get(GHOST_TRANSLUCENCY);
    }

    protected MKEntity(EntityType<? extends PathfinderMob> type, Level worldIn) {
        super(type, worldIn);
        if (!worldIn.isClientSide()){
            setAttackComboStatsAndDefault(1, GameConstants.TICKS_PER_SECOND);
            setupDifficulty(worldIn.getDifficulty());
        }
        entityTradeContainer = new EntityTradeContainer(this);
        castAnimTimer = 0;
        currentStage = 0;
        visualCastState = VisualCastState.NONE;
        castingAbility = null;
        lungeSpeed = .25;
        updateEngine = new EntityUpdateEngine(this);
        animSync.attach(updateEngine);
        particleEffectTracker = ParticleEffectInstanceTracker.getTracker(this);
        animSync.addPublic(particleEffectTracker);
        nonCombatMoveType = NonCombatMoveType.RANDOM_WANDER;
        combatMoveType = CombatMoveType.MELEE;
        getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent((mkEntityData -> {
            mkEntityData.attachUpdateEngine(updateEngine);
            mkEntityData.getAbilityExecutor().setStartCastCallback(this::startCast);
            mkEntityData.getAbilityExecutor().setCompleteAbilityCallback(this::endCast);
        }));
    }

    public boolean hasBossStages(){
        return !bossStages.isEmpty();
    }

    public int getCurrentStage() {
        return currentStage;
    }

    public void addBossStage(BossStage stage){
        if (!hasBossStages()){
            stage.apply(this);
        }
        bossStages.add(stage);
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return !isGhost() && super.isInvisibleTo(player);
    }

    @Override
    public boolean isInvisible() {
        return isGhost() || super.isInvisible();
    }

    public boolean hasNextStage(){
        return bossStages.size() > getCurrentStage() + 1;
    }

    public BossStage getNextStage(){
        return bossStages.get(getCurrentStage() + 1);
    }

    protected double getCastingSpeedForDifficulty(Difficulty difficulty){
        switch (difficulty){
            case NORMAL:
                return 0.5;
            case HARD:
                return 0.75;
            case EASY:
            default:
                return 0.25;
        }
    }

    protected void setupDifficulty(Difficulty difficulty){
        AttributeInstance inst = getAttribute(MKAttributes.CASTING_SPEED);
        if (inst != null){
            inst.addTransientModifier(new AttributeModifier("difficulty",
                    getCastingSpeedForDifficulty(difficulty), AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }


    public float getTranslucency(){
        // vanilla value is 0.15f
        return isGhost() ? getGhostTranslucency() : 0.15f;
    }

    public void postDefinitionApply(NpcDefinition definition){
        float maxHealth = getMaxHealth();
        if (maxHealth > 100.0f){
            float ratio = maxHealth / 100.0f;
            float adjustForBase = ratio - 1.0f;
            AttributeInstance inst = getAttribute(MKAttributes.HEAL_EFFICIENCY);
            if (inst != null){
                inst.addTransientModifier(new AttributeModifier("heal_scaling",
                        adjustForBase, AttributeModifier.Operation.ADDITION));
            }
        }
    }



    public ParticleEffectInstanceTracker getParticleEffectTracker() {
        return particleEffectTracker;
    }

    @Override
    public EntityUpdateEngine getUpdateEngine() {
        return updateEngine;
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
                .add(NpcAttributes.AGGRO_RANGE, 6)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
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

    @Override
    protected void dropExperience() {
        super.dropExperience();
    }

    public void attackEntityWithRangedAttack(LivingEntity target, float launchPower, float launchVelocity) {
        ItemStack arrowStack = this.getProjectile(this.getItemInHand(InteractionHand.MAIN_HAND));
        AbstractArrow arrowEntity = ProjectileUtil.getMobArrow(this, arrowStack, launchPower);
        Item mainhand = this.getMainHandItem().getItem();
        if (mainhand instanceof BowItem){
            arrowEntity = ((BowItem) this.getMainHandItem().getItem()).customArrow(arrowEntity);
        }
        EntityUtils.shootArrow(this, arrowEntity, target, launchPower * launchVelocity);
        this.playSound(getShootSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(arrowEntity);
    }

    protected SoundEvent getShootSound(){
        return SoundEvents.ARROW_SHOOT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    protected SoundEvent getStepSound(){
        return SoundEvents.ZOMBIE_VILLAGER_STEP;
    }

    @Override
    public float getScale() {
        return entityData.get(SCALE);
    }

    public void setRenderScale(float newScale){
        entityData.set(SCALE, newScale);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(7, new LookAtThreatTargetGoal(this));
        this.targetSelector.addGoal(3, new MKTargetGoal(this, true, true));
        this.goalSelector.addGoal(0, new ReturnToSpawnGoal(this));
        this.goalSelector.addGoal(2, new MovementGoal(this));
        this.meleeAttackGoal =  new MKMeleeAttackGoal(this);
        this.goalSelector.addGoal(4, new MKBowAttackGoal(this, 5, 15.0f));
        this.goalSelector.addGoal(5, meleeAttackGoal);
        this.goalSelector.addGoal(3, new UseAbilityGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    public boolean avoidsWater(){
        return true;
    }

    private void handleCombatMovementDetect(ItemStack stack){
        if (ItemUtils.isRangedWeapon(stack)){
            setCombatMoveType(CombatMoveType.RANGE);
        } else {
            setCombatMoveType(CombatMoveType.MELEE);
        }
    }

    public void callForHelp(LivingEntity entity, float threatVal){
       brain.getMemory(MKMemoryModuleTypes.ALLIES).ifPresent(x -> {
           x.forEach(ent -> {
               if (ent instanceof MKEntity){
                   if (ent.distanceToSqr(this) < 9.0){
                       ((MKEntity) ent).addThreat(entity, threatVal, true);
                   }
               }
           });
       });
    }

    @Override
    public void setItemInHand(InteractionHand hand, ItemStack stack) {
        super.setItemInHand(hand, stack);
        if (hand == InteractionHand.MAIN_HAND){
            handleCombatMovementDetect(stack);
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if (slotIn == EquipmentSlot.MAINHAND){
            handleCombatMovementDetect(stack);
        }
    }

    @Override
    public void killed(ServerLevel world, LivingEntity killedEntity) {
        super.killed(world, killedEntity);
        enterNonCombatMovementState();
    }

    public MKMeleeAttackGoal getMeleeAttackGoal(){
        return meleeAttackGoal;
    }

    public void setComboDefaults(int count, int cooldown){
        comboCountDefault = count;
        comboCooldownDefault = cooldown;
    }

    public void setAttackComboStatsAndDefault(int count, int cooldown){
        setComboDefaults(count, cooldown);
        restoreComboDefaults();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public void restoreComboDefaults(){
        setAttackComboCount(comboCountDefault);
        setAttackComboCooldown(comboCooldownDefault);
    }

    public void setAttackComboCount(int count){
        comboCount = count;
    }

    public int getAttackComboCount(){
        return comboCount;
    }

    public void setAttackComboCooldown(int ticks){
        comboCooldown = ticks;
    }

    public int getAttackComboCooldown(){
        return comboCooldown;
    }

    @Override
    public void clearThreat() {
        getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_MAP);
        getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_TARGET);
        getBrain().eraseMemory(MKMemoryModuleTypes.THREAT_LIST);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(SCALE)){
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

    public MovementStrategy getMovementStrategy(AbilityTargetingDecision decision){
        MKAbility ability = decision.getAbility();
        if (ability == null){
            return StationaryMovementStrategy.STATIONARY_MOVEMENT_STRATEGY;
        }
        switch (decision.getMovementSuggestion()){
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

    public void returnToSpawnTick(){
        boolean isReturningToPlayer = MKCore.getEntityData(this).map(x -> x.getPets().isPet()
                && x.getPets().getOwner() instanceof Player).orElse(false);
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
            if (cap.wasMKSpawned()){
                getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT, cap.getSpawnPos());
            }
        });
        enterNonCombatMovementState();
        return entityData;
    }

    @Override
    public void addThreat(LivingEntity entity, float value, boolean propagate) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).addThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP, newMap);
        if (propagate) {
            MKCore.getEntityData(this).ifPresent(x -> {
                if (x.getPets().hasPet()) {
                    x.getPets().addThreatToPets(entity, value, false);
                }
            });
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
        if (nonCombatBehavior != null && !hasThreatTarget()){
            nonCombatBehavior.getEntity().ifPresent(x -> getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT, x.blockPosition()));
        }
    }

    public void resetSwing(){
        attackStrengthTicker = 0;
    }

    public void subtractFromTicksSinceLastSwing(int toSubtract){
        attackStrengthTicker -= toSubtract;
    }

    public int getTicksSinceLastSwing(){
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

    public void returnToDefaultMovementState(){
        LivingEntity target = getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET).orElse(null);
        if (target != null){
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

    public int getWanderRange(){
        return 10;
    }

    @Override
    public void enterCombatMovementState(LivingEntity target) {
        getBrain().setMemory(MKMemoryModuleTypes.MOVEMENT_TARGET, target);
        switch (getCombatMoveType()){
            case STATIONARY:
                MovementStrategyController.enterStationary(this);
                break;
            case RANGE:
                MovementStrategyController.enterCastingMode(this, 6.0);
                break;
            case MELEE:
            default:
                MovementStrategyController.enterMeleeMode(this, 1);
                break;
        }
    }

    @Override
    public void enterNonCombatMovementState() {
        if (nonCombatBehavior != null) {
            if (nonCombatBehavior.getBehaviorType() == PetNonCombatBehavior.Behavior.FOLLOW) {
                nonCombatBehavior.getEntity().ifPresent(x -> MovementStrategyController.enterFollowMode(this, 2, x));
            } else if (nonCombatBehavior.getBehaviorType() == PetNonCombatBehavior.Behavior.GUARD) {
                nonCombatBehavior.getPos().ifPresent(x -> getBrain().setMemory(MKMemoryModuleTypes.SPAWN_POINT, new BlockPos(x)));
            }
        } else {
            switch (getNonCombatMoveType()){
                case RANDOM_WANDER:
                    MovementStrategyController.enterRandomWander(this);
                    break;
                case STATIONARY:
                default:
                    MovementStrategyController.enterStationary(this);
                    break;
            }
        }
    }

    public boolean hasThreatTarget(){
        return getBrain().getMemory(MKMemoryModuleTypes.THREAT_TARGET).isPresent();
    }

    public void reduceThreat(LivingEntity entity, float value) {
        Optional<Map<LivingEntity, ThreatMapEntry>> threatMap = this.brain.getMemory(MKMemoryModuleTypes.THREAT_MAP);
        Map<LivingEntity, ThreatMapEntry> newMap = threatMap.orElse(new HashMap<>());
        newMap.put(entity, newMap.getOrDefault(entity, new ThreatMapEntry()).subtractThreat(value));
        this.brain.setMemory(MKMemoryModuleTypes.THREAT_MAP, newMap);
    }

    @Override
    public void setTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setTarget(entitylivingbaseIn);
    }

    public double getAttackSpeedMultiplier(){
        AttributeInstance attackSpeed = getAttribute(Attributes.ATTACK_SPEED);
        return attackSpeed.getValue() / getBaseAttackSpeedValueWithItem();
    }

    public double getBaseAttackSpeedValueWithItem(){
        ItemStack itemInHand = getMainHandItem();
        double baseValue = getAttribute(Attributes.ATTACK_SPEED).getBaseValue();
        if (!itemInHand.equals(ItemStack.EMPTY)) {
            if (itemInHand.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(
                    Attributes.ATTACK_SPEED)) {
                Collection<AttributeModifier> itemAttackSpeed = itemInHand.getAttributeModifiers(EquipmentSlot.MAINHAND)
                        .get(Attributes.ATTACK_SPEED);
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
        return 0.5F - worldIn.getBrightness(pos);
    }



    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity) {
            addThreat((LivingEntity) source.getEntity(), amount * NpcConstants.DAMAGE_THREAT_MULTIPLIER, true);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        if (hasNextStage()){
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
        return getBrain().getMemory(MKMemoryModuleTypes.THREAT_MAP).map(x -> x.containsKey(target)).orElse(false);
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity target) {
        super.setLastHurtByMob(target);
        if (target != null){
            addThreat(target, NpcConstants.INITIAL_THREAT, true);
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (hand.equals(InteractionHand.MAIN_HAND) && getCapability(FactionCapabilities.MOB_FACTION_CAPABILITY)
                .map((cap) -> cap.getRelationToEntity(player) != Targeting.TargetRelation.ENEMY).orElse(false)){
            if (!player.level.isClientSide() && player instanceof ServerPlayer){
                if (player.isShiftKeyDown()){
                    player.openMenu(entityTradeContainer);
                } else {
                    getCapability(ChatCapabilities.NPC_DIALOGUE_CAPABILITY).ifPresent(cap ->
                            cap.startDialogue((ServerPlayer) player, false));
                }

            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getHighestThreat() {
        return getBrain().getMemory(MKMemoryModuleTypes.THREAT_MAP).map(x -> {
            List<ThreatMapEntry> sorted = x.values().stream()
                    .sorted(Comparator.comparingDouble(ThreatMapEntry::getCurrentThreat))
                    .collect(Collectors.toList());
            if (sorted.size() == 0) {
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
                        MKMemoryModuleTypes.ALLIES,
                        MKMemoryModuleTypes.ENEMIES,
                        MKMemoryModuleTypes.THREAT_LIST,
                        MKMemoryModuleTypes.THREAT_MAP,
                        MKMemoryModuleTypes.VISIBLE_ENEMIES,
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.PATH,
                        MKMemoryModuleTypes.MOVEMENT_STRATEGY,
                        MKMemoryModuleTypes.MOVEMENT_TARGET,
                        MKMemoryModuleTypes.CURRENT_ABILITY,
                        MKAbilityMemories.ABILITY_TARGET.get(),
                        MKMemoryModuleTypes.SPAWN_POINT,
                        MKMemoryModuleTypes.IS_RETURNING,
                        MKMemoryModuleTypes.ABILITY_TIMEOUT,
                        MKAbilityMemories.ABILITY_POSITION_TARGET.get()
                ),
                ImmutableList.of(
                        MKSensorTypes.ENTITIES_SENSOR,
                        MKSensorTypes.THREAT_SENSOR,
                        MKSensorTypes.DESTINATION_SENSOR,
                        MKSensorTypes.ABILITY_SENSOR
                ));
    }

    @Override
    public ItemStack getProjectile(ItemStack shootable) {
        if (shootable.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem)shootable.getItem()).getSupportedHeldProjectiles();
            ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
            return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

}
