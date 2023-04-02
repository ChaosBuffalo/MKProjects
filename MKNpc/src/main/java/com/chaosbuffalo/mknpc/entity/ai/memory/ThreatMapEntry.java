package com.chaosbuffalo.mknpc.entity.ai.memory;

public class ThreatMapEntry {
    private float currentThreat;

    public ThreatMapEntry() {
        currentThreat = 0;
    }

    public float getCurrentThreat() {
        return currentThreat;
    }

    public ThreatMapEntry addThreat(float value) {
        currentThreat += value;
        return this;
    }

    public ThreatMapEntry subtractThreat(float value) {
        currentThreat -= value;
        return this;
    }
}
