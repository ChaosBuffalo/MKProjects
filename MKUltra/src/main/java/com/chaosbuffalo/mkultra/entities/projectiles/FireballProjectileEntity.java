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
import com.chaosbuffalo.mkultra.abilities.misc.FireballAbility;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireballProjectileEntity extends SpriteTrailProjectileEntity {

    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "fireball_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "fireball_detonate");

    public FireballProjectileEntity(EntityType<? extends Projectile> entityTypeIn,
                                    Level worldIn) {
        super(entityTypeIn, worldIn, new ItemStack(MKUItems.fireballProjectileItem.get()));
        setDeathTime(GameConstants.TICKS_PER_SECOND * 5);
        setTrailAnimation(ParticleAnimationManager.ANIMATIONS.get(TRAIL_PARTICLES));
    }


    @Override
    protected boolean onImpact(Entity caster, HitResult result, int amplifier) {
        if (!this.level.isClientSide && caster instanceof LivingEntity casterLiving) {
            SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            SoundUtils.serverPlaySoundAtEntity(this, MKUSounds.spell_fire_4.get(), cat);
            MKParticles.spawn(this, new Vec3(0.0, 0.0, 0.0), DETONATE_PARTICLES);

            FireballAbility ability = MKUAbilities.FIREBALL.get();
            MKEffectBuilder<?> damage = MKAbilityDamageEffect.from(casterLiving, CoreDamageTypes.FireDamage.get(),
                            ability.getBaseDamage(),
                            ability.getScaleDamage(),
                            ability.getModifierScaling())
                    .ability(ability)
                    .directEntity(this)
                    .skillLevel(getSkillLevel())
                    .amplify(amplifier);

            MKEffectBuilder<?> fireBreak = MKUEffects.BREAK_FIRE.get().builder(casterLiving)
                    .ability(ability)
                    .directEntity(this)
                    .timed(Math.round((getSkillLevel() + 1) * GameConstants.TICKS_PER_SECOND))
                    .skillLevel(getSkillLevel())
                    .amplify(amplifier);

            AreaEffectBuilder.createOnEntity(casterLiving, this)
                    .effect(damage, getTargetContext())
                    .effect(fireBreak, getTargetContext())
                    .instant()
                    .color(16737330).radius(ability.getExplosionRadius(), true)
                    .disableParticle()
                    .spawn();
            return true;
        }
        return false;
    }

    @Override
    public float getGravityVelocity() {
        return 0.0f;
    }

    @Override
    protected TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

}
