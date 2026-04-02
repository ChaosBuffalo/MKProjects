package com.chaosbuffalo.mkcore.core.talents.nodes;

import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.core.talents.TalentType;
import com.chaosbuffalo.mkcore.init.CoreTalentTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public class EntitlementGrantTalentNode extends TalentNode {
    public static final MapCodec<EntitlementGrantTalentNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MKEntitlement.REFERENCE_CODEC.fieldOf("entitlement").forGetter(EntitlementGrantTalentNode::getEntitlement),
            TalentNodeDisplay.REFERENCE_CODEC.fieldOf("display_info").forGetter(i -> i.displayHolder),
            UUIDUtil.STRING_CODEC.fieldOf("nodeId").forGetter(EntitlementGrantTalentNode::getNodeId)
    ).apply(builder, EntitlementGrantTalentNode::new));

    private final UUID nodeId;
    private final Holder<MKEntitlement> entitlement;

    public EntitlementGrantTalentNode(Holder<MKEntitlement> entitlement, Holder<TalentNodeDisplay> displayHolder, UUID nodeId) {
        super(displayHolder, 1);
        this.nodeId = nodeId;
        this.entitlement = entitlement;
    }

    @Override
    public TalentType<EntitlementGrantTalentNode> getType() {
        return CoreTalentTypes.ENTITLEMENT_GRANT.get();
    }

    public Holder<MKEntitlement> getEntitlement() {
        return entitlement;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public EntitlementInstance createInstance() {
        return new EntitlementInstance(entitlement, nodeId, false);
    }
}
