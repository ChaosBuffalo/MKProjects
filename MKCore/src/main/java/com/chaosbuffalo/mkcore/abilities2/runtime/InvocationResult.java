package com.chaosbuffalo.mkcore.abilities2.runtime;

import javax.annotation.Nullable;
import java.util.UUID;

public record InvocationResult(
        boolean started,
        @Nullable UUID invocationId,
        @Nullable FailureReason failureReason
) {
    public static InvocationResult started(UUID invocationId) {
        return new InvocationResult(true, invocationId, null);
    }

    public static InvocationResult failed(FailureReason failureReason) {
        return new InvocationResult(false, null, failureReason);
    }
}
