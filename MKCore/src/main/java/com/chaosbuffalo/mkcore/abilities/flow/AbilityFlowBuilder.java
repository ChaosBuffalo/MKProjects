package com.chaosbuffalo.mkcore.abilities.flow;

public class AbilityFlowBuilder {

    public AbilityFlowBuilder add(AbilityFlowStep... steps) {
        return this;
    }

    public AbilityFlow build() {
        return new AbilityFlow();
    }
}
