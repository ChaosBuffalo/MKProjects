package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public class SkinLikeWoodEffect extends MKEffect {
    public final UUID MODIFIER_ID = UUID.fromString("60f31ee6-4a8e-4c35-8746-6c5950187e77");

    public SkinLikeWoodEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(Attributes.ARMOR, MODIFIER_ID, 4, 1, AttributeModifier.Operation.ADDITION,
                MKAttributes.ABJURATION);
        SpellTriggers.ENTITY_HURT.registerPreScale(this::onEntityHurt);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }

    private void onEntityHurt(LivingHurtEvent event, DamageSource source, IMKEntityData targetData) {
        if (targetData.getEffects().isEffectActive(this)) {
            if (targetData instanceof MKPlayerData playerData) {
                if (!playerData.getStats().consumeMana(1)) {
                    targetData.getEffects().removeEffect(this);
                }
            }
        }
    }
}

