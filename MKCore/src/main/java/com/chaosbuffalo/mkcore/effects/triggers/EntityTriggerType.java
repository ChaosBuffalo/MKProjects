package com.chaosbuffalo.mkcore.effects.triggers;

import java.util.Objects;

public final class EntityTriggerType<TContext> {
    private final String name;

    public EntityTriggerType(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "EntityTriggerType{" + name + '}';
    }
}
