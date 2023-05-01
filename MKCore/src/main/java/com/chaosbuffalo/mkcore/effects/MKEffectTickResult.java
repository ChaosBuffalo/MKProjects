package com.chaosbuffalo.mkcore.effects;

public enum MKEffectTickResult {
    NoUpdate(false),
    Update(true),
    Remove(true);

    private final boolean sendsClientUpdate;

    MKEffectTickResult(boolean sendsClientUpdate) {
        this.sendsClientUpdate = sendsClientUpdate;
    }

    public boolean sendsClientUpdate() {
        return sendsClientUpdate;
    }
}
