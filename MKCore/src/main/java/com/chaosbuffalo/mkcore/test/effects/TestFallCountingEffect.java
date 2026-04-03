package com.chaosbuffalo.mkcore.test.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.*;
import com.chaosbuffalo.mkcore.effects.triggers.CoreTriggerTypes;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.google.common.reflect.TypeToken;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

import java.util.UUID;

public class TestFallCountingEffect extends MKEffect implements MKTriggerContributor {

    private final TypeToken<State> STATE = new TypeToken<>() {
    };

    public TestFallCountingEffect() {
        super(MobEffectCategory.BENEFICIAL);
    }

    private void onFall(LivingFallEvent event, IMKEntityData targetData) {
        if (!(targetData instanceof MKPlayerData playerData))
            return;

        MKCore.LOGGER.info("onFall {} {}d {}x", playerData.getEntity(), event.getDistance(), event.getDamageMultiplier());

        targetData.getEffects().effects(this).forEach(activeEffect -> {
            ChatUtils.sendMessage(playerData.getEntity(), "onFall");
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

    @Override
    public void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar) {
        registrar.add(CoreTriggerTypes.FALL, context ->
                activeEffect.getState(STATE).counter++);
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
