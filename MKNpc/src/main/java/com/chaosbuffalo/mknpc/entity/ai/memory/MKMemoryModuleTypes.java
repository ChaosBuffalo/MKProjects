package com.chaosbuffalo.mknpc.entity.ai.memory;


import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.ai.movement_strategy.MovementStrategy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKMemoryModuleTypes {

    @ObjectHolder("mknpc:allies")
    public static MemoryModuleType<List<LivingEntity>> ALLIES;

    @ObjectHolder("mknpc:enemies")
    public static MemoryModuleType<List<LivingEntity>> ENEMIES;

    @ObjectHolder("mknpc:visible_enemies")
    public static MemoryModuleType<List<LivingEntity>> VISIBLE_ENEMIES;

    @ObjectHolder("mknpc:threat_map")
    public static MemoryModuleType<Map<LivingEntity, ThreatMapEntry>> THREAT_MAP;

    @ObjectHolder("mknpc:threat_list")
    public static MemoryModuleType<List<LivingEntity>> THREAT_LIST;

    @ObjectHolder("mknpc:threat_target")
    public static MemoryModuleType<LivingEntity> THREAT_TARGET;

    @ObjectHolder("mknpc:movement_strategy")
    public static MemoryModuleType<MovementStrategy> MOVEMENT_STRATEGY;

    @ObjectHolder("mknpc:movement_target")
    public static MemoryModuleType<LivingEntity> MOVEMENT_TARGET;

    @ObjectHolder("mknpc:current_ability")
    public static MemoryModuleType<MKAbility> CURRENT_ABILITY;

    @ObjectHolder("mknpc:spawn_point")
    public static MemoryModuleType<BlockPos> SPAWN_POINT;

    @ObjectHolder("mknpc:is_returning")
    public static MemoryModuleType<Boolean> IS_RETURNING;

    @ObjectHolder("mknpc:ability_timeout")
    public static MemoryModuleType<Integer> ABILITY_TIMEOUT;


    @SubscribeEvent
    public static void registerModuleTypes(RegistryEvent.Register<MemoryModuleType<?>> evt) {
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "allies"));
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "enemies"));
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "visible_enemies"));
        evt.getRegistry().register(new MemoryModuleType<Map<LivingEntity, ThreatMapEntry>>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "threat_map"));
        evt.getRegistry().register(new MemoryModuleType<List<LivingEntity>>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "threat_list"));
        evt.getRegistry().register(new MemoryModuleType<LivingEntity>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "threat_target"));
        evt.getRegistry().register(new MemoryModuleType<MovementStrategy>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "movement_strategy"));
        evt.getRegistry().register(new MemoryModuleType<LivingEntity>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "movement_target"));
        evt.getRegistry().register(new MemoryModuleType<MKAbility>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "current_ability"));
        evt.getRegistry().register(new MemoryModuleType<BlockPos>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "spawn_point"));
        evt.getRegistry().register(new MemoryModuleType<Boolean>(Optional.empty())
                .setRegistryName(MKNpc.MODID, "is_returning"));
        evt.getRegistry().register(new MemoryModuleType<Integer>(Optional.empty())
            .setRegistryName(MKNpc.MODID, "ability_timeout"));
    }


}
