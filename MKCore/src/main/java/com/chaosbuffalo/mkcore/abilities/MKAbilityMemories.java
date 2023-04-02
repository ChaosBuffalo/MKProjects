package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class MKAbilityMemories {

    public static DeferredRegister<MemoryModuleType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, MKCore.MOD_ID);

    public static RegistryObject<MemoryModuleType<LivingEntity>> ABILITY_TARGET = REGISTRY.register("ability_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<TargetUtil.LivingOrPosition>> ABILITY_POSITION_TARGET = REGISTRY.register("ability_position_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}

