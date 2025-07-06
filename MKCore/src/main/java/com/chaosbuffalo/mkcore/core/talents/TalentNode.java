package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;

public abstract class TalentNode {
    public static final Codec<TalentNode> CODEC = MKCoreRegistry.TALENT_TYPES.byNameCodec()
            .dispatch(TalentNode::getType, TalentType::codec);

    protected final int maxRanks;
    protected final Holder<TalentNodeDisplay> displayHolder;
    protected TalentLineDefinition line;
    protected int index;

    public TalentNode(Holder<TalentNodeDisplay> displayHolder, int maxRanks) {
        this.maxRanks = maxRanks;
        this.displayHolder = displayHolder;
    }

    public TalentNodeDisplay getDisplay() {
        return displayHolder.value();
    }

    public abstract TalentType<?> getType();

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

    public int getMaxRanks() {
        return maxRanks;
    }

    public TalentRecord createRecord(TalentTreeRecord treeRecord) {
        return new TalentRecord(this, treeRecord);
    }

    @Override
    public String toString() {
        return "TalentNode{" +
                "index=" + index +
                ", maxRanks=" + maxRanks +
                '}';
    }
}
