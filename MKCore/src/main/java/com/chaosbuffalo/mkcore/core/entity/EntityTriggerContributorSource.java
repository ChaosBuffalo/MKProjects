package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.effects.triggers.EntityTriggerRegistrar;

public interface EntityTriggerContributorSource {
    long getTriggerContributorVersion();

    void contributeTriggers(EntityTriggerRegistrar registrar);
}
