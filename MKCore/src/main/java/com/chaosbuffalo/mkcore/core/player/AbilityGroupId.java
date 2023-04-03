package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.AbilityType;

import java.util.EnumSet;
import java.util.Set;

public enum AbilityGroupId {
    Basic(EnumSet.of(AbilityType.Basic), GameConstants.DEFAULT_BASIC_ABILITIES, GameConstants.MAX_BASIC_ABILITIES),
    Passive(EnumSet.of(AbilityType.Passive), GameConstants.DEFAULT_PASSIVE_ABILITIES, GameConstants.MAX_PASSIVE_ABILITIES),
    Ultimate(EnumSet.of(AbilityType.Ultimate), GameConstants.DEFAULT_ULTIMATE_ABILITIES, GameConstants.MAX_ULTIMATE_ABILITIES),
    Item(EnumSet.of(AbilityType.Basic, AbilityType.Ultimate), GameConstants.DEFAULT_ITEM_ABILITIES, GameConstants.MAX_ITEM_ABILITIES);

    private final Set<AbilityType> memberTypes;
    private final int defaultSlots;
    private final int maxSlots;

    AbilityGroupId(Set<AbilityType> memberTypes,
                   int defaultSlots, int maxSlots) {
        this.memberTypes = memberTypes;
        this.defaultSlots = defaultSlots;
        this.maxSlots = maxSlots;
    }

    public boolean fitsAbilityType(AbilityType abilityType) {
        return memberTypes.contains(abilityType);
    }

    public int getDefaultSlots() {
        return defaultSlots;
    }

    public int getMaxSlots() {
        return maxSlots;
    }
}
