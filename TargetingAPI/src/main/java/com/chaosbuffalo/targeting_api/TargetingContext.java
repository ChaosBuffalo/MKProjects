package com.chaosbuffalo.targeting_api;


import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;

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

    public boolean canBeCreative() {
        return canBeCreative;
    }

    public boolean canBeSpectator() {
        return canBeSpectator;
    }

    public boolean canTargetCaster() {
        return canTargetCaster;
    }

    public Component getLocalizedDescription() {
        return Component.translatable(locKey);
    }

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

    public static class Builder {
        private boolean canTargetCaster;
        private boolean requiresAlive;
        private boolean canBeSpectator;
        private boolean canBeCreative;
        private final Class<? extends Entity> clazz;
        private BiPredicate<Entity, Entity> targetTest;
        private String locKey;

        public Builder(Class<? extends Entity> clazz) {
            this.clazz = clazz;
            canTargetCaster = true;
            requiresAlive = true;
            locKey = "targeting_api.targeting_context.default";
        }

        public Builder(TargetingContext existing) {
            canTargetCaster = existing.canTargetCaster;
            requiresAlive = existing.requiresAlive;
            canBeCreative = existing.canBeCreative;
            canBeSpectator = existing.canBeSpectator;
            clazz = existing.clazz;
            targetTest = existing.targetTest;
            locKey = existing.locKey;
        }

        public Builder canTargetCaster(boolean allow) {
            canTargetCaster = allow;
            return this;
        }

        public Builder requiresTargetAlive(boolean allow) {
            requiresAlive = allow;
            return this;
        }

        public Builder setLocalizationKey(String key) {
            locKey = key;
            return this;
        }

        public Builder canTargetSpectators(boolean allow) {
            canBeSpectator = allow;
            return this;
        }

        public Builder canTargetCreative(boolean allow) {
            canBeCreative = allow;
            return this;
        }

        public Builder setTargetTest(BiPredicate<Entity, Entity> test) {
            this.targetTest = test;
            return this;
        }

        public TargetingContext build() {
            return new TargetingContext(this);
        }

        public static Builder create(Class<? extends Entity> classFilter) {
            return new TargetingContext.Builder(classFilter);
        }

        public static Builder from(TargetingContext existing) {
            return new TargetingContext.Builder(existing);
        }
    }
}
