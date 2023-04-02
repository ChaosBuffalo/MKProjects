package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public class TestFallCountingEffect extends MKEffect {

    private final TypeToken<State> STATE = new TypeToken<State>() {
    };

    public TestFallCountingEffect() {
        super(MobEffectCategory.BENEFICIAL);
        SpellTriggers.FALL.register(this::onFall);
    }

    private void onFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        MKCore.LOGGER.info("onFall {} {}", entity, event.getAmount());

        MKPlayerData targetData = MKCore.getPlayerOrNull(entity);
        if (targetData == null)
            return;

        targetData.getEffects().effects(this).forEach(activeEffect -> {
            ChatUtils.sendMessage(targetData.getEntity(), "onFall");
            activeEffect.getState(STATE).counter++;
        });
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    public static class State extends MKEffectState {

        private int lastCounter;
        private int counter;
        private final int max = 5;

        @Override
        public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
            return counter > lastCounter;
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            lastCounter = counter;
            if (!(targetData instanceof MKPlayerData playerData)) {
                return false;
            }

            ChatUtils.sendMessage(playerData.getEntity(), "Fall counter %d", counter);

            instance.modifyStackCount(1);

            if (counter >= max) {
                ChatUtils.sendMessage(playerData.getEntity(), "Fall counter done");
                return false;
            }

            return true;
        }
    }
}
