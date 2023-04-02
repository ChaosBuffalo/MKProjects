package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.core.entity.EntityStats;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MKEntityData implements IMKEntityData {

    private final LivingEntity entity;
    private final AbilityExecutor abilityExecutor;
    private final EntityStats stats;
    private final EntityAbilityKnowledge knowledge;
    private final CombatExtensionModule combatExtensionModule;
    private final EntityEffectHandler effectHandler;
    private final EntityPetModule pets;

    public MKEntityData(LivingEntity livingEntity) {
        entity = Objects.requireNonNull(livingEntity);
        knowledge = new EntityAbilityKnowledge(this);
        abilityExecutor = new AbilityExecutor(this);
        stats = new EntityStats(this);
        combatExtensionModule = new CombatExtensionModule(this);
        effectHandler = new EntityEffectHandler(this);
        pets = new EntityPetModule(this);
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
    public EntityAbilityKnowledge getKnowledge() {
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
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("knowledge", getKnowledge().serialize());
        tag.put("effects", effectHandler.serialize());
        return tag;
    }

    @Override
    public EntityPetModule getPets() {
        return pets;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        getKnowledge().deserialize(tag.getCompound("knowledge"));
        effectHandler.deserialize(tag.getCompound("effects"));
    }

    @Override
    public void onPlayerStartTracking(ServerPlayer playerEntity) {
        getEffects().sendAllEffectsToPlayer(playerEntity);
    }

    @Override
    public void attachUpdateEngine(UpdateEngine engine) {
        pets.getSyncComponent().attach(engine);
    }
}
