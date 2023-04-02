package com.chaosbuffalo.mkcore.core;

public enum AbilityType {
    Basic(AbilityGroupId.Basic),
    Passive(AbilityGroupId.Passive),
    Ultimate(AbilityGroupId.Ultimate);

    private final AbilityGroupId group;

    AbilityType(AbilityGroupId group) {
        this.group = group;
    }

    public AbilityGroupId getGroup() {
        return group;
    }

}
