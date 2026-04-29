package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.healing.MKHealSource;
import com.chaosbuffalo.mkcore.core.healing.MKHealing;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class LifeSiphonEffect extends MKEffect {

    public LifeSiphonEffect() {
        super(MobEffectCategory.BENEFICIAL);
        SpellTriggers.LIVING_KILL_ENTITY.register(this, this::onLivingKillEntity);
    }

    public void onLivingKillEntity(LivingDeathEvent event, DamageSource source, IMKEntityData killerData) {
        LivingEntity living = killerData.getEntity();
        SoundUtils.serverPlaySoundAtEntity(living, MKUSounds.spell_dark_5.value(), living.getSoundSource());
        MKHealSource healSource = MKHealSource.getShadowHeal(MKUAbilities.LIFE_SIPHON.getId(), living, living)
                .setHealBonusFormula(MKUAbilities.LIFE_SIPHON.get().getHealingBonusFormula());
        float amount = MKUAbilities.LIFE_SIPHON.get().getBaseHealingValue(living);
        MKHealing.healEntityFrom(living, amount, healSource);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
