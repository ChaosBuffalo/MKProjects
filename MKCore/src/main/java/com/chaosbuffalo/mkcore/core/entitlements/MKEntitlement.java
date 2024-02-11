package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class MKEntitlement implements IRecordType<EntitlementInstance> {
    private final int maxEntitlements;

    public MKEntitlement(int maxEntitlements) {
        this.maxEntitlements = maxEntitlements;
    }

    public ResourceLocation getId() {
        return MKCoreRegistry.ENTITLEMENTS.getKey(this);
    }

    public int getMaxEntitlements() {
        return maxEntitlements;
    }

    @Override
    public abstract EntitlementTypeHandler createTypeHandler(MKPlayerData playerData);

    public Component getDescription() {
        ResourceLocation id = getId();
        return Component.translatable(String.format("%s.entitlement.%s.name", id.getNamespace(), id.getPath()));
    }
}
