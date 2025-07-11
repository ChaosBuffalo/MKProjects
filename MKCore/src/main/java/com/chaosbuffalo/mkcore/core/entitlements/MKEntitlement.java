package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    public MutableComponent getName() {
        return Component.translatable(nameKey(getId()));
    }

    public MutableComponent getDescription() {
        ResourceLocation id = getId();
        return Component.translatable(descriptionKey(id));
    }

    public static String nameKey(ResourceLocation id) {
        return id.toLanguageKey("entitlement", "name");
    }

    public static String descriptionKey(ResourceLocation id) {
        return id.toLanguageKey("entitlement", "description");
    }

}
