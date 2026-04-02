package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.combat.damage.MKDamageContext;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class FallTriggers extends SpellTriggers.TriggerCollectionBase {
    @FunctionalInterface
    public interface FallTrigger {
        void apply(LivingDamageEvent.Pre event, DamageSource source, LivingEntity entity);
    }

    private static final String TAG = "FALL";
    private static final List<FallTrigger> fallTriggers = new ArrayList<>();

    @Override
    public boolean hasTriggers() {
        return !fallTriggers.isEmpty();
    }

    public void register(FallTrigger trigger) {
        fallTriggers.add(trigger);
    }

    public void onLivingFall(LivingDamageEvent.Pre event, DamageSource source, LivingEntity entity) {
        if (fallTriggers.isEmpty())
            return;

        IMKEntityData entityData = MKCore.getEntityDataOrThrow(entity);
        if (startTrigger(entityData, TAG))
            return;
        fallTriggers.forEach(f -> f.apply(event, source, entity));
        endTrigger(entityData, TAG);
    }

    public void onLivingFall(MKDamageContext context) {
        context.runLegacyEventMutation("mkcore:fall_trigger", event ->
                onLivingFall(event, context.getSource(), context.getTarget()));
    }
}
