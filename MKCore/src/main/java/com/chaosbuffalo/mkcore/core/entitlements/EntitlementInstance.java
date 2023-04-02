package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.records.IRecordInstance;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class EntitlementInstance implements IRecordInstance {

    protected MKEntitlement entitlement;
    protected UUID uuid;

    public EntitlementInstance(Dynamic<?> dynamic) {
        deserializeDynamic(dynamic);
    }

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

    public boolean isValid() {
        return uuid != null && entitlement != null;
    }

    @Override
    public IRecordType<?> getRecordType() {
        return entitlement.getRecordType();
    }

    public <T> T serializeDynamic(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        ResourceLocation id = entitlement != null && entitlement.getId() != null ?
                entitlement.getId() :
                MKCoreRegistry.INVALID_ENTITLEMENT;

        builder.put(ops.createString("entitlement"), ops.createString(id.toString()));
        if (uuid != null) {
            builder.put(ops.createString("entitlementId"), ops.createString(uuid.toString()));
        }
        return ops.createMap(builder.build());
    }

    public <T> void deserializeDynamic(Dynamic<T> dynamic) {
        ResourceLocation loc = dynamic.get("entitlement").asString().map(ResourceLocation::new).result()
                .orElse(MKCoreRegistry.INVALID_ENTITLEMENT);
        this.entitlement = MKCoreRegistry.getEntitlement(loc);
        this.uuid = dynamic.get("entitlementId").asString().map(UUID::fromString).result().orElse(null);
    }

    @Override
    public String toString() {
        return "EntitlementInstance{" +
                "entitlement=" + entitlement +
                ", uuid=" + uuid +
                '}';
    }
}
