package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

public class TalentRecord implements IRecordInstance<TalentRecord> {

    private final TalentNode node;
    private final ResourceLocation uniqueId;
    private final String lineName;
    private final int index;
    private int currentRank;

    public TalentRecord(TalentNode node, ResourceLocation treeId, String lineName, int index) {
        this.node = node;
        this.uniqueId = treeId.withSuffix("/%s/%d".formatted(lineName, index));
        this.lineName = lineName;
        this.index = index;
        currentRank = 0;
    }

    public TalentNode getNode() {
        return node;
    }

    public String getLineName() {
        return lineName;
    }

    public int getIndex() {
        return index;
    }

    public ResourceLocation getUniqueId() {
        return uniqueId;
    }

    public boolean isKnown() {
        return currentRank > 0;
    }

    public int getRank() {
        return currentRank;
    }

    public boolean setRank(int value) {
        if (value < 0 || value > node.getMaxRanks()) {
            return false;
        }
        currentRank = value;
        return true;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("rank"), ops.createInt(currentRank));
        return ops.createMap(builder.build());
    }

    public <T> boolean deserialize(Dynamic<T> dynamic) {
        int rank = dynamic.get("rank").asInt(0);
        return setRank(rank);
    }

    public String toString() {
        return String.format("TalentRecord{line=%s, index=%d, node=%s, rank=%d}", lineName, index, node, currentRank);
    }

    @Override
    public IRecordType<TalentRecord> getRecordType() {
        return node.getType();
    }
}
