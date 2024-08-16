package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

public class MKAbilityMemories {

    public static DeferredRegister<MemoryModuleType<?>> REGISTRY =
            DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MKCore.MOD_ID);

    public static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LivingEntity>> ABILITY_TARGET = REGISTRY.register("ability_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<TargetUtil.LivingOrPosition>> ABILITY_POSITION_TARGET = REGISTRY.register("ability_position_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<BaseProjectileEntity>>> CURRENT_PROJECTILES = REGISTRY.register("current_projectiles",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}

