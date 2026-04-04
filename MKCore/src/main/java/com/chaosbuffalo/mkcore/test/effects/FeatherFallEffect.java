package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.FallTriggerContext;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FeatherFallEffect extends MKEffect implements MKTriggerContributor {

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return MKTestEffects.FEATHER_FALL.get().builder(source);
    }

    public FeatherFallEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    @Override
    public void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar) {
        registrar.add(CoreTriggerTypes.FALL, this::onFall);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }

    private void onFall(FallTriggerContext context) {
        IMKEntityData targetData = context.entityData();
        if (targetData.getEffects().isEffectActive(this)) {
            context.event().setCanceled(true);
            if (targetData.getEntity() instanceof Player player) {
                player.sendSystemMessage(Component.translatable("My legs are OK"));
            }
        }
    }
}
