package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.*;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.sync.controllers.SyncController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class MKEntityData implements IMKEntityData {

    private final LivingEntity entity;
    private final AbilityExecutor abilityExecutor;
    private final MobStats stats;
    private final Lazy<EntityEquipment> equipment;
    private final MobAbilityKnowledge abilities;
    private final CombatExtensionModule combatExtensionModule;
    private final EntityEffectHandler effectHandler;
    private final EntityPetModule pets;
    private final EntityRiderModule riders;
    @Nullable
    private ParticleEffectInstanceTracker instanceTracker = null;

    public MKEntityData(LivingEntity livingEntity) {
        entity = Objects.requireNonNull(livingEntity);
        abilities = new MobAbilityKnowledge(this);
        abilityExecutor = new AbilityExecutor(this);
        stats = new MobStats(this);
        equipment = Lazy.of(() -> new EntityEquipment(this));
        combatExtensionModule = new CombatExtensionModule(this);
        effectHandler = new EntityEffectHandler(this);
        pets = new EntityPetModule(this);
        riders = new EntityRiderModule(this);
    }

    @Nonnull
    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public AbilityExecutor getAbilityExecutor() {
        return abilityExecutor;
    }

    @Override
    public MobAbilityKnowledge getAbilities() {
        return abilities;
    }

    @Override
    public MobStats getStats() {
        return stats;
    }

    @Override
    public CombatExtensionModule getCombatExtension() {
        return combatExtensionModule;
    }

    @Override
    public EntityEffectHandler getEffects() {
        return effectHandler;
    }

    @Override
    public EntityEquipment getEquipment() {
        return equipment.get();
    }

    public void setInstanceTracker(@Nullable ParticleEffectInstanceTracker instanceTracker) {
        this.instanceTracker = instanceTracker;
    }

    @Override
    public Optional<ParticleEffectInstanceTracker> getParticleEffectTracker() {
        return Optional.ofNullable(instanceTracker);
    }

    @Override
    public void onJoinWorld() {
        if (isServerSide()) {
            getEffects().onJoinWorld();
        }
    }

    public void update() {
        getEffects().tick();
        getAbilityExecutor().tick();
        getStats().tick();
        getCombatExtension().tick();
    }

    @Override
    public EntityPetModule getPets() {
        return pets;
    }

    @Override
    public EntityRiderModule getRiders() {
        return riders;
    }

    @Override
    public void onPlayerStartTracking(ServerPlayer playerEntity) {
        getEffects().sendAllEffectsToPlayer(playerEntity);
    }

    @Override
    public void attachUpdateEngine(SyncController engine) {
        pets.getSyncComponent().attach(engine);
        stats.getSyncComponent().attach(engine);
        riders.getSyncComponent().attach(engine);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("abilities", abilities.serialize());
        tag.put("effects", effectHandler.serialize());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        abilities.deserialize(nbt.getCompound("abilities"));
        effectHandler.deserialize(nbt.getCompound("effects"));
    }
}
