package com.chaosbuffalo.mkcore.effects.triggers;

public interface EntityTriggerRegistrar {
    <TContext> void add(EntityTriggerType<TContext> triggerType, EntityTrigger<? super TContext> trigger);

    default <TContext, TExtraContext> void add(EntityTriggerType<TContext> triggerType,
                                               TExtraContext extraContext,
                                               ContextualEntityTrigger<? super TContext, ? super TExtraContext> trigger) {
        add(triggerType, context -> trigger.execute(context, extraContext));
    }
}
