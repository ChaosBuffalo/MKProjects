package com.chaosbuffalo.mkcore.abilities.flow.steps;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.flow.AbilityFlowStep;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

public class PeriodicTickStep extends AbilityFlowStep {
    private int period;
    private ContinueCastServerFn serverCallback;

    public interface ContinueCastServerFn {
        void onContinue(IMKEntityData casterData, int castTimeLeft, int totalTicks, AbilityContext context);
    }

    public PeriodicTickStep(int period) {
        this.period = period;
    }

    public PeriodicTickStep onServer(ContinueCastServerFn cast) {
        this.serverCallback = cast;
        return this;
    }

    @Override
    public PeriodicTickStep onClient(String tag) {
        super.onClient(tag);
        return this;
    }
}
