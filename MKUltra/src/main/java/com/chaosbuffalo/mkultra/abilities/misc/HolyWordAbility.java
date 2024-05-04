package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.HolyWordEffect;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HolyWordAbility extends ProjectileAbility {

    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "holy_word_casting");
    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "holy_word_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "holy_word_detonate");
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 30);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 10);
    protected final IntAttribute baseStunDuration = new IntAttribute("baseStunDuration", 3);
    protected final IntAttribute scaleStunDuration = new IntAttribute("scaleStunDuration", 1);
    protected final FloatAttribute stunModiferScaling = new FloatAttribute("stunModifier", 1.0f);
    protected final IntAttribute stacks = new IntAttribute("stacks", 5);
    public HolyWordAbility() {
        super(MKAttributes.EVOCATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        addAttributes(baseDuration, scaleDuration, baseStunDuration, scaleStunDuration, stunModiferScaling, stacks);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
        projectileSpeed.setDefaultValue(0.8f);
        baseDamage.setDefaultValue(5.0f);
        scaleDamage.setDefaultValue(3.0f);
        setCastTime(GameConstants.TICKS_PER_SECOND * 2);
        setCooldownTicks(GameConstants.TICKS_PER_SECOND * 5);
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_holy_3.get(), cat);
        MKParticles.spawn(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        if (result.getType().equals(HitResult.Type.ENTITY)) {
            EntityHitResult entityTrace = (EntityHitResult) result;

            MKCore.getEntityData(caster).ifPresent(casterData -> {
                float skillLevel = getSkillLevel(caster, skill);
                MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(caster, CoreDamageTypes.HolyDamage.get(),
                                getBaseDamage(),
                                getScaleDamage(),
                                getModifierScaling())
                        .ability(this)
                        .directEntity(projectile)
                        .skillLevel(skillLevel)
                        .amplify(amplifier);

                MKEffectBuilder<?> stunCounter = HolyWordEffect.from(caster,
                                baseStunDuration.value(),
                                scaleStunDuration.value(),
                                stunModiferScaling.value(),
                                stacks.value())
                        .ability(this)
                        .directEntity(projectile)
                        .skillLevel(skillLevel)
                        .amplify(amplifier)
                        .timed(getBuffDuration(casterData, skillLevel, baseDuration.value(), scaleDuration.value()));


                MKCore.getEntityData(entityTrace.getEntity()).ifPresent(x -> {
                    x.getEffects().addEffect(damage);
                    x.getEffects().addEffect(stunCounter);
                });
            });
        }
        return true;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_holy.get();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_holy_2.get();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(skill);
        Component dmg = getDamageDescription(entityData,
                CoreDamageTypes.HolyDamage.get(), baseDamage.value(), scaleDamage.value(), level, modifierScaling.value());
        float duration = convertDurationToSeconds(getBuffDuration(entityData, level,
                baseDuration.value(), scaleDuration.value()));
        return Component.translatable(getDescriptionTranslationKey(),
                dmg,
                MKUEffects.HOLY_WORD_EFFECT.get().getDisplayName(),
                NUMBER_FORMATTER.format(duration),
                stacks.value(),
                NUMBER_FORMATTER.format(convertDurationToSeconds(
                        getBuffDuration(entityData, level, baseStunDuration.value(), scaleStunDuration.value())))
                );
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level);
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.holyWordProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 3);
        return projectile;
    }
}
