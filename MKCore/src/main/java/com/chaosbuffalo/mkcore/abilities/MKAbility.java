package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.AbilityUseCondition;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.StandardUseCondition;
import com.chaosbuffalo.mkcore.core.*;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mkcore.utils.text.IconTextComponent;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class MKAbility extends ForgeRegistryEntry<MKAbility> implements ISerializableAttributeContainer {

    private int castTime;
    private int cooldown;
    private float manaCost;
    private final List<ISerializableAttribute<?>> attributes;
    private AbilityUseCondition useCondition;
    private final Set<Attribute> skillAttributes;
    private static final ResourceLocation EMPTY_PARTICLES = new ResourceLocation(MKCore.MOD_ID, "fx.casting.empty");
    protected final ResourceLocationAttribute casting_particles = new ResourceLocationAttribute("casting_particles", EMPTY_PARTICLES);
    public static final ResourceLocation POOL_SLOT_ICON = new ResourceLocation(MKCore.MOD_ID, "textures/talents/pool_count_icon_filled.png");
    public static final NumberFormat PERCENT_FORMATTER = NumberFormat.getPercentInstance();
    public static final NumberFormat INTEGER_FORMATTER = NumberFormat.getIntegerInstance();
    public static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance();


    public MKAbility() {
        this.cooldown = GameConstants.TICKS_PER_SECOND;
        this.castTime = 0;
        this.manaCost = 1;
        this.attributes = new ArrayList<>();
        this.skillAttributes = new HashSet<>();
        setUseCondition(new StandardUseCondition(this));
        addAttribute(casting_particles);
    }

    public boolean hasCastingParticles() {
        return casting_particles.getValue().compareTo(EMPTY_PARTICLES) != 0;
    }

    public ResourceLocation getCastingParticles() {
        return casting_particles.getValue();
    }

    public Component getDamageDescription(IMKEntityData casterData, MKDamageType damageType, float damage,
                                          float scale, float level, float modifierScaling) {
        float bonus = casterData.getStats().getDamageTypeBonus(damageType) * modifierScaling;
        float abilityDamage = damage + (scale * level) + bonus;
        MutableComponent damageStr = TextComponent.EMPTY.copy();
        damageStr.append(new TextComponent(NUMBER_FORMATTER.format(abilityDamage)).withStyle(ChatFormatting.BOLD));
        if (bonus != 0) {
            damageStr.append(new TextComponent(String.format(" (+%s)", NUMBER_FORMATTER.format(bonus))).withStyle(ChatFormatting.BOLD));
        }
        damageStr.append(" ").append(damageType.getDisplayName().withStyle(damageType.getFormatting()));
        return damageStr;
    }


    protected MutableComponent formatEffectValue(float damage, float levelScale, float level, float bonus, float scaleMod) {
        float value = damage + (levelScale * level) + (bonus * scaleMod);
        MutableComponent damageStr = TextComponent.EMPTY.copy();
        damageStr.append(new TextComponent(NUMBER_FORMATTER.format(value)).withStyle(ChatFormatting.BOLD));
        if (bonus != 0) {
            damageStr.append(new TextComponent(String.format(" (+%s)", NUMBER_FORMATTER.format(bonus))).withStyle(ChatFormatting.BOLD));
        }
        return damageStr;
    }

    public Component getHealDescription(IMKEntityData casterData, float value,
                                        float scale, float level, float modifierScaling) {
        float bonus = casterData.getStats().getHealBonus();
        return formatEffectValue(value, scale, level, bonus, modifierScaling).withStyle(ChatFormatting.GREEN);
    }

    protected Component formatManaValue(IMKEntityData casterData, float value, float scale, float level,
                                        float bonus, float modifierScaling) {
        return formatEffectValue(value, scale, level, bonus, modifierScaling).withStyle(ChatFormatting.BLUE);
    }

    protected Component getSkillDescription(IMKEntityData casterData) {
        Component skillList = ComponentUtils.formatList(getSkillAttributes(),
                attr -> new TranslatableComponent(attr.getDescriptionId()));
        return new TranslatableComponent("mkcore.ability.description.skill", skillList);
    }

    public void buildDescription(IMKEntityData casterData, Consumer<Component> consumer) {
        if (casterData instanceof MKPlayerData) {
            MKPlayerData playerData = (MKPlayerData) casterData;
            MKAbilityInfo info = playerData.getAbilities().getKnownAbility(getAbilityId());
            if (info != null && info.usesAbilityPool()) {
                consumer.accept(new IconTextComponent(POOL_SLOT_ICON, "mkcore.ability.description.uses_pool").withStyle(ChatFormatting.ITALIC));
            }
        }
        if (!skillAttributes.isEmpty()) {
            consumer.accept(getSkillDescription(casterData));
        }
        consumer.accept(getManaCostDescription(casterData));
        consumer.accept(getCooldownDescription(casterData));
        consumer.accept(getCastTimeDescription(casterData));
        getTargetSelector().buildDescription(this, casterData, consumer);
        consumer.accept(getAbilityDescription(casterData));
    }

    protected Component getCooldownDescription(IMKEntityData casterData) {
        float seconds = (float) casterData.getStats().getAbilityCooldown(this) / GameConstants.TICKS_PER_SECOND;
        return new TranslatableComponent("mkcore.ability.description.cooldown", seconds);
    }

    protected Component getCastTimeDescription(IMKEntityData casterData) {
        int castTicks = casterData.getStats().getAbilityCastTime(this);
        float seconds = (float) castTicks / GameConstants.TICKS_PER_SECOND;
        Component time = castTicks > 0 ?
                new TranslatableComponent("mkcore.ability.description.seconds", seconds) :
                new TranslatableComponent("mkcore.ability.description.instant");
        return new TranslatableComponent("mkcore.ability.description.cast_time", time);
    }

    protected Component getManaCostDescription(IMKEntityData casterData) {
        return new TranslatableComponent("mkcore.ability.description.mana_cost", getManaCost(casterData));
    }

    protected Component getAbilityDescription(IMKEntityData casterData) {
        return new TranslatableComponent(getDescriptionTranslationKey());
    }

    public void setUseCondition(AbilityUseCondition useCondition) {
        this.useCondition = useCondition;
    }

    public AbilityUseCondition getUseCondition() {
        return useCondition;
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> attr) {
        attributes.add(attr);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... attrs) {
        attributes.addAll(Arrays.asList(attrs));
    }

    public ResourceLocation getAbilityId() {
        return MKCoreRegistry.ABILITIES.getKey(this);
    }

    @Nonnull
    public MKAbilityInfo createAbilityInfo() {
        return new MKAbilityInfo(this);
    }

    public Component getAbilityName() {
        return new TranslatableComponent(getTranslationKey());
    }

    protected String getTranslationKey() {
        ResourceLocation abilityId = getAbilityId();
        return String.format("%s.%s.name", abilityId.getNamespace(), abilityId.getPath());
    }

    protected String getDescriptionTranslationKey() {
        ResourceLocation abilityId = getAbilityId();
        return String.format("%s.%s.description", abilityId.getNamespace(), abilityId.getPath());
    }

    public ResourceLocation getAbilityIcon() {
        ResourceLocation abilityId = getAbilityId();
        return new ResourceLocation(abilityId.getNamespace(), String.format("textures/abilities/%s.png", abilityId.getPath().split(Pattern.quote("."))[1]));
    }

    public AbilityRenderer getRenderer() {
        return AbilityRenderer.INSTANCE;
    }

    protected int getBaseCastTime() {
        return castTime;
    }

    public int getCastTime(IMKEntityData casterData) {
        return getBaseCastTime();
    }

    protected void setCastTime(int castTicks) {
        castTime = castTicks;
    }

    public boolean canApplyCastingSpeedModifier() {
        return true;
    }

    public float getDistance(LivingEntity entity) {
        return 1.0f;
    }

    protected float getMeleeReach(LivingEntity entity) {
        return (float) entity.getAttribute(MKAttributes.ATTACK_REACH).getValue();
    }

    protected void setCooldownTicks(int ticks) {
        this.cooldown = ticks;
    }

    protected void setCooldownSeconds(int seconds) {
        this.cooldown = seconds * GameConstants.TICKS_PER_SECOND;
    }

    protected int getBaseCooldown() {
        return cooldown;
    }

    public int getCooldown(IMKEntityData casterData) {
        return getBaseCooldown();
    }

    public AbilityType getType() {
        return AbilityType.Basic;
    }

    public abstract TargetingContext getTargetContext();

    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        return Targeting.isValidTarget(getTargetContext(), caster, target);
    }

    protected float getBaseManaCost() {
        return manaCost;
    }

    public float getManaCost(IMKEntityData casterData) {
        return getBaseManaCost() + getManaCostModifierForSkills(casterData);
    }

    protected float getManaCostModifierForSkills(IMKEntityData casterData) {
        float total = 0.0f;
        for (Attribute attribute : getSkillAttributes()) {
            total += getSkillLevel(casterData.getEntity(), attribute);
        }
        return total;
    }

    protected void setManaCost(float cost) {
        manaCost = cost;
    }

    public boolean meetsCastingRequirements(IMKEntityData casterData, MKAbilityInfo info) {
        return casterData.getAbilityExecutor().canActivateAbility(this) &&
                casterData.getStats().canActivateAbility(this);
    }

    public <T> T serializeDynamic(DynamicOps<T> ops) {
        return ops.createMap(
                ImmutableMap.of(
                        ops.createString("cooldown"), ops.createInt(getBaseCooldown()),
                        ops.createString("manaCost"), ops.createFloat(getBaseManaCost()),
                        ops.createString("castTime"), ops.createInt(getBaseCastTime()),
                        ops.createString("attributes"), serializeAttributeMap(ops)
                )
        );
    }

    public <T> void deserializeDynamic(Dynamic<T> dynamic) {
        MKCore.LOGGER.debug("ability deserialize {}", dynamic.getValue());
        setCooldownTicks(dynamic.get("cooldown").asInt(getBaseCooldown()));
        setManaCost(dynamic.get("manaCost").asFloat(getBaseManaCost()));
        setCastTime(dynamic.get("castTime").asInt(getBaseCastTime()));
        deserializeAttributeMap(dynamic, "attributes");
    }

    public static float convertDurationToSeconds(int dur) {
        return dur / GameConstants.FTICKS_PER_SECOND;
    }

    @Nullable
    public SoundEvent getCastingSoundEvent() {
        return CoreSounds.casting_default.get();
    }

    @Nullable
    public SoundEvent getSpellCompleteSoundEvent() {
        return CoreSounds.spell_cast_default.get();
    }

    public void executeWithContext(IMKEntityData casterData, AbilityContext context, MKAbilityInfo abilityInfo) {
        casterData.getAbilityExecutor().startAbility(context, abilityInfo);
    }

    public Component getTargetContextLocalization() {
        return new TranslatableComponent("mkcore.ability_description.target_type",
                getTargetContext().getLocalizedDescription());
    }

    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.NONE;
    }

    public Set<MemoryModuleType<?>> getRequiredMemories() {
        return getTargetSelector().getRequiredMemories();
    }

    public boolean isExecutableContext(AbilityContext context) {
        return getRequiredMemories().stream().allMatch(context::hasMemory);
    }

    public void continueCast(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft, AbilityContext context) {
    }

    public void continueCastClient(LivingEntity castingEntity, IMKEntityData casterData, int castTimeLeft) {
    }

    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
    }

    public boolean isInterruptedBy(IMKEntityData targetData, CastInterruptReason reason) {
        return true;
    }

    @Deprecated
    public List<LivingEntity> getTargetsInLine(LivingEntity caster, Vec3 from, Vec3 to, boolean checkValid, float growth) {
        return TargetUtil.getTargetsInLine(caster, from, to, growth, checkValid ? this::isValidTarget : null);
    }

    protected void shootProjectile(BaseProjectileEntity projectileEntity, float velocity, float accuracy,
                                   LivingEntity entity, AbilityContext context) {
        Vec3 startPos = entity.position().add(new Vec3(0, entity.getEyeHeight(), 0));
        startPos.add(Vec3.directionFromRotation(entity.getRotationVector()).multiply(.5, 0.0, .5));
        projectileEntity.setPos(startPos.x, startPos.y, startPos.z);
        if (entity instanceof Player) {
            projectileEntity.shoot(entity, entity.getXRot(), entity.getYRot(), 0, velocity, accuracy);
        } else {
            context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(targetEntity ->
                    EntityUtils.shootProjectileAtTarget(projectileEntity, targetEntity, velocity, accuracy));
        }
    }

    public static float getSkillLevel(LivingEntity castingEntity, Attribute skillAttribute) {
        if (skillAttribute == null) {
            return 0.0f;
        }
        AttributeInstance skill = castingEntity.getAttribute(skillAttribute);
        return skill != null ? (float) (skill.getValue() / GameConstants.SKILL_POINTS_PER_LEVEL) : 0.0f;
    }

    public static double convertSkillToMultiplier(double value) {
        return value / 20.0;
    }

    protected MKAbility addSkillAttribute(Attribute attribute) {
        this.skillAttributes.add(attribute);
        return this;
    }

    public Set<Attribute> getSkillAttributes() {
        return Collections.unmodifiableSet(skillAttributes);
    }

    protected int getBuffDuration(IMKEntityData casterData, float level, int base, int scale) {
        int duration = Math.round((base + scale * level) * GameConstants.TICKS_PER_SECOND);
        return MKCombatFormulas.applyBuffDurationModifier(casterData, duration);
    }
}
