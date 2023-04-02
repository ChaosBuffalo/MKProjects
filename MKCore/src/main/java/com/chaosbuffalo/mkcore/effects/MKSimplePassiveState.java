package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class MKSimplePassiveState extends MKEffectState {
    public static MKSimplePassiveState INSTANCE = new MKSimplePassiveState();

    @Override
    public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
        return true;
    }

    @Override
    public boolean isReady(IMKEntityData targetData, MKActiveEffect instance) {
        // This is a simple passive meant to provide attributes and does not need to tick
        return false;
    }
}
