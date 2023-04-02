package com.chaosbuffalo.mkcore.utils;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private double total = 0;

    public void add(double weight, E result) {
        if (weight <= 0 || map.containsValue(result))
            return;
        total += weight;
        map.put(total, result);
    }

    public E next(Random random) {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }

    public Collection<E> getValues() {
        return map.values();
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }
}

