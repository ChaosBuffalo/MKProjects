package com.chaosbuffalo.mkcore.effects.triggers;

@FunctionalInterface
public interface EntityTrigger<TContext> {
    void execute(TContext context);
}
