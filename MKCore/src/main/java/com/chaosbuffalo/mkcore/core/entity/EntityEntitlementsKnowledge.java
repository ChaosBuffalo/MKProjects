package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class EntityEntitlementsKnowledge {
    protected final IMKEntityData entityData;
    protected final Map<UUID, EntitlementInstance> entitlements = new HashMap<>();

    public EntityEntitlementsKnowledge(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    public boolean hasEntitlement(MKEntitlement entitlement) {
        return getEntitlementLevel(entitlement) > 0;
    }

    public void addEntitlement(EntitlementInstance instance) {
        addEntitlementInternal(instance, true);
    }

    protected void addEntitlementInternal(EntitlementInstance instance, boolean doBroadcast) {
        if (instance.isValid() && !entitlements.containsKey(instance.getUUID())) {
            entitlements.put(instance.getUUID(), instance);
            if (doBroadcast) {
                onInstanceChanged(instance);
            }
        } else {
            MKCore.LOGGER.error("Trying to add invalid entitlement or already added entitlement: {}", instance);
        }
    }

    public Stream<EntitlementInstance> getInstanceStream() {
        return entitlements.values().stream();
    }

    public void removeEntitlement(UUID id) {
        EntitlementInstance existing = entitlements.remove(id);
        if (existing != null) {
            onInstanceChanged(existing);
        } else {
            MKCore.LOGGER.error("Trying to remove entitlement with id {} but it doesn't exist", id);
        }
    }

    public int getEntitlementLevel(MKEntitlement entitlement) {
        return (int) getInstanceStream()
                .filter(instance -> instance.getEntitlement() == entitlement)
                .limit(entitlement.getMaxEntitlements())
                .count();
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag entitlementsTag = new ListTag();
        for (EntitlementInstance instance : entitlements.values()) {
            entitlementsTag.add(instance.serializeDynamic(NbtOps.INSTANCE));
        }
        tag.put("entitlements", entitlementsTag);
        return tag;
    }

    public boolean deserialize(CompoundTag tag) {
        entitlements.clear();
        ListTag entitlementsTag = tag.getList("entitlements", Tag.TAG_COMPOUND);
        for (Tag entNbt : entitlementsTag) {
            EntitlementInstance newEnt = new EntitlementInstance(new Dynamic<>(NbtOps.INSTANCE, entNbt));
            if (newEnt.isValid()) {
                addEntitlementInternal(newEnt, false);
            }
        }
        return true;
    }

    protected void onInstanceChanged(EntitlementInstance instance) {

    }
}
