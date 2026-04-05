package com.chaosbuffalo.mkcore.effects.triggers;

@FunctionalInterface
public interface ContextualEntityTrigger<TContext, TExtraContext> {
    void execute(TContext context, TExtraContext extraContext);
}
