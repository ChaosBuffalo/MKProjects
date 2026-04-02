package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record EntitlementInstance(Holder<MKEntitlement> entitlement, UUID instanceId, boolean persistent)
        implements IRecordInstance<EntitlementInstance> {

    public static final Codec<EntitlementInstance> CODEC = RecordCodecBuilder.<EntitlementInstance>mapCodec(builder -> builder.group(
            MKEntitlement.REFERENCE_CODEC.fieldOf("entitlement").forGetter(EntitlementInstance::entitlement),
            UUIDUtil.STRING_CODEC.fieldOf("instanceId").forGetter(EntitlementInstance::instanceId),
            Codec.BOOL.fieldOf("persistent").forGetter(EntitlementInstance::persistent)
    ).apply(builder, EntitlementInstance::new)).codec();

    public EntitlementInstance(Holder<MKEntitlement> entitlement, UUID instanceId) {
        this(entitlement, instanceId, true);
    }

    @Override
    public EntitlementType<?> getRecordType() {
        return entitlement.value().getEntitlementType();
    }
}
