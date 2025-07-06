package com.chaosbuffalo.mkcore.core.talents.talent_types;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.handlers.EntitlementGrantTalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class EntitlementGrantTalentType extends TalentType<EntitlementGrantTalentNode> {

    @Override
    public MutableComponent getTypeDisplayName(TalentRecord record) {
        return Component.translatableWithFallback("talent_type.mkcore.entitlement_grant.name", "Entitlement Grant");
    }

    @Override
    public TalentTypeHandler createTypeHandler(Persona persona) {
        return new EntitlementGrantTalentTypeHandler(persona);
    }

    @Override
    public MapCodec<EntitlementGrantTalentNode> codec() {
        return EntitlementGrantTalentNode.MAP_CODEC;
    }

    @Override
    public MutableComponent getTalentNodeName(TalentRecord record) {
        if (!(record.getNode() instanceof EntitlementGrantTalentNode entitlementNode)) {
            return Component.literal("bad grant node type");
        }
        return entitlementNode.getEntitlement().value().getName();
    }

    @Override
    public MutableComponent getTalentDescription(TalentRecord record) {
        if (!(record.getNode() instanceof EntitlementGrantTalentNode entitlementNode)) {
            return Component.literal("bad grant node type");
        }
        return entitlementNode.getEntitlement().value().getDescription();
    }
}
