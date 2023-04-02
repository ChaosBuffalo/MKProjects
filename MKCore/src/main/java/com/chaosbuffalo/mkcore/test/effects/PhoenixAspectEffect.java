package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class PhoenixAspectEffect extends MKEffect {

    public static final UUID MODIFIER_ID = UUID.fromString("721f69b8-c361-4b80-897f-724f84e08ae7");

    public PhoenixAspectEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(MKAttributes.COOLDOWN, MODIFIER_ID, 0.33, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttribute(MKAttributes.MANA_REGEN, MODIFIER_ID, 1.0f, AttributeModifier.Operation.ADDITION);
    }

    public void enableFlying(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void onInstanceAdded(IMKEntityData targetData, MKActiveEffect newInstance) {
        super.onInstanceAdded(targetData, newInstance);
        enableFlying(targetData.getEntity());
    }

    @Override
    public void onInstanceReady(IMKEntityData targetData, MKActiveEffect activeInstance) {
        super.onInstanceReady(targetData, activeInstance);
        enableFlying(targetData.getEntity());
    }

    @Override
    public void onInstanceRemoved(IMKEntityData targetData, MKActiveEffect expiredEffect) {
        super.onInstanceRemoved(targetData, expiredEffect);
        if (targetData.getEntity() instanceof ServerPlayer player) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
