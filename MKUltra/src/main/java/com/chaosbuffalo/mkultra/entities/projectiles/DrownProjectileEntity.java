package com.chaosbuffalo.mkultra.entities.projectiles;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.wet_wizard.DrownAbility;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DrownProjectileEntity extends SpriteTrailProjectileEntity {

    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "drown_trail");
    public static final ResourceLocation DETONATE_PARTICLES = new ResourceLocation(MKUltra.MODID, "drown_detonate");

    private DrownAbility ability;
    private MKAbilityInfo abilityInfo;


    public DrownProjectileEntity(EntityType<? extends Projectile> entityTypeIn,
                                 Level worldIn) {
        super(entityTypeIn, worldIn, new ItemStack(MKUItems.drownProjectileItem.get()));
        setDeathTime(GameConstants.TICKS_PER_SECOND * 3);
        setTrailAnimation(ParticleAnimationManager.ANIMATIONS.get(TRAIL_PARTICLES));
    }

    public void setSourceAbility(DrownAbility ability, MKAbilityInfo abilityInfo) {
        this.ability = ability;
        this.abilityInfo = abilityInfo;
    }

    @Override
    protected boolean onImpact(Entity caster, HitResult result, int amplifier) {
        if (!this.level.isClientSide && caster instanceof LivingEntity) {
            if (ability == null || abilityInfo == null) {
                return false;
            }

            SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            SoundUtils.serverPlaySoundAtEntity(this, MKUSounds.spell_water_5.get(), cat);
            MKParticles.spawn(this, new Vec3(0.0, 0.0, 0.0), DETONATE_PARTICLES);

            if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityTrace = (EntityHitResult) result;
                MKCore.getEntityData(caster).ifPresent(casterData -> {
                    MKEffectBuilder<?> damage = ability.getDotEffect(casterData, abilityInfo, getSkillLevel());
                    MKCore.getEntityData(entityTrace.getEntity()).ifPresent(x -> {
                        x.getEffects().addEffect(damage);
                    });
                });
            }
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

