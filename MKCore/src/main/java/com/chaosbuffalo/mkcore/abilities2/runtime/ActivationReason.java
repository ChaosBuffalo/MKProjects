package com.chaosbuffalo.mkcore.abilities2.runtime;

public enum ActivationReason {
    DIRECT_REQUEST,
    REACTION,
    DELIVERY_CALLBACK,
    CHANNEL_TICK,
    AURA_PULSE,
    PASSIVE_LIFECYCLE,
    TOGGLE_LIFECYCLE
}
