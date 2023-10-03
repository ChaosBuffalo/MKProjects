package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.core.entity.EntityEffectHandler;
import com.chaosbuffalo.mkcore.core.pets.EntityPetModule;
import com.chaosbuffalo.mkcore.core.player.ParticleEffectInstanceTracker;
import com.chaosbuffalo.mkcore.sync.UpdateEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface IMKEntityData extends INBTSerializable<CompoundTag> {

    @Nonnull
    LivingEntity getEntity();

    default boolean isServerSide() {
        return !getEntity().getCommandSenderWorld().isClientSide();
    }

    default boolean isClientSide() {
        return getEntity().getCommandSenderWorld().isClientSide();
    }

    AbilityExecutor getAbilityExecutor();

    IMKAbilityKnowledge getAbilities();

    Optional<ParticleEffectInstanceTracker> getParticleEffectTracker();

    IMKEntityStats getStats();

    CombatExtensionModule getCombatExtension();

    EntityEffectHandler getEffects();

    EntityPetModule getPets();

    void onJoinWorld();

    void onPlayerStartTracking(ServerPlayer playerEntity);

    void attachUpdateEngine(UpdateEngine engine);
}
