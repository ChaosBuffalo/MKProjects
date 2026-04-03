package com.chaosbuffalo.mkcore.effects.triggers;

public interface EntityTriggerRegistrar {
    <TContext> void add(EntityTriggerType<TContext> triggerType, EntityTrigger<? super TContext> trigger);
}
