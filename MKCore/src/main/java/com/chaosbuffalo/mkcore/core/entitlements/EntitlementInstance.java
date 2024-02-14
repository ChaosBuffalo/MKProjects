package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public class EntitlementInstance implements IRecordInstance<EntitlementInstance> {

    public static final Codec<EntitlementInstance> CODEC = RecordCodecBuilder.<EntitlementInstance>mapCodec(builder ->
                    builder.group(
                            MKCoreRegistry.ENTITLEMENTS.getCodec().fieldOf("entitlement").forGetter(EntitlementInstance::getEntitlement),
                            UUIDUtil.STRING_CODEC.fieldOf("instanceId").forGetter(EntitlementInstance::getUUID)
                    ).apply(builder, EntitlementInstance::new))
            .codec();


    protected final MKEntitlement entitlement;
    protected final UUID uuid;

    public EntitlementInstance(MKEntitlement entitlement, UUID uuid) {
        this.entitlement = entitlement;
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public MKEntitlement getEntitlement() {
        return entitlement;
    }

    @Override
    public EntitlementType getRecordType() {
        return entitlement.getEntitlementType();
    }

    @Override
    public String toString() {
        return "EntitlementInstance{" +
                "entitlement=" + entitlement +
                ", uuid=" + uuid +
                '}';
    }
}
