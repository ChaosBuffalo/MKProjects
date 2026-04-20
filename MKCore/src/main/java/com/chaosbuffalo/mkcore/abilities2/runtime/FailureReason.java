package com.chaosbuffalo.mkcore.abilities2.runtime;

public enum FailureReason {
    UNKNOWN_ABILITY,
    UNKNOWN_ACTIVATION,
    ACTIVATION_NOT_EXTERNALLY_CALLABLE,
    INVALID_TARGETS,
    NOT_ENOUGH_RESOURCE,
    ON_COOLDOWN,
    BUSY,
    INTERRUPTED,
    TARGET_LOST,
    UNSUPPORTED_FEATURE
}
