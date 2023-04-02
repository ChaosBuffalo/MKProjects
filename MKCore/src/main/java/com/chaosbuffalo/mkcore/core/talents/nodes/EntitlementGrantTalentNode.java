package com.chaosbuffalo.mkcore.core.talents.nodes;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.talent_types.EntitlementGrantTalent;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class EntitlementGrantTalentNode extends TalentNode {

    private final UUID nodeId;

    public EntitlementGrantTalentNode(EntitlementGrantTalent talent, Dynamic<?> entry) {
        super(talent, entry);
        this.nodeId = entry.get("nodeId").asString().map(UUID::fromString).result().orElse(UUID.randomUUID());
    }

    public EntitlementGrantTalentNode(Supplier<EntitlementGrantTalent> talent, UUID nodeId) {
        super(talent.get());
        this.nodeId = nodeId;
    }

    @Override
    public EntitlementGrantTalent getTalent() {
        return (EntitlementGrantTalent) super.getTalent();
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public <T> T serialize(DynamicOps<T> ops) {
        T value = super.serialize(ops);
        Optional<T> merged = ops.mergeToMap(value, ops.createString("nodeId"), ops.createString(nodeId.toString())).resultOrPartial(MKCore.LOGGER::error);
        return merged.orElse(value);
    }
}
