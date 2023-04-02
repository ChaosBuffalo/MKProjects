package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IMKEntityEntitlements extends IMKSerializable<CompoundTag> {

    boolean hasEntitlement(MKEntitlement entitlement);

    void addEntitlement(EntitlementInstance instance);

    void removeEntitlement(EntitlementInstance instance);

    void removeEntitlementByUUID(UUID id);

    int getEntitlementLevel(MKEntitlement entitlement);

    void addUpdatedCallback(BiConsumer<EntitlementInstance, IMKEntityEntitlements> entitlementConsumer);

    void addLoadedCallback(Consumer<IMKEntityEntitlements> loadedConsumer);
}
