package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MKDamageSource extends DamageSource {
    protected final MKDamageType damageType;
    protected float modifierScaling = 1.0f;
    protected AbilityFormula damageBonusFormula = createLegacyDamageBonusFormula(1.0f);
    @Nullable
    protected InteractionHand attackHand;

    public enum Origination {
        MK_ABILITY,
        DAMAGE_TYPE
    }

    public abstract Origination getOrigination();

    private MKDamageSource(Level level, MKDamageType damageType,
                           @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CoreDamageTypes.MK_DAMAGE),
                directEntity, causingEntity);
        this.damageType = damageType;
    }

    @Override
    public boolean scalesWithDifficulty() {
        // We apply our own scaling
        return false;
    }

    public static class EffectDamage extends MKDamageSource {

        @Nullable
        protected final String damageTypeName;

        private EffectDamage(Level level, MKDamageType damageType, @Nullable Entity directEntity,
                             @Nullable Entity causingEntity, @Nullable String damageTypeName) {
            super(level, damageType, directEntity, causingEntity);
            this.damageTypeName = damageTypeName;
        }

        @Nullable
        public String getDamageTypeName() {
            return damageTypeName;
        }

        @Override
        public Origination getOrigination() {
            return Origination.DAMAGE_TYPE;
        }

        @Nonnull
        @Override
        public Component getLocalizedDeathMessage(LivingEntity killedEntity) {
            // FIXME: better message
            MutableComponent comp = Component.translatable("%s got dropped", killedEntity.getDisplayName());
            if (getEntity() != null) {
                comp.append(" by ").append(getEntity().getDisplayName());
            } else {
                comp.append(" anonymously");
            }
            if (damageType != null || damageTypeName != null) {
                comp.append(" with some major ");
                if (damageTypeName != null) {
                    comp.append(Component.translatable(damageTypeName));
                } else {
                    comp.append(damageType.getDisplayName());
                }
            }
            return comp;
        }
    }

    public static class AbilityDamage extends MKDamageSource {
        @Nullable
        private final ResourceLocation abilityId;

        private AbilityDamage(Level level, MKDamageType damageType,
                              @Nullable Entity directEntity,
                              @Nullable Entity causingEntity,
                              @Nullable ResourceLocation abilityId) {
            super(level, damageType, directEntity, causingEntity);
            this.abilityId = abilityId;
        }

        @Nullable
        public ResourceLocation getAbilityId() {
            return abilityId;
        }

        @Override
        public Origination getOrigination() {
            return Origination.MK_ABILITY;
        }

        @Nonnull
        @Override
        public Component getLocalizedDeathMessage(LivingEntity killedEntity) {
            // FIXME: better message
            MutableComponent comp = Component.translatable("%s got dropped", killedEntity.getDisplayName());
            if (getEntity() != null) {
                comp.append(" by ").append(getEntity().getDisplayName());
            } else {
                comp.append(" anonymously");
            }
            if (abilityId != null) {
                MKAbility ability = MKCoreRegistry.getAbility(abilityId);
                if (ability != null) {
                    comp.append(" by ability ").append(ability.getAbilityName());
                }
            }
            if (damageType != null) {
                comp.append(" with some major ").append(damageType.getDisplayName());
            }
            return comp;
        }
    }

    public float getModifierScaling() {
        return modifierScaling;
    }

    public MKDamageSource setModifierScaling(float value) {
        modifierScaling = value;
        damageBonusFormula = createLegacyDamageBonusFormula(value);
        return this;
    }

    public AbilityFormula getDamageBonusFormula() {
        return damageBonusFormula;
    }

    public MKDamageSource setDamageBonusFormula(AbilityFormula damageBonusFormula) {
        this.damageBonusFormula = damageBonusFormula;
        return this;
    }

    public MKDamageType getMKDamageType() {
        return damageType;
    }

    @Nullable
    public InteractionHand getAttackHand() {
        return attackHand;
    }

    public MKDamageSource setAttackHand(@Nullable InteractionHand hand) {
        attackHand = hand;
        return this;
    }

    public boolean isMeleeDamage() {
        return damageType.equals(CoreDamageTypes.MeleeDamage.get());
    }

    public static AbilityFormula createLegacyDamageBonusFormula(float modifierScaling) {
        return AbilityFormula.multiply(
                AbilityFormula.constant(modifierScaling),
                AbilityFormula.context(FormulaContextKey.DAMAGE_BONUS)
        );
    }

    public static MKDamageSource causeAbilityDamage(Level level, MKDamageType damageType,
                                                    ResourceLocation abilityId,
                                                    @Nullable Entity immediateSource,
                                                    @Nullable Entity trueSource) {
        if (damageType.equals(CoreDamageTypes.MeleeDamage.get())) {
            return causeMeleeDamage(level, abilityId, immediateSource, trueSource);
        }
        return new AbilityDamage(level, damageType, immediateSource, trueSource, abilityId);
    }

    public static MKDamageSource causeAbilityDamage(Level level, MKDamageType damageType,
                                                    ResourceLocation abilityId,
                                                    @Nullable Entity immediateSource,
                                                    @Nullable Entity trueSource,
                                                    float modifierScaling) {
        return causeAbilityDamage(level, damageType, abilityId, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }

    public static MKDamageSource causeEffectDamage(Level level, MKDamageType damageType, String effectType,
                                                   @Nullable Entity immediateSource,
                                                   @Nullable Entity trueSource) {
        return new EffectDamage(level, damageType, immediateSource, trueSource, effectType);
    }

    public static MKDamageSource causeEffectDamage(Level level, MKDamageType damageType, String effectType,
                                                   @Nullable Entity immediateSource,
                                                   @Nullable Entity trueSource,
                                                   float modifierScaling) {
        return causeEffectDamage(level, damageType, effectType, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }


    public static MKDamageSource causeMeleeDamage(Level level, ResourceLocation abilityId,
                                                  @Nullable Entity immediateSource,
                                                  @Nullable Entity trueSource) {
        return new AbilityDamage(level, CoreDamageTypes.MeleeDamage.get(), immediateSource, trueSource, abilityId);
    }

    public static MKDamageSource causeMeleeDamage(Level level, ResourceLocation abilityId,
                                                  @Nullable Entity immediateSource,
                                                  @Nullable Entity trueSource,
                                                  @Nullable InteractionHand attackHand) {
        return causeMeleeDamage(level, abilityId, immediateSource, trueSource).setAttackHand(attackHand);
    }

    public static MKDamageSource causeMeleeDamage(Level level, ResourceLocation abilityId,
                                                  @Nullable Entity immediateSource,
                                                  @Nullable Entity trueSource,
                                                  float modifierScaling) {
        return causeMeleeDamage(level, abilityId, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }

    public static MKDamageSource causeMeleeDamage(Level level, ResourceLocation abilityId,
                                                  @Nullable Entity immediateSource,
                                                  @Nullable Entity trueSource,
                                                  float modifierScaling,
                                                  @Nullable InteractionHand attackHand) {
        return causeMeleeDamage(level, abilityId, immediateSource, trueSource, attackHand)
                .setModifierScaling(modifierScaling);
    }
}
