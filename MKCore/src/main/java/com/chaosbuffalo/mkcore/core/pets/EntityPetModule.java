package com.chaosbuffalo.mkcore.core.pets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.sync.types.SyncEntity;
import com.chaosbuffalo.mkcore.sync.adapters.SyncMapUpdater;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityPetModule implements ISyncGroupProvider {
    private final SyncGroup syncGroup = new SyncGroup();
    protected final IMKEntityData entityData;
    protected final SyncEntity<LivingEntity> owner = new SyncEntity<>();
    protected final Map<ResourceLocation, MKPet<?>> pets = new HashMap<>();
    protected final Map<ResourceLocation, MKPet.ClientMKPet> clientPetMap = new HashMap<>();
    protected final SyncMapUpdater<ResourceLocation, MKPet.ClientMKPet> clientPets = new SyncMapUpdater<>(
            clientPetMap, ResourceLocation::toString, ResourceLocation::tryParse, EntityPetModule::createClientPet);

    private static MKPet.ClientMKPet createClientPet(ResourceLocation petId) {
        return new MKPet.ClientMKPet(petId, null);
    }

    public EntityPetModule(IMKEntityData entityData) {
        this.entityData = entityData;
        syncGroup.addPublic("owner", owner);
        syncGroup.addPublic("clientPets", clientPets);
    }

    public void addPet(MKPet<?> pet) {
        if (pet.isActive()) {
            MKCore.getEntityData(pet.getEntity()).ifPresent(x -> x.getPets().setOwner(entityData.getEntity()));
            pets.put(pet.getName(), pet);
            clientPetMap.put(pet.getName(), pet.getClientPet());
            clientPets.markDirty(pet.getName());
        } else {
            MKCore.LOGGER.debug("Tried to add invalid pet {} to {}", pet.getName(), entityData.getEntity());
        }

    }

    public void addThreatToPets(LivingEntity source, float threatValue, boolean propagate) {
        pets.values().forEach(x -> x.addThreat(source, threatValue, propagate));
    }

    public void tick() {
        clientPetMap.values().forEach(MKPet.ClientMKPet::tick);
        List<MKPet<?>> expired = null;
        for (MKPet<?> pet : pets.values()) {
            if (pet.tick()) {
                if (expired == null) expired = new ArrayList<>();
                expired.add(pet);
            }
        }
        if (expired != null) {
            for (MKPet<?> pet : expired) {
                removePet(pet);
            }
        }
    }

    public void removePet(MKPet<?> pet) {
        pets.remove(pet.getName());
        clientPetMap.remove(pet.getName());
        clientPets.markDirty(pet.getName());

    }

    public Map<ResourceLocation, MKPet.ClientMKPet> getClientPets() {
        return clientPetMap;
    }

    public boolean hasPet() {
        for (MKPet<?> pet : pets.values()) {
            if (pet.isActive()) return true;
        }
        return false;
    }

    public boolean isPetActive(ResourceLocation name) {
        return pets.containsKey(name) && pets.get(name).isActive();
    }

    public Optional<MKPet<?>> getPet(ResourceLocation name) {
        return Optional.ofNullable(pets.get(name));
    }

    public boolean isPet() {
        return owner.hasEntity();
    }

    public void setOwner(LivingEntity owner) {
        this.owner.set(owner);
    }

    @Nullable
    public LivingEntity getOwner() {
        return owner.get(entityData.getEntity().level(), LivingEntity.class);
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    public void onDeath(Entity.RemovalReason reason) {
        pets.values().forEach(x -> {
            if (x.getEntity() != null) {
                x.getEntity().remove(reason);
            }
        });
        pets.clear();
    }
}
