package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class SoulDrainEffect extends MKEffect {

    public SoulDrainEffect() {
        super(MobEffectCategory.BENEFICIAL);
        SpellTriggers.LIVING_KILL_ENTITY.register(this, this::onLivingKillEntity);
    }

    public void onLivingKillEntity(LivingDeathEvent event, DamageSource source, IMKEntityData data) {
        SoundUtils.serverPlaySoundAtEntity(data.getEntity(), MKUSounds.spell_dark_4.get(), data.getEntity().getSoundSource());
        float mana = MKUAbilities.SOUL_DRAIN.get().getDrainValue((attr) -> MKAbility.getSkillLevel(data.getEntity(), attr));
        data.getStats().addMana(mana);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
