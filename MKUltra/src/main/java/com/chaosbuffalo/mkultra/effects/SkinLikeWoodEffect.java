package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.UUID;

public class SkinLikeWoodEffect extends MKEffect implements MKTriggerContributor {
    public final UUID MODIFIER_ID = UUID.fromString("60f31ee6-4a8e-4c35-8746-6c5950187e77");

    public SkinLikeWoodEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(Attributes.ARMOR, MODIFIER_ID, 4, 1, AttributeModifier.Operation.ADD_VALUE,
                MKAttributes.ABJURATION);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }

    private void onEntityHurt(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData targetData) {
        if (targetData.getEffects().isEffectActive(this)) {
            if (targetData instanceof MKPlayerData playerData) {
                if (!playerData.getStats().consumeMana(1)) {
                    targetData.getEffects().removeEffect(this);
                }
            }
        }
    }

    @Override
    public void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar) {
        registrar.add(CoreTriggerTypes.VICTIM_PRE_SCALE, context ->
                onEntityHurt(context.event(), context.source(), context.victimData()));
    }
}
