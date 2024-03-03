package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.effects.triggers.*;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpellTriggers {

    public static final FallTriggers FALL = new FallTriggers();
    public static final LivingHurtEntityTriggers LIVING_HURT_ENTITY = new LivingHurtEntityTriggers();
    public static final EntityHurtTriggers ENTITY_HURT = new EntityHurtTriggers();
    public static final LivingKillEntityTriggers LIVING_KILL_ENTITY = new LivingKillEntityTriggers();

    public static abstract class TriggerCollectionBase {

        public abstract boolean hasTriggers();

        // true = trigger already active or not needed
        // false = trigger started
        protected boolean startTrigger(IMKEntityData source, String tag) {
            if (!hasTriggers())
                return true;

            if (source instanceof MKPlayerData playerData) {
                if (playerData.getCombatExtension().hasSpellTag(tag)) {
                    return true;
                }
                playerData.getCombatExtension().addSpellTag(tag);
                return false;
            }
            return true;
        }

        protected void endTrigger(IMKEntityData source, String tag) {
            if (source instanceof MKPlayerData playerData) {
                playerData.getCombatExtension().removeSpellTag(tag);
            }
        }
    }

    public static abstract class EffectBasedTriggerCollection<TTrigger> extends TriggerCollectionBase {
        protected final Map<MKEffect, TTrigger> effectTriggers = new HashMap<>();

        @Override
        public boolean hasTriggers() {
            return !effectTriggers.isEmpty();
        }

        public void register(MKEffect potion, TTrigger trigger) {
            effectTriggers.put(potion, trigger);
        }

        protected void runTrigger(LivingEntity entity, String tag, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            IMKEntityData entityData = MKCore.getEntityDataOrNull(entity);
            if (entityData == null)
                return;
            runTrigger(entityData, tag, consumer);
        }

        protected void runTrigger(IMKEntityData entityData, String tag, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            if (startTrigger(entityData, tag))
                return;

            dispatchTriggers(entityData, consumer);

            endTrigger(entityData, tag);
        }

        private void dispatchTriggers(IMKEntityData targetData, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            if (!targetData.getEffects().hasEffects())
                return;

            for (MKActiveEffect effect : targetData.getEffects().effects()) {
                TTrigger trigger = effectTriggers.get(effect.getEffect());
                if (trigger != null) {
                    consumer.accept(trigger, effect);
                }
            }
        }
    }
}
