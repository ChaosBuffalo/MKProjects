package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

public class FeatherFallEffect extends MKEffect {

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return MKTestEffects.FEATHER_FALL.get().builder(source);
    }

    public FeatherFallEffect() {
        super(MobEffectCategory.BENEFICIAL);
        SpellTriggers.FALL.register(this::onFall);
    }

    private void onFall(LivingFallEvent event, IMKEntityData targetData) {
        if (targetData.getEffects().isEffectActive(this)) {
            event.setCanceled(true);
            if (targetData.getEntity() instanceof Player player) {
                player.sendSystemMessage(Component.translatable("My legs are OK"));
            }
        }
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
