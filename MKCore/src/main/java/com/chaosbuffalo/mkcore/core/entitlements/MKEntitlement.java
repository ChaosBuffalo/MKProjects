package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class MKEntitlement {
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

    public abstract EntitlementType getEntitlementType();

    public Component getDescription() {
        ResourceLocation id = getId();
        return Component.translatable(String.format("%s.entitlement.%s.name", id.getNamespace(), id.getPath()));
    }
}
