package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import com.mojang.serialization.Dynamic;

public class EntitlementGrantTalent extends MKTalent {

    private final TalentType<?> talentType;

    public EntitlementGrantTalent(TalentType<?> talentType) {
        super();
        this.talentType = talentType;
    }

    @Override
    public TalentType<?> getTalentType() {
        return talentType;
    }

    @Override
    public <T> TalentNode createNode(Dynamic<T> dynamic) {
        return new EntitlementGrantTalentNode(this, dynamic);
    }
}
