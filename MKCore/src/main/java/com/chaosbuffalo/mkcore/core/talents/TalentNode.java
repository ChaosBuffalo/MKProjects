package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;

public abstract class TalentNode {
    public static final Codec<TalentNode> CODEC = MKCoreRegistry.TALENT_TYPES.byNameCodec()
            .dispatch(TalentNode::getType, TalentType::codec);

    protected final int maxRanks;
    protected final Holder<TalentNodeDisplay> displayHolder;

    public TalentNode(Holder<TalentNodeDisplay> displayHolder, int maxRanks) {
        this.maxRanks = maxRanks;
        this.displayHolder = displayHolder;
    }

    public TalentNodeDisplay getDisplay() {
        return displayHolder.value();
    }

    public abstract TalentType<?> getType();

    public int getMaxRanks() {
        return maxRanks;
    }

    @Override
    public String toString() {
        return "TalentNode{" +
                "maxRanks=" + maxRanks +
                '}';
    }
}
