package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public record EntitlementInstance(MKEntitlement entitlement, UUID instanceId)
        implements IRecordInstance<EntitlementInstance> {

    public static final Codec<EntitlementInstance> CODEC = RecordCodecBuilder.<EntitlementInstance>mapCodec(builder ->
                    builder.group(
                            MKCoreRegistry.ENTITLEMENTS.getCodec().fieldOf("entitlement").forGetter(EntitlementInstance::entitlement),
                            UUIDUtil.STRING_CODEC.fieldOf("instanceId").forGetter(EntitlementInstance::instanceId)
                    ).apply(builder, EntitlementInstance::new))
            .codec();

    @Override
    public EntitlementType getRecordType() {
        return entitlement.getEntitlementType();
    }
}
