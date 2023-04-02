package com.chaosbuffalo.mkcore.effects.status;

import com.chaosbuffalo.mkcore.core.CastInterruptReason;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class StunEffect extends MKEffect {
    public static final UUID MODIFIER_ID = UUID.fromString("e27f71ce-26f0-465e-b465-7e5ea711e53c");

    public StunEffect() {
        super(MobEffectCategory.HARMFUL);
        addAttribute(Attributes.MOVEMENT_SPEED, MODIFIER_ID, -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return CoreEffects.STUN.get().builder(source);
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        applyEffect(targetData, newInstance);
    }

    @Override
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        super.onInstanceRemoved(targetData, expiredEffect);
        LivingEntity target = targetData.getEntity();
        if (target instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }

    @Override
    public void onInstanceReady(IMKEntityData targetData, MKActiveEffect activeInstance) {
        super.onInstanceReady(targetData, activeInstance);
        applyEffect(targetData, activeInstance);
    }

    private void applyEffect(IMKEntityData targetData, MKActiveEffect activeEffect) {
        LivingEntity target = targetData.getEntity();
        if (target instanceof Mob mob) {
            mob.setNoAi(true);
        }
        targetData.getAbilityExecutor().interruptCast(CastInterruptReason.Stun);
        SoundUtils.serverPlaySoundAtEntity(target, CoreSounds.stun_sound.get(), target.getSoundSource());
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}

