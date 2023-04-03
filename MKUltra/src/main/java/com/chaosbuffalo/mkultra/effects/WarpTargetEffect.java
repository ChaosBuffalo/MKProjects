package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WarpTargetEffect extends MKEffect {

    public WarpTargetEffect() {
        super(MobEffectCategory.HARMFUL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return MKUEffects.WARP_TARGET.get().builder(source);
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public static class State extends MKEffectState {

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
            // We definitely need the source for this effect so make an attempt to recover the casting entity
            if (!activeEffect.hasSourceEntity()) {
                activeEffect.recoverState(targetData);
            }
            LivingEntity source = activeEffect.getSourceEntity();
            if (source == null) {
                return false;
            }
            Vec3 playerOrigin = source.position();
            Vec3 heading = source.getLookAngle();
            targetData.getAbilityExecutor().interruptCast(CastInterruptReason.Teleport);
            targetData.getEntity().teleportTo(
                    playerOrigin.x + heading.x,
                    playerOrigin.y + heading.y + 1.0,
                    playerOrigin.z + heading.z);
            return true;
        }
    }
}
