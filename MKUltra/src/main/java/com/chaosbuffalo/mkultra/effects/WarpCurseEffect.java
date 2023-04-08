package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.ScalingValueEffectState;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WarpCurseEffect extends MKEffect {
    public static final int DEFAULT_PERIOD = 40;

    public static MKEffectBuilder<?> from(LivingEntity source, float base, float scaling, float modifier, ResourceLocation castParticles) {
        return MKUEffects.WARP_CURSE.get().builder(source).state(s -> {
                    s.setEffectParticles(castParticles);
                    s.setScalingParameters(base, scaling, modifier);
                })
                .periodic(DEFAULT_PERIOD);
    }

    public WarpCurseEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends ScalingValueEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            LivingEntity target = targetData.getEntity();

            float damage = getScaledValue(activeEffect.getStackCount(), activeEffect.getSkillLevel());
            target.hurt(MKDamageSource.causeAbilityDamage(targetData.getEntity().getLevel(), CoreDamageTypes.ShadowDamage.get(),
                    activeEffect.getAbilityId(), activeEffect.getDirectEntity(), activeEffect.getSourceEntity(),
                    getModifierScale()), damage);

            SoundUtils.serverPlaySoundAtEntity(target, MKUSounds.spell_fire_5.get(), target.getSoundSource());
            boolean hasTeleported = false;
            int attempts = 5;
            while (!hasTeleported && attempts > 0) {
                Vec3 targetOrigin = target.position();
                double nextX = targetOrigin.x + (target.getRandom().nextInt(8) - target.getRandom().nextInt(8));
                double nextY = targetOrigin.y + 1.0;
                double nextZ = targetOrigin.z + (target.getRandom().nextInt(8) - target.getRandom().nextInt(8));
                hasTeleported = EntityUtils.safeTeleportEntity(target, new Vec3(nextX, nextY, nextZ));
                attempts--;
            }
            targetData.getAbilityExecutor().interruptCast(CastInterruptReason.Teleport);
            sendEffectParticles(target);
            return true;
        }
    }
}
