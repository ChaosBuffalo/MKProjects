package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.KillTriggerContext;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class LifeSiphonEffect extends MKEffect implements MKTriggerContributor {

    public LifeSiphonEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    public void onLivingKillEntity(KillTriggerContext context) {
        LivingEntity living = context.killerData().getEntity();
        SoundUtils.serverPlaySoundAtEntity(living, MKUSounds.spell_dark_5.value(), living.getSoundSource());
        MKHealSource healSource = new MKHealSource(MKUAbilities.LIFE_SIPHON.getId(), living, living,
                CoreDamageTypes.ShadowDamage.get(), MKUAbilities.LIFE_SIPHON.get().getModifierScaling());
        float amount = MKUAbilities.LIFE_SIPHON.get().getHealingValue(living);
        MKHealing.healEntityFrom(living, amount, healSource);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }

    @Override
    public void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar) {
        registrar.add(CoreTriggerTypes.KILL, this::onLivingKillEntity);
    }
}
