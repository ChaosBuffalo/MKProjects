package com.chaosbuffalo.mkultra.entities.projectiles;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.effects.AreaEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.green_knight.SpiritBombAbility;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.Targeting;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Created by Jacob on 7/28/2018.
 */
public class SpiritBombProjectileEntity extends SpriteTrailProjectileEntity {

    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "spirit_bomb_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "spirit_bomb_detonate");


    public SpiritBombProjectileEntity(EntityType<? extends Projectile> entityTypeIn,
                                      Level worldIn) {
        super(entityTypeIn, worldIn, new ItemStack(MKUItems.spiritBombProjectileItem.get()));
        setDeathTime(60);
        setAirProcTime(GameConstants.TICKS_PER_SECOND);
        setDoAirProc(true);
        setDoGroundProc(true);
        setGroundProcTime(GameConstants.TICKS_PER_SECOND);
        setTrailAnimation(ParticleAnimationManager.ANIMATIONS.get(TRAIL_PARTICLES));
    }

    @Override
    protected TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    protected boolean onImpact(Entity caster, HitResult result, int amplifier) {
        if (!this.level.isClientSide && caster != null) {
            SoundSource cat = caster.getSoundSource();
            SoundUtils.serverPlaySoundAtEntity(this, MKUSounds.spell_thunder_3.get(), cat);
            switch (result.getType()) {
                case BLOCK:
                    break;
                case ENTITY:
                    EntityHitResult entityTrace = (EntityHitResult) result;
                    if (entityTrace.getEntity() instanceof LivingEntity target) {
                        if (Targeting.isValidTarget(getTargetContext(), caster, target)) {
                            this.setDeltaMovement(0.0, 0.0, 0.0);
                        }
                    }
                    break;
                case MISS:
                    break;
            }
        }
        return false;
    }


    @Override
    public float getGravityVelocity() {
        return 0.00F;
    }

    @Override
    protected boolean onAirProc(Entity caster, int amplifier) {
        return doEffect(caster, amplifier);
    }

    private boolean doEffect(Entity caster, int amplifier) {
        if (!this.level.isClientSide && caster instanceof LivingEntity casterLiving) {
            SpiritBombAbility ability = MKUAbilities.SPIRIT_BOMB.get();
            MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(casterLiving, CoreDamageTypes.NatureDamage.get(),
                            ability.getBaseDamage(),
                            ability.getScaleDamage(),
                            ability.getModifierScaling())
                    .ability(ability)
                    .directEntity(this)
                    .skillLevel(getSkillLevel())
                    .amplify(amplifier);

            AreaEffectBuilder.createOnEntity(casterLiving, this)
                    .effect(damage, getTargetContext())
                    .instant()
                    .color(65535)
                    .radius(4.0f, true)
                    .disableParticle()
                    .spawn();
            SoundSource cat = caster.getSoundSource();
            SoundUtils.serverPlaySoundAtEntity(this, MKUSounds.spell_magic_explosion.get(), cat);
            MKParticles.spawn(this, new Vec3(0.0, 0.0, 0.0), DETONATE_PARTICLES);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onGroundProc(Entity caster, int amplifier) {
        return doEffect(caster, amplifier);
    }
}
