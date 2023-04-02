package com.chaosbuffalo.mkcore.core;

public enum CastInterruptReason {
    Jump(false),
    StartedBlocking(false),
    Stun(false),
    CounterSpell(false),
    Teleport(false),
    Death(true);

    private final boolean noBypass;

    CastInterruptReason(boolean noBypass) {
        this.noBypass = noBypass;
    }

    public boolean cannotBeBypassed() {
        return noBypass;
    }
}
