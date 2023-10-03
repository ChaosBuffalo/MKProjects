package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.core.entity.EntityStats;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class MKEntityData implements IMKEntityData {

    private final LivingEntity entity;
    private final AbilityExecutor abilityExecutor;
    private final EntityStats stats;
    private final EntityAbilityKnowledge knowledge;
    private final CombatExtensionModule combatExtensionModule;
    private final EntityEffectHandler effectHandler;
    private final EntityPetModule pets;

    @Nullable
    private ParticleEffectInstanceTracker instanceTracker = null;

    public MKEntityData(LivingEntity livingEntity) {
        entity = Objects.requireNonNull(livingEntity);
        knowledge = new EntityAbilityKnowledge(this);
        abilityExecutor = new AbilityExecutor(this);
        stats = new EntityStats(this);
        combatExtensionModule = new CombatExtensionModule(this);
        effectHandler = new EntityEffectHandler(this);
        pets = new EntityPetModule(this);
    }

    public void setInstanceTracker(ParticleEffectInstanceTracker instanceTracker) {
        this.instanceTracker = instanceTracker;
    }

    @Override
    public Optional<ParticleEffectInstanceTracker> getParticleEffectTracker() {
        return Optional.ofNullable(instanceTracker);
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
    public EntityAbilityKnowledge getAbilities() {
        return knowledge;
    }

    @Override
    public EntityStats getStats() {
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
    public void onJoinWorld() {
        getEffects().onJoinWorld();
    }

    public void update() {
        getEntity().getCommandSenderWorld().getProfiler().push("MKEntityData.update");

        getEntity().getCommandSenderWorld().getProfiler().push("EntityEffects.tick");
        getEffects().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("AbilityExecutor.tick");
        getAbilityExecutor().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("EntityStats.tick");
        getStats().tick();
        getEntity().getCommandSenderWorld().getProfiler().popPush("EntityCombat.tick");
        getCombatExtension().tick();
        getEntity().getCommandSenderWorld().getProfiler().pop();

        getEntity().getCommandSenderWorld().getProfiler().pop();
    }

    @Override
    public EntityPetModule getPets() {
        return pets;
    }

    @Override
    public void onPlayerStartTracking(ServerPlayer playerEntity) {
        getEffects().sendAllEffectsToPlayer(playerEntity);
    }

    @Override
    public void attachUpdateEngine(UpdateEngine engine) {
        pets.getSyncComponent().attach(engine);
        stats.getSyncComponent().attach(engine);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("knowledge", knowledge.serialize());
        tag.put("effects", effectHandler.serialize());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        knowledge.deserialize(nbt.getCompound("knowledge"));
        effectHandler.deserialize(nbt.getCompound("effects"));
    }
}
