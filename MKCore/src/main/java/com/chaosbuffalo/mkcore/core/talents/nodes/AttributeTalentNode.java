package com.chaosbuffalo.mkcore.core.talents.nodes;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalent;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.function.Supplier;

public class AttributeTalentNode extends TalentNode {

    private final double perRank;

    public AttributeTalentNode(AttributeTalent talent, Dynamic<?> dynamic) {
        super(talent, dynamic);
        this.perRank = dynamic.get("value").asDouble(talent.getDefaultPerRank());
    }

    public AttributeTalentNode(Supplier<AttributeTalent> talent, int maxRanks, double perRank) {
        super(talent.get(), maxRanks);
        this.perRank = perRank;
    }

    public double getValue(int rank) {
        return perRank * rank;
    }

    public double getPerRank() {
        return perRank;
    }

    @Override
    public AttributeTalent getTalent() {
        return (AttributeTalent) super.getTalent();
    }

    public <T> T serialize(DynamicOps<T> ops) {
        T value = super.serialize(ops);
        Optional<T> merged = ops.mergeToMap(value, ops.createString("value"), ops.createDouble(perRank)).resultOrPartial(MKCore.LOGGER::error);
        return merged.orElse(value);
    }
}
