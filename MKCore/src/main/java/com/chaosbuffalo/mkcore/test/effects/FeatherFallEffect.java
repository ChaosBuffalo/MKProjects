package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class FeatherFallEffect extends MKEffect {

    public static MKEffectBuilder<?> from(LivingEntity source) {
        return MKTestEffects.FEATHER_FALL.get().builder(source);
    }

    public FeatherFallEffect() {
        super(MobEffectCategory.BENEFICIAL);
        SpellTriggers.FALL.register(this::onFall);
    }

    private void onFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        MKCore.getEntityData(entity).ifPresent(targetData -> {
            if (targetData.getEffects().isEffectActive(this)) {
                event.setAmount(0.0f);
                if (entity instanceof Player) {
                    entity.sendMessage(new TextComponent("My legs are OK"), Util.NIL_UUID);
                }
            }
        });
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
