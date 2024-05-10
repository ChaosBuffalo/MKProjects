package com.chaosbuffalo.mkcore.abilities.flow.steps;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.flow.AbilityFlowStep;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class BeginTimedCastStep extends AbilityFlowStep {

    private int castTicks;
    private BeginCastFn serverCallback;

    public interface BeginCastFn {
        void onBegin(IMKEntityData casterData, int castTime, AbilityContext context);
    }

    public BeginTimedCastStep(int castTicks) {
        this.castTicks = castTicks;
    }

    public BeginTimedCastStep onServer(BeginCastFn cast) {
        this.serverCallback = cast;
        return this;
    }

    @Override
    public BeginTimedCastStep onClient(String tag) {
        super.onClient(tag);
        return this;
    }
}
