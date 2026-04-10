package com.chaosbuffalo.mknpc.entity.ai.goal;

import com.chaosbuffalo.mknpc.entity.MKEntity;

public class FlyingUseAbilityGoal extends UseAbilityGoal {
    public FlyingUseAbilityGoal(MKEntity entity) {
        super(entity, true);
    }
}
