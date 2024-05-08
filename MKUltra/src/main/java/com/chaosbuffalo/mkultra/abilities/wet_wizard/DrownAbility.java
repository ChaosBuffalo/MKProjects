package com.chaosbuffalo.mkultra.abilities.wet_wizard;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.DrownEffect;
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

public class DrownAbility extends ProjectileAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "drown_casting");
    public static final ResourceLocation TICK_PARTICLES = new ResourceLocation(MKUltra.MODID, "drown_effect");
    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "drown_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "drown_detonate");
    protected final IntAttribute baseDuration = new IntAttribute("baseDuration", 10);
    protected final IntAttribute scaleDuration = new IntAttribute("scaleDuration", 2);
    protected final ResourceLocationAttribute tick_particles = new ResourceLocationAttribute("tick_particles", TICK_PARTICLES);


    public DrownAbility() {
        super(MKAttributes.CONJURATION);
        setCooldownSeconds(10);
        setManaCost(5);
        setCastTime(GameConstants.TICKS_PER_SECOND);
        addAttributes(baseDuration, scaleDuration, tick_particles);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
        projectileSpeed.setDefaultValue(0.9f);
        baseDamage.setDefaultValue(4.0f);
        scaleDamage.setDefaultValue(2.0f);
        modifierScaling.setDefaultValue(0.2f);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(skill);
        Component dotStr = getDamageDescription(entityData,
                CoreDamageTypes.NatureDamage.get(), baseDamage.value(), scaleDamage.value(), level, modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(),
                dotStr, NUMBER_FORMATTER.format(convertDurationToSeconds(DrownEffect.DEFAULT_PERIOD)),
                NUMBER_FORMATTER.format(convertDurationToSeconds(getBuffDuration(entityData, level,
                        baseDuration.value(), scaleDuration.value()))));
    }

    protected MKEffectBuilder<?> getDotEffect(IMKEntityData casterData, float level) {
        int durTicks = getBuffDuration(casterData, level, baseDuration.value(), scaleDuration.value());
        return DrownEffect.from(casterData.getEntity(), baseDamage.value(), scaleDamage.value(),
                        modifierScaling.value(), tick_particles.getValue())
                .ability(this)
                .skillLevel(level)
                .timed(durTicks);
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_water_5.get(), cat);
        MKParticles.spawn(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        if (result.getType().equals(HitResult.Type.ENTITY)) {
            EntityHitResult entityTrace = (EntityHitResult) result;
            MKCore.getEntityData(caster).ifPresent(casterData -> {
                MKEffectBuilder<?> damage = getDotEffect(casterData, getSkillLevel(caster, skill));
                MKCore.getEntityData(entityTrace.getEntity()).ifPresent(x -> {
                    x.getEffects().addEffect(damage);
                });
            });
        }
        return true;
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level);
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.drownProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 3);
        return projectile;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_water.get();
    }

    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_water_7.get();
    }
}
