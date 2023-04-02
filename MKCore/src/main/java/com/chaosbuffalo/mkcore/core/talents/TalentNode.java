package com.chaosbuffalo.mkcore.core.talents;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class TalentNode {
    private final MKTalent talent;
    private final int maxRanks;
    private TalentLineDefinition line;
    private int index;

    public TalentNode(MKTalent talent, Dynamic<?> entry) {
        this.talent = talent;
        this.maxRanks = entry.get("max_points").asInt(1);
    }

    public TalentNode(MKTalent talent, int maxRanks) {
        this.talent = talent;
        this.maxRanks = maxRanks;
    }

    public TalentNode(MKTalent talent) {
        this(talent, 1);
    }

    void link(TalentLineDefinition line, int index) {
        this.index = index;
        this.line = line;
    }

    public TalentTreeDefinition getTree() {
        return line.getTree();
    }

    public TalentLineDefinition getLine() {
        return line;
    }

    public int getIndex() {
        return index;
    }

    TalentType<?> getTalentType() {
        return talent.getTalentType();
    }

    public MKTalent getTalent() {
        return talent;
    }

    public int getMaxRanks() {
        return maxRanks;
    }

    public TalentRecord createRecord() {
        return new TalentRecord(this);
    }

    public String getPositionString() {
        return String.format("%s@%d", getLine().getName(), getIndex());
    }

    @Override
    public String toString() {
        return "TalentNode{" +
                "pos=" + getPositionString() +
                "talent=" + talent +
                ", maxRanks=" + maxRanks +
                '}';
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("name"), ops.createString(talent.getTalentId().toString()));
        builder.put(ops.createString("max_points"), ops.createInt(maxRanks));
        return ops.createMap(builder.build());
    }
}
