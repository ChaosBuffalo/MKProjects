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
import com.chaosbuffalo.mkultra.abilities.misc.ProjectileAbility;
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

public class FireballProjectileEntity extends AbilityProjectileEntity {

    public static final ResourceLocation TRAIL_PARTICLES = new ResourceLocation(MKUltra.MODID, "fireball_trail");


    public FireballProjectileEntity(EntityType<? extends Projectile> entityTypeIn,
                                    Level worldIn) {
        super(entityTypeIn, worldIn);
        setItem(new ItemStack(MKUItems.fireballProjectileItem.get()));
        setAbility(MKUAbilities.FIREBALL);
        setDeathTime(GameConstants.TICKS_PER_SECOND * 5);
        setTrailAnimation(TRAIL_PARTICLES);
    }
}
