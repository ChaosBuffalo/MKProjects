package com.chaosbuffalo.mkcore.abilities.flow;

import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.effects.MKEffect;

public class AbilityFlow {

    public AbilityFlow() {

    }

    public static AbilityFlowBuilder builder() {
        return new AbilityFlowBuilder();
    }

    public static AbilityFlowBuilder builder(AbilityClientState state) {
        return new AbilityFlowBuilder();
    }

    public static AbilityFlow of(AbilityFlowStep... steps) {
        return new AbilityFlow();
    }
}
