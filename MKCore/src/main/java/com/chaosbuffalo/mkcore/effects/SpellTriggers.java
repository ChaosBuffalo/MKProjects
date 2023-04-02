package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.triggers.*;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpellTriggers {

    @Deprecated // Still used by MKWeapons. Delete once moved
    public static boolean isMinecraftPhysicalDamage(DamageSource source) {
        return DamageUtils.isMinecraftPhysicalDamage(source);
    }

    public static final FallTriggers FALL = new FallTriggers();
    public static final LivingHurtEntityTriggers LIVING_HURT_ENTITY = new LivingHurtEntityTriggers();
    public static final EntityHurtTriggers ENTITY_HURT = new EntityHurtTriggers();
    public static final EntityAttackedTriggers LIVING_ATTACKED = new EntityAttackedTriggers();
    public static final PlayerAttackEntityTriggers PLAYER_ATTACK_ENTITY = new PlayerAttackEntityTriggers();
    public static final EmptyLeftClickTriggers EMPTY_LEFT_CLICK = new EmptyLeftClickTriggers();
    public static final LivingKillEntityTriggers LIVING_KILL_ENTITY = new LivingKillEntityTriggers();
    public static final LivingDeathTriggers LIVING_DEATH = new LivingDeathTriggers();
    public static final LivingEquipmentChangeEvent LIVING_EQUIPMENT_CHANGE = new LivingEquipmentChangeEvent();

    public static abstract class TriggerCollectionBase {

        public abstract boolean hasTriggers();

        // true = trigger already active or not needed
        // false = trigger started
        protected boolean startTrigger(Entity source, String tag) {
            if (!hasTriggers())
                return true;

            if (source instanceof Player) {
                return MKCore.getPlayer(source).map(cap -> {
                    if (cap.getCombatExtension().hasSpellTag(tag)) {
                        return true;
                    }
                    cap.getCombatExtension().addSpellTag(tag);
                    return false;
                }).orElse(true);
            }
            return true;
        }

        protected void endTrigger(Entity source, String tag) {
            if (source instanceof Player) {
                MKCore.getPlayer(source).ifPresent(cap -> cap.getCombatExtension().removeSpellTag(tag));
            }
        }
    }

    public static abstract class EffectBasedTriggerCollection<TTrigger> extends TriggerCollectionBase {
        protected final Map<MKEffect, TTrigger> effectTriggers = new HashMap<>();

        @Override
        public boolean hasTriggers() {
            return effectTriggers.size() > 0;
        }

        public void register(MKEffect potion, TTrigger trigger) {
            effectTriggers.put(potion, trigger);
        }

        protected void runTrigger(LivingEntity entity, String tag, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            if (startTrigger(entity, tag))
                return;

            dispatchTriggers(entity, consumer);

            endTrigger(entity, tag);
        }

        private void dispatchTriggers(LivingEntity entity, BiConsumer<TTrigger, MKActiveEffect> consumer) {
            MKCore.getEntityData(entity).ifPresent(targetData -> {
                for (MKActiveEffect effect : targetData.getEffects().effects()) {
                    TTrigger trigger = effectTriggers.get(effect.getEffect());
                    if (trigger != null) {
                        consumer.accept(trigger, effect);
                    }
                }
            });
        }
    }
}
