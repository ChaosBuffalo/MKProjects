package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.effects.triggers.EntityTrigger;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;
import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerType;

import java.util.*;

public class EntityTriggerRegistry {
    private final List<EntityTriggerContributorSource> contributorSources;
    private final Set<EntityTriggerType<?>> activeTriggerTypes = new HashSet<>();
    private long builtFromContributorVersion = -1;
    private Map<EntityTriggerType<?>, List<EntityTrigger<?>>> triggersByType = Map.of();

    public EntityTriggerRegistry(List<EntityTriggerContributorSource> contributorSources) {
        this.contributorSources = List.copyOf(contributorSources);
    }

    @Deprecated(forRemoval = false)
    public void rebuild() {
        builtFromContributorVersion = -1;
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
        long contributorVersion = computeCombinedContributorVersion();
        if (builtFromContributorVersion == contributorVersion) {
            return;
        }

        Map<EntityTriggerType<?>, List<EntityTrigger<?>>> builder = new HashMap<>();
        EntityTriggerRegistrar registrar = new EntityTriggerRegistrar() {
            @Override
            public <TContext> void add(EntityTriggerType<TContext> triggerType, EntityTrigger<? super TContext> trigger) {
                builder.computeIfAbsent(triggerType, ignored -> new ArrayList<>()).add(trigger);
            }
        };

        for (EntityTriggerContributorSource source : contributorSources) {
            source.contributeTriggers(registrar);
        }

        Map<EntityTriggerType<?>, List<EntityTrigger<?>>> finalized = new HashMap<>();
        builder.forEach((type, triggers) -> finalized.put(type, List.copyOf(triggers)));
        triggersByType = finalized;
        builtFromContributorVersion = contributorVersion;
    }

    private long computeCombinedContributorVersion() {
        long version = 1L;
        for (EntityTriggerContributorSource source : contributorSources) {
            version = 31L * version + source.getTriggerContributorVersion();
        }
        return version;
    }
}
