package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public abstract class MKEntitlement {
    public static final Codec<MKEntitlement> DIRECT_CODEC = MKCoreRegistry.ENTITLEMENT_TYPES.byNameCodec()
            .dispatch(MKEntitlement::getEntitlementType, EntitlementType::codec);

    public static final Codec<Holder<MKEntitlement>> CODEC = RegistryFileCodec.create(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY, DIRECT_CODEC);
    public static final Codec<Holder<MKEntitlement>> REFERENCE_CODEC = RegistryFixedCodec.create(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);
    public static final Codec<ResourceKey<MKEntitlement>> KEY_CODEC = ResourceKey.codec(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

    protected final Component displayName;
    protected final Component description;
    private final int maxEntitlements;

    public MKEntitlement(int maxEntitlements) {
        this.maxEntitlements = maxEntitlements;
        displayName = Component.empty();
        description = Component.empty();
    }

    public MKEntitlement(Component displayName, Component description, int maxEntitlements) {
        this.displayName = displayName;
        this.description = description;
        this.maxEntitlements = maxEntitlements;
    }

    public int getMaxEntitlements() {
        return maxEntitlements;
    }

    public abstract EntitlementType<?> getEntitlementType();

    public MutableComponent getName() {
        return displayName.copy();
    }

    public MutableComponent getDescription() {
        return description.copy();
    }

    public static String nameKey(ResourceLocation id) {
        return id.toLanguageKey("entitlement", "name");
    }

    public static String descriptionKey(ResourceLocation id) {
        return id.toLanguageKey("entitlement", "description");
    }

}
