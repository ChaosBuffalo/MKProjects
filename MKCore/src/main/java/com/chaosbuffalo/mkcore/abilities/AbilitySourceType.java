package com.chaosbuffalo.mkcore.abilities;

import java.util.EnumSet;
import java.util.function.BiFunction;

public enum AbilitySourceType {
    ITEM(AbilitySource.ItemAbilitySource::decode, 1, SourceFlags.HasComplexAcquisition, SourceFlags.Persistent),
    TRAINED(AbilitySource::decode, 2, SourceFlags.PlaceOnBarWhenLearned, SourceFlags.UseAbilityPool, SourceFlags.Persistent),
    GRANTED(AbilitySource::decode, 3, SourceFlags.PlaceOnBarWhenLearned, SourceFlags.Persistent),
    // Talents are stored separately and this source is granted to the entity upon talent record deserialization.
    // This is mostly to support the case where the talent tree version changes and no longer provides an ability it used to.
    // In that case there would be no way to know that the ability should be forgotten by the player
    TALENT(AbilitySource.TalentSource::decode, 4, SourceFlags.HasComplexAcquisition),
    ADMIN(AbilitySource::decode, 5, SourceFlags.Persistent);

    private enum SourceFlags {
        PlaceOnBarWhenLearned,
        UseAbilityPool,
        HasComplexAcquisition,
        Persistent;
    }

    private final BiFunction<AbilitySourceType, String, AbilitySource> factory;
    private final int priority;
    private final EnumSet<SourceFlags> flags;

    AbilitySourceType(BiFunction<AbilitySourceType, String, AbilitySource> factory, int priority, SourceFlags... options) {
        this.factory = factory;
        this.priority = priority;
        this.flags = options.length > 0 ?
                EnumSet.of(options[0], options) :
                EnumSet.noneOf(SourceFlags.class);
    }

    public BiFunction<AbilitySourceType, String, AbilitySource> getFactory() {
        return factory;
    }

    public int getPriority() {
        return priority;
    }

    public boolean placeOnBarWhenLearned() {
        return flags.contains(SourceFlags.PlaceOnBarWhenLearned);
    }

    public boolean usesAbilityPool() {
        return flags.contains(SourceFlags.UseAbilityPool);
    }

    // Whether the Ability can be forgotten without prerequisites, such as being granted by a talent or item
    public boolean isSimple() {
        return !flags.contains(SourceFlags.HasComplexAcquisition);
    }

    public boolean isPersistent() {
        return flags.contains(SourceFlags.Persistent);
    }
}
