package com.chaosbuffalo.mkcore.core.damage;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MKDamageSource extends DamageSource {
    protected final MKDamageType damageType;
    @Nullable
    protected Entity trueSource;
    @Nullable
    protected Entity immediateSource;
    protected float modifierScaling = 1.0f;
    protected boolean suppressTriggers;

    public enum Origination {
        MK_ABILITY,
        DAMAGE_TYPE
    }

    public abstract Origination getOrigination();

    @Override
    @Nullable
    public Entity getDirectEntity() {
        return immediateSource;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return trueSource;
    }

    private MKDamageSource(Level level, MKDamageType damageType,
                           @Nullable Entity immediateSource, @Nullable Entity trueSource) {
        super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CoreDamageTypes.MK_DAMAGE));
        this.immediateSource = immediateSource;
        this.trueSource = trueSource;
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

        private EffectDamage(Level level, MKDamageType damageType, @Nullable Entity immediateSource, @Nullable Entity trueSource, @Nullable String damageTypeName) {
            super(level, damageType, immediateSource, trueSource);
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
            if (trueSource != null) {
                comp.append(" by ").append(trueSource.getDisplayName());
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
                              @Nullable Entity immediateSource,
                              @Nullable Entity trueSource,
                              @Nullable ResourceLocation abilityId) {
            super(level, damageType, immediateSource, trueSource);
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
            if (trueSource != null) {
                comp.append(" by ").append(trueSource.getDisplayName());
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
        return this;
    }

    public MKDamageType getMKDamageType() {
        return damageType;
    }

    public boolean isMeleeDamage() {
        return damageType.equals(CoreDamageTypes.MeleeDamage.get());
    }

    public boolean shouldSuppressTriggers() {
        return suppressTriggers;
    }

    public MKDamageSource setSuppressTriggers(boolean suppressTriggers) {
        this.suppressTriggers = suppressTriggers;
        return this;
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
                                                  float modifierScaling) {
        return causeMeleeDamage(level, abilityId, immediateSource, trueSource)
                .setModifierScaling(modifierScaling);
    }
}
