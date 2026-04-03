package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTrigger;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerType;
import com.chaosbuffalo.mkcore.effects.triggers.MKTriggerContributor;

import java.util.*;

public class EntityTriggerRegistry {
    private final IMKEntityData entityData;
    private final Set<EntityTriggerType<?>> activeTriggerTypes = new HashSet<>();
    private boolean dirty = true;
    private Map<EntityTriggerType<?>, List<EntityTrigger<?>>> triggersByType = Map.of();

    public EntityTriggerRegistry(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    public void rebuild() {
        dirty = true;
    }

    public <TContext> void dispatch(EntityTriggerType<TContext> triggerType, TContext context) {
        rebuildIfNeeded();
        List<EntityTrigger<?>> triggers = triggersByType.get(triggerType);
        if (triggers == null || triggers.isEmpty()) {
            return;
        }
        if (!activeTriggerTypes.add(triggerType)) {
            return;
        }

        try {
            for (EntityTrigger<?> trigger : triggers) {
                dispatchTyped(trigger, context);
            }
        } finally {
            activeTriggerTypes.remove(triggerType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <TContext> void dispatchTyped(EntityTrigger<?> trigger, TContext context) {
        ((EntityTrigger<TContext>) trigger).execute(context);
    }

    private void rebuildIfNeeded() {
        if (!dirty) {
            return;
        }

        Map<EntityTriggerType<?>, List<EntityTrigger<?>>> builder = new HashMap<>();
        EntityTriggerRegistrar registrar = new EntityTriggerRegistrar() {
            @Override
            public <TContext> void add(EntityTriggerType<TContext> triggerType, EntityTrigger<? super TContext> trigger) {
                builder.computeIfAbsent(triggerType, ignored -> new ArrayList<>()).add(trigger);
            }
        };

        for (MKActiveEffect activeEffect : entityData.getEffects().effects()) {
            if (activeEffect.getEffect() instanceof MKTriggerContributor contributor) {
                contributor.registerTriggers(activeEffect, registrar);
            }
        }

        Map<EntityTriggerType<?>, List<EntityTrigger<?>>> finalized = new HashMap<>();
        builder.forEach((type, triggers) -> finalized.put(type, List.copyOf(triggers)));
        triggersByType = finalized;
        dirty = false;
    }
}
