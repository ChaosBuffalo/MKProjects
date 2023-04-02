package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class FallTriggers extends SpellTriggers.TriggerCollectionBase {
    @FunctionalInterface
    public interface FallTrigger {
        void apply(LivingHurtEvent event, DamageSource source, LivingEntity entity);
    }

    private static final String TAG = "FALL";
    private static final List<FallTrigger> fallTriggers = new ArrayList<>();

    @Override
    public boolean hasTriggers() {
        return fallTriggers.size() > 0;
    }

    public void register(FallTrigger trigger) {
        fallTriggers.add(trigger);
    }

    public void onLivingFall(LivingHurtEvent event, DamageSource source, LivingEntity entity) {
        if (fallTriggers.size() == 0 || startTrigger(entity, TAG))
            return;
        fallTriggers.forEach(f -> f.apply(event, source, entity));
        endTrigger(entity, TAG);
    }
}
