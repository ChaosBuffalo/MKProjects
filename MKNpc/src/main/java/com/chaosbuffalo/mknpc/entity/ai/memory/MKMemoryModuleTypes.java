package com.chaosbuffalo.mknpc.entity.ai.memory;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MKMemoryModuleTypes {

    private static final DeferredRegister<MemoryModuleType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, MKNpc.MODID);

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> ALLIES = REGISTRY.register("allies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> ENEMIES = REGISTRY.register("enemies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> VISIBLE_ENEMIES = REGISTRY.register("visible_enemies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Map<LivingEntity, ThreatMapEntry>>> THREAT_MAP = REGISTRY.register("threat_map",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> THREAT_LIST = REGISTRY.register("threat_list",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> THREAT_TARGET = REGISTRY.register("threat_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<MovementStrategy>> MOVEMENT_STRATEGY = REGISTRY.register("movement_strategy",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> MOVEMENT_TARGET = REGISTRY.register("movement_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<MKAbilityInfo>> CURRENT_ABILITY = REGISTRY.register("current_ability",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<BlockPos>> SPAWN_POINT = REGISTRY.register("spawn_point",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> IS_RETURNING = REGISTRY.register("is_returning",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Integer>> ABILITY_TIMEOUT = REGISTRY.register("ability_timeout",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
