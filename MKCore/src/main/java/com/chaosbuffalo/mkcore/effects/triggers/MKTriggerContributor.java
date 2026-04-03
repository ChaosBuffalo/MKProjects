package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.effects.MKActiveEffect;

public interface MKTriggerContributor {
    void registerTriggers(MKActiveEffect activeEffect, EntityTriggerRegistrar registrar);
}
