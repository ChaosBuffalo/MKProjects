package com.chaosbuffalo.mkultra.abilities.green_knight;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.*;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.CureEffect;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class ExplosiveGrowthAbility extends MKAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "explosive_growth_casting");
    public static final ResourceLocation CAST_PARTICLES = new ResourceLocation(MKUltra.MODID, "explosive_growth_cast");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "explosive_growth_detonate");
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 10.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 5.0f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);
    protected final ResourceLocationAttribute cast_particles = new ResourceLocationAttribute("cast_particles", CAST_PARTICLES);
    protected final ResourceLocationAttribute detonate_particles = new ResourceLocationAttribute("detonate_particles", DETONATE_PARTICLES);

    public ExplosiveGrowthAbility() {
        super();
        setCooldownSeconds(35);
        setManaCost(6);
        setCastTime(GameConstants.TICKS_PER_SECOND / 4);
        addAttributes(baseDamage, scaleDamage, cast_particles, detonate_particles);
        addSkillAttribute(MKAttributes.RESTORATION);
        addSkillAttribute(MKAttributes.PANKRATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Ultimate;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ALL;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.LINE;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData casterData, MKAbilityInfo abilityInfo) {
        Component damageStr = getDamageDescription(casterData, CoreDamageTypes.MeleeDamage.get(), baseDamage.value(),
                scaleDamage.value(),
                abilityInfo.getSkillValue(casterData, MKAttributes.PANKRATION),
                modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 8.0f;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_shadow.get();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_earth_8.get();
    }

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context, MKAbilityInfo abilityInfo) {
        super.endCast(castingEntity, casterData, context, abilityInfo);
        float restoLevel = abilityInfo.getSkillValue(casterData, MKAttributes.RESTORATION);
        float pankrationLevel = abilityInfo.getSkillValue(casterData, MKAttributes.PANKRATION);

        SoundSource cat = castingEntity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        float damage = baseDamage.value() + scaleDamage.value() * pankrationLevel;

        MKEffectBuilder<?> cure = CureEffect.from(castingEntity)
                .ability(this)
                .skillLevel(restoLevel);
        MKEffectBuilder<?> remedy = MKUAbilities.NATURES_REMEDY.get().createNaturesRemedyEffect(casterData, restoLevel)
                .ability(this);

        Vec3 look = castingEntity.getLookAngle().scale(getDistance(castingEntity, abilityInfo));
        Vec3 from = castingEntity.position().add(0, castingEntity.getEyeHeight(), 0);
        Vec3 to = from.add(look);
        List<LivingEntity> entityHit = TargetUtil.getTargetsInLine(castingEntity, from, to, 1.0f, this::isValidTarget);

        for (LivingEntity entHit : entityHit) {
            Targeting.TargetRelation relation = Targeting.getTargetRelation(castingEntity, entHit);
            switch (relation) {
                case FRIEND: {
                    MKCore.getEntityData(entHit).ifPresent(targetData -> {
                        targetData.getEffects().addEffect(cure);
                        targetData.getEffects().addEffect(remedy);
                    });

                    SoundUtils.serverPlaySoundAtEntity(entHit, MKUSounds.spell_earth_6.get(), cat);
                    break;
                }
                case ENEMY: {
                    entHit.hurt(MKDamageSource.causeMeleeDamage(castingEntity.getLevel(), getAbilityId(), castingEntity, castingEntity), damage);
                    SoundUtils.serverPlaySoundAtEntity(entHit, MKUSounds.spell_earth_1.get(), cat);
                    break;
                }
            }

            MKParticles.spawn(entHit, new Vec3(0.0, 1.0, 0.0), detonate_particles.getValue());
        }

        HitResult blockHit = RayTraceUtils.rayTraceBlocks(castingEntity, from, to, false);
        if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
            to = blockHit.getLocation();
        }

        casterData.getEffects().addEffect(cure);
        casterData.getEffects().addEffect(remedy);
        castingEntity.teleportTo(to.x, to.y, to.z);
        Vec3 pos = to;
        MKParticles.spawn(castingEntity, from, CAST_PARTICLES, spawn -> {
            spawn.addLoc(pos);
        });
    }
}
