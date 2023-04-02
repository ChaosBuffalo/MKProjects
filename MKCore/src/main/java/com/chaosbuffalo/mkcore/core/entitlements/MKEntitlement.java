package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class MKEntitlement extends ForgeRegistryEntry<MKEntitlement> {
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

    public abstract IRecordType<?> getRecordType();

    public Component getDescription() {
        ResourceLocation id = getId();
        return new TranslatableComponent(String.format("%s.entitlement.%s.name", id.getNamespace(), id.getPath()));
    }
}
