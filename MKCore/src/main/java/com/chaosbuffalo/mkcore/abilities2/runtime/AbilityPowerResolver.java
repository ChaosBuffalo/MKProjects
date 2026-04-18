package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityScalar;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public interface AbilityPowerResolver {
    AbilityStatSnapshot captureInvocationStats(IMKEntityData caster);

    AbilityStatSnapshot resolveStats(AbilityActionContext context, StatCapturePolicy policy);

    double resolve(AbilityScalar scalar, AbilityActionContext context);
}
