package com.chaosbuffalo.targeting_api;


import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;

/**
 * Immutable description of the rules used to decide whether an {@link Entity}
 * can be targeted.
 * <p>
 * A context combines entity type filtering, self-targeting rules, alive and
 * player state checks, and a final custom predicate.
 */
public class TargetingContext {
    private final boolean canTargetCaster;
    private final boolean requiresAlive;
    private final boolean canBeSpectator;
    private final boolean canBeCreative;
    private final Class<? extends Entity> clazz;
    private final BiPredicate<Entity, Entity> targetTest;
    private final String locKey;

    protected boolean isValidClass(Entity target) {
        return clazz.isInstance(target);
    }

    /**
     * @return {@code true} if creative-mode players may be targeted
     */
    public boolean canBeCreative() {
        return canBeCreative;
    }

    /**
     * @return {@code true} if spectator entities may be targeted
     */
    public boolean canBeSpectator() {
        return canBeSpectator;
    }

    /**
     * @return {@code true} if the caster may target itself
     */
    public boolean canTargetCaster() {
        return canTargetCaster;
    }

    /**
     * Returns a localized description component for this context.
     *
     * @return the translated description component
     */
    public Component getLocalizedDescription() {
        return Component.translatable(locKey);
    }

    /**
     * Tests whether a target satisfies this context.
     *
     * @param caster the acting entity
     * @param target the candidate target
     * @return {@code true} if the target passes all configured checks
     */
    public boolean isValidTarget(Entity caster, Entity target) {
        if (caster == null || target == null) {
            return false;
        }

        if (!isValidClass(target)) {
            return false;
        }

        if (!canTargetCaster && Targeting.areEntitiesEqual(caster, target)) {
            return false;
        }

        if (requiresAlive && !target.isAlive()) {
            return false;
        }

        if (!canBeSpectator && target.isSpectator()) {
            return false;
        }

        if (!canBeCreative && target instanceof Player && ((Player) target).isCreative()) {
            return false;
        }

        return targetTest.test(caster, target);
    }

    private TargetingContext(Builder builder) {
        this.clazz = builder.clazz;
        this.requiresAlive = builder.requiresAlive;
        this.canTargetCaster = builder.canTargetCaster;
        this.canBeCreative = builder.canBeCreative;
        this.canBeSpectator = builder.canBeSpectator;
        this.targetTest = builder.targetTest;
        this.locKey = builder.locKey;
    }

    /**
     * Builder for creating or copying {@link TargetingContext} instances.
     */
    public static class Builder {
        private boolean canTargetCaster;
        private boolean requiresAlive;
        private boolean canBeSpectator;
        private boolean canBeCreative;
        private final Class<? extends Entity> clazz;
        private BiPredicate<Entity, Entity> targetTest;
        private String locKey;

        /**
         * Creates a builder with default settings for the supplied entity class.
         * <p>
         * Defaults are: caster can be targeted, targets must be alive,
         * {@link Player players} in creative mode can be targeted, and the
         * default localization key is used.
         *
         * @param clazz the required target entity type
         */
        public Builder(Class<? extends Entity> clazz) {
            this.clazz = clazz;
            canTargetCaster = true;
            requiresAlive = true;
            canBeCreative = true;
            locKey = "targeting_api.targeting_context.default";
        }

        /**
         * Creates a builder initialized from an existing context.
         *
         * @param existing the context to copy
         */
        public Builder(TargetingContext existing) {
            canTargetCaster = existing.canTargetCaster;
            requiresAlive = existing.requiresAlive;
            canBeCreative = existing.canBeCreative;
            canBeSpectator = existing.canBeSpectator;
            clazz = existing.clazz;
            targetTest = existing.targetTest;
            locKey = existing.locKey;
        }

        /**
         * Controls whether the caster may target itself.
         *
         * @param allow {@code true} to allow self-targeting
         * @return this builder
         */
        public Builder canTargetCaster(boolean allow) {
            canTargetCaster = allow;
            return this;
        }

        /**
         * Controls whether dead targets are accepted.
         *
         * @param allow {@code true} if targets must be alive
         * @return this builder
         */
        public Builder requiresTargetAlive(boolean allow) {
            requiresAlive = allow;
            return this;
        }

        /**
         * Sets the translation key used by {@link #getLocalizedDescription()}.
         *
         * @param key the translation key
         * @return this builder
         */
        public Builder setLocalizationKey(String key) {
            locKey = key;
            return this;
        }

        /**
         * Controls whether spectator entities may be targeted.
         *
         * @param allow {@code true} to allow spectators
         * @return this builder
         */
        public Builder canTargetSpectators(boolean allow) {
            canBeSpectator = allow;
            return this;
        }

        /**
         * Controls whether creative-mode players may be targeted.
         *
         * @param allow {@code true} to allow creative players
         * @return this builder
         */
        public Builder canTargetCreative(boolean allow) {
            canBeCreative = allow;
            return this;
        }

        /**
         * Sets the final predicate used after the basic context checks succeed.
         *
         * @param test the target validation predicate
         * @return this builder
         */
        public Builder setTargetTest(BiPredicate<Entity, Entity> test) {
            this.targetTest = test;
            return this;
        }

        /**
         * Builds an immutable targeting context from the current builder state.
         *
         * @return the created targeting context
         */
        public TargetingContext build() {
            if (targetTest == null) throw new IllegalStateException("targetTest must be set before building a TargetingContext");
            return new TargetingContext(this);
        }

        /**
         * Creates a new builder for the supplied entity class.
         *
         * @param classFilter the required target entity type
         * @return a new builder
         */
        public static Builder create(Class<? extends Entity> classFilter) {
            return new TargetingContext.Builder(classFilter);
        }

        /**
         * Creates a builder initialized from an existing context.
         *
         * @param existing the context to copy
         * @return a new builder
         */
        public static Builder from(TargetingContext existing) {
            return new TargetingContext.Builder(existing);
        }
    }
}
