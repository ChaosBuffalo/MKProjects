package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import net.minecraftforge.common.util.Lazy;

import java.util.EnumSet;
import java.util.function.Supplier;

public enum AbilityGroupId {
    Basic(true, true, () -> EnumSet.of(AbilityType.Basic), GameConstants.DEFAULT_BASIC_ABILITIES, GameConstants.MAX_BASIC_ABILITIES),
    Passive(true, false, () -> EnumSet.of(AbilityType.Passive), GameConstants.DEFAULT_PASSIVE_ABILITIES, GameConstants.MAX_PASSIVE_ABILITIES),
    Ultimate(true, true, () -> EnumSet.of(AbilityType.Ultimate), GameConstants.DEFAULT_ULTIMATE_ABILITIES, GameConstants.MAX_ULTIMATE_ABILITIES),
    Item(false, true, () -> EnumSet.of(AbilityType.Basic, AbilityType.Ultimate), GameConstants.DEFAULT_ITEM_ABILITIES, GameConstants.MAX_ITEM_ABILITIES);

    private final boolean requiresAbilityKnown;
    private final boolean isActive;
    private final Lazy<EnumSet<AbilityType>> memberTypes;
    private final int defaultSlots;
    private final int maxSlots;

    AbilityGroupId(boolean requiresAbilityKnown, boolean isActive, Supplier<EnumSet<AbilityType>> memberTypes,
                   int defaultSlots, int maxSlots) {
        this.requiresAbilityKnown = requiresAbilityKnown;
        this.isActive = isActive;
        this.memberTypes = Lazy.of(memberTypes);
        this.defaultSlots = defaultSlots;
        this.maxSlots = maxSlots;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean requiresAbilityKnown() {
        return requiresAbilityKnown;
    }

    public EnumSet<AbilityType> getMemberTypes() {
        return memberTypes.get();
    }

    public boolean fitsAbilityType(AbilityType abilityType) {
        return getMemberTypes().contains(abilityType);
    }

    public int getDefaultSlots() {
        return defaultSlots;
    }

    public int getMaxSlots() {
        return maxSlots;
    }
}
