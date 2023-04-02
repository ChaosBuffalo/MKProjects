package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public interface IMKEntityData {

    @Nonnull
    LivingEntity getEntity();

    default boolean isServerSide() {
        return !getEntity().getCommandSenderWorld().isClientSide();
    }

    AbilityExecutor getAbilityExecutor();

    IMKEntityKnowledge getKnowledge();

    IMKEntityStats getStats();

    CombatExtensionModule getCombatExtension();

    EntityEffectHandler getEffects();

    CompoundTag serialize();

    EntityPetModule getPets();

    void deserialize(CompoundTag nbt);

    void onJoinWorld();

    void onPlayerStartTracking(ServerPlayer playerEntity);

    void attachUpdateEngine(UpdateEngine engine);
}
