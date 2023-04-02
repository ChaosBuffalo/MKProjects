package com.chaosbuffalo.mkcore.core.pets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.SyncComponent;
import com.chaosbuffalo.mkcore.sync.SyncBool;
import com.chaosbuffalo.mkcore.sync.SyncEntity;
import com.chaosbuffalo.mkcore.sync.SyncMapUpdater;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntityPetModule implements IPlayerSyncComponentProvider {
    private final SyncComponent sync = new SyncComponent("petModule");
    protected final IMKEntityData entityData;
    protected final SyncBool isPet = new SyncBool("isPet", false);
    protected final SyncEntity<LivingEntity> owner = new SyncEntity<>("owner", null, LivingEntity.class);
    protected final Map<ResourceLocation, MKPet<?>> pets = new HashMap<>();
    protected final Map<ResourceLocation, MKPet.ClientMKPet> clientPetMap = new HashMap<>();
    protected final SyncMapUpdater<ResourceLocation, MKPet.ClientMKPet> clientPets = new SyncMapUpdater<>("clientPets",
            () -> clientPetMap, ResourceLocation::toString, ResourceLocation::tryParse, EntityPetModule::createClientPet);

    private static MKPet.ClientMKPet createClientPet(ResourceLocation petId) {
        return new MKPet.ClientMKPet(petId, null);
    }

    public EntityPetModule(IMKEntityData entityData) {
        this.entityData = entityData;
        addSyncPublic(owner);
        addSyncPublic(isPet);
        addSyncPublic(clientPets);
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
        pets.values().stream().filter(MKPet::tick).collect(Collectors.toList()).forEach(this::removePet);
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
        return pets.values().stream().anyMatch(MKPet::isActive);
    }

    public boolean isPetActive(ResourceLocation name) {
        return pets.containsKey(name) && pets.get(name).isActive();
    }

    public Optional<MKPet<?>> getPet(ResourceLocation name) {
        return Optional.ofNullable(pets.get(name));
    }

    public boolean isPet() {
        return isPet.get();
    }

    public void setOwner(LivingEntity owner) {
        isPet.set(true);
        this.owner.set(owner);
    }

    @Nullable
    public LivingEntity getOwner() {
        return owner.get();
    }

    @Override
    public SyncComponent getSyncComponent() {
        return sync;
    }

    public void onDeath() {
        pets.values().forEach(x -> {
            if (x.getEntity() != null) {
                x.getEntity().remove(Entity.RemovalReason.KILLED);
            }
        });
    }
}
