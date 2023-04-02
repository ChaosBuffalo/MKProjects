package com.chaosbuffalo.mkcore.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CriticalStats<T> {

    private final float defaultRate;
    private final float defaultMultiplier;
    private final List<CriticalEntry<T>> criticalEntries = new ArrayList<>();

    public CriticalStats(float defaultRate, float defaultMultiplier) {
        this.defaultRate = defaultRate;
        this.defaultMultiplier = defaultMultiplier;
    }

    public float getDefaultChance() {
        return defaultRate;
    }

    public float getDefaultMultiplier() {
        return defaultMultiplier;
    }

    public void addCriticalStats(Class<? extends T> objClass, int priority, float criticalChance, float damageMultiplier) {
        criticalEntries.add(new CriticalEntry<>(objClass, priority, criticalChance, damageMultiplier));
        Collections.sort(criticalEntries);
    }

    private boolean objectMatches(CriticalEntry<T> stat, T obj) {
        Class<? extends T> entityClass = stat.entity;
        return entityClass.isInstance(obj);
    }

    public float getChance(T obj) {
        if (obj == null) {
            return defaultRate;
        }
        for (CriticalEntry<T> stat : criticalEntries) {
            if (objectMatches(stat, obj)) {
                return stat.chance;
            }
        }
        return defaultRate;
    }

    public float getMultiplier(T obj) {
        if (obj == null) {
            return defaultMultiplier;
        }
        for (CriticalEntry<T> stat : criticalEntries) {
            if (objectMatches(stat, obj)) {
                return stat.damageMultiplier;
            }
        }
        return defaultMultiplier;
    }

    public boolean hasChance(T obj) {
        return criticalEntries.stream().anyMatch(stat -> objectMatches(stat, obj));
    }

    private static class CriticalEntry<T> implements Comparable<CriticalEntry<T>> {

        public final Class<? extends T> entity;
        final int priority;
        final float chance;
        final float damageMultiplier;

        CriticalEntry(Class<? extends T> entityClass, int priorityIn, float chanceIn, float damageMult) {
            entity = entityClass;
            priority = priorityIn;
            chance = chanceIn;
            damageMultiplier = damageMult;
        }

        @Override
        public int compareTo(CriticalEntry<T> o) {
            return o.priority - priority;
        }
    }
}
