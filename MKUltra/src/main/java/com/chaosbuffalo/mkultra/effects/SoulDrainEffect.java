package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class SoulDrainEffect extends MKEffect implements MKTriggerContributor {

    public SoulDrainEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    public void onLivingKillEntity(LivingDeathEvent event, DamageSource source, IMKEntityData data) {
        SoundUtils.serverPlaySoundAtEntity(data.getEntity(), MKUSounds.spell_dark_4.value(), data.getEntity().getSoundSource());
        float mana = MKUAbilities.SOUL_DRAIN.get().getDrainValue((attr) -> MKAbility.getSkillLevel(data.getEntity(), attr));
        data.getStats().addMana(mana);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }

    @Override
    public void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar) {
        registrar.add(CoreTriggerTypes.KILL, context ->
                onLivingKillEntity(context.event(), context.source(), context.killerData()));
    }
}
