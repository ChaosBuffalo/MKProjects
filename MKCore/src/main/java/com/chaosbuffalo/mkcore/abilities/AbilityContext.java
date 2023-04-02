package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class AbilityContext {
    public static final AbilityContext EMPTY = new AbilityContext(ImmutableMap.of());

    private final Map<MemoryModuleType<?>, Optional<?>> memories;

    public AbilityContext() {
        memories = new HashMap<>();
    }

    private AbilityContext(Map<MemoryModuleType<?>, Optional<?>> memories) {
        this.memories = memories;
    }

    public <U> void setMemory(MemoryModuleType<U> memoryType,
                              @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<U> value) {
        memories.put(memoryType, value);
    }

    public <U> AbilityContext withMemory(MemoryModuleType<U> memoryType,
                                         @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<U> value) {
        setMemory(memoryType, value);
        return this;
    }

    public <U> AbilityContext withMemory(Supplier<MemoryModuleType<U>> memoryType,
                                         @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<U> value) {
        setMemory(memoryType.get(), value);
        return this;
    }

    public <U> AbilityContext withBrainMemory(LivingEntity entity, MemoryModuleType<U> memoryType) {
        Optional<U> value = entity.getBrain().getMemory(memoryType);
        setMemory(memoryType, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getMemory(MemoryModuleType<T> memory) {
        return (Optional<T>) memories.get(memory);
    }

    public <T> Optional<T> getMemory(Supplier<MemoryModuleType<T>> memory) {
        return getMemory(memory.get());
    }

    public <T> boolean hasMemory(MemoryModuleType<T> memory) {
        Optional<T> type = getMemory(memory);
        return type != null && type.isPresent();
    }

    public static AbilityContext singleTarget(LivingEntity target) {
        return new AbilityContext().withMemory(MKAbilityMemories.ABILITY_TARGET, Optional.ofNullable(target));
    }

    public static AbilityContext selfTarget(IMKEntityData targetData) {
        return singleTarget(targetData.getEntity());
    }

    public static AbilityContext singleOrPositionTarget(TargetUtil.LivingOrPosition position) {
        return new AbilityContext().withMemory(MKAbilityMemories.ABILITY_POSITION_TARGET, Optional.ofNullable(position));
    }
}
