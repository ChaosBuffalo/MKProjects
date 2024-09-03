package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.PlayerRecordDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerEntitlements {
    private final Map<UUID, EntitlementInstance> entitlements = new HashMap<>();
    private final PlayerRecordDispatcher<EntitlementInstance> dispatcher;

    public PlayerEntitlements(Persona persona) {
        dispatcher = new PlayerRecordDispatcher<>(persona, this::getInstanceStream);
    }

    public Stream<EntitlementInstance> getInstanceStream() {
        return entitlements.values().stream();
    }

    public boolean hasEntitlement(MKEntitlement entitlement) {
        return getEntitlementLevel(entitlement) > 0;
    }

    public void addEntitlement(EntitlementInstance instance) {
        if (!entitlements.containsKey(instance.instanceId())) {
            entitlements.put(instance.instanceId(), instance);
            onInstanceChanged(instance);
        } else {
            MKCore.LOGGER.error("Trying to add invalid entitlement or already added entitlement: {}", instance);
        }
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
                .filter(instance -> instance.entitlement() == entitlement)
                .limit(entitlement.getMaxEntitlements())
                .count();
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag entitlementsTag = new ListTag();
        for (EntitlementInstance instance : entitlements.values()) {
            EntitlementInstance.CODEC.encodeStart(NbtOps.INSTANCE, instance)
                    .resultOrPartial(MKCore.LOGGER::error)
                    .ifPresent(entitlementsTag::add);
        }
        tag.put("entitlements", entitlementsTag);
        return tag;
    }

    public boolean deserialize(CompoundTag tag) {
        entitlements.clear();
        ListTag entitlementsTag = tag.getList("entitlements", Tag.TAG_COMPOUND);
        for (Tag entNbt : entitlementsTag) {
            EntitlementInstance.CODEC.parse(NbtOps.INSTANCE, entNbt)
                    .resultOrPartial(MKCore.LOGGER::error)
                    .ifPresent(e -> entitlements.put(e.instanceId(), e));
        }
        return true;
    }

    protected void onInstanceChanged(EntitlementInstance instance) {
        dispatcher.onRecordUpdated(instance);
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerEntitlements.onPersonaActivated");
        dispatcher.onPersonaActivated();
    }
}
