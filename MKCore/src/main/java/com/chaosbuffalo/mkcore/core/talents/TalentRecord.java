package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class TalentRecord implements IRecordInstance {

    private int currentRank;
    private final TalentNode node;

    public TalentRecord(TalentNode node) {
        this.node = node;
        currentRank = 0;
    }

    public TalentNode getNode() {
        return node;
    }

    public boolean isKnown() {
        return currentRank > 0;
    }

    public int getRank() {
        return currentRank;
    }

    public boolean modifyRank(int value) {
        int next = currentRank + value;
        if (next >= 0 && next <= node.getMaxRanks()) {
            setRank(next);
            return true;
        }
        return false;
    }

    public void setRank(int value) {
        currentRank = value;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("rank"), ops.createInt(currentRank));
        return ops.createMap(builder.build());
    }

    public <T> boolean deserialize(Dynamic<T> dynamic) {
        int rank = dynamic.get("rank").asInt(0);
        if (rank > node.getMaxRanks())
            return false;
        // Validation complete, assign the points
        currentRank = rank;
        return true;
    }

    public String toString() {
        return String.format("TalentRecord{node=%s, rank=%d}", node, currentRank);
    }

    @Override
    public IRecordType<?> getRecordType() {
        return node.getTalentType();
    }
}
