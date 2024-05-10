package com.chaosbuffalo.mkcore.abilities.flow;

import com.chaosbuffalo.mkcore.abilities.flow.steps.BeginTimedCastStep;
import com.chaosbuffalo.mkcore.abilities.flow.steps.CastFinishStep;
import com.chaosbuffalo.mkcore.abilities.flow.steps.PeriodicTickStep;
import com.chaosbuffalo.mkcore.effects.MKEffect;

public class FlowSteps {

    public static BeginTimedCastStep startTimedCast(int castTime) {
        return new BeginTimedCastStep(castTime);
    }

    public static PeriodicTickStep every(int ticks) {
        return new PeriodicTickStep(ticks);
    }

    public static AbilityFlowStep at(int tick) {
        return new AbilityFlowStep();
    }

    public static AbilityFlowStep startGCD() {
        return new AbilityFlowStep();
    }

    public static AbilityFlowStep startGCD(int ticks) {
        return new AbilityFlowStep();
    }

    public static CastFinishStep finish() {
        return new CastFinishStep();
    }

    public static AbilityFlowStep applyEffect(MKEffect effect) {
        return new AbilityFlowStep();
    }

}
