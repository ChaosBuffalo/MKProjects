package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbilityContext {
    public static final AbilityContext EMPTY = new AbilityContext(ImmutableMap.of());

    private final Map<MemoryModuleType<?>, Optional<?>> memories;
    private IMKEntityData casterData;

    public AbilityContext(IMKEntityData entityData) {
        memories = new HashMap<>();
        this.casterData = entityData;
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


    private BiFunction<IMKEntityData, Attribute, Float> supplier;

    public float getSkill(Attribute attribute) {
        if (supplier != null) {
            return supplier.apply(casterData, attribute);
        }
        return MKAbility.getSkillLevel(casterData.getEntity(), attribute);
    }

    public void setSkillResolver(BiFunction<IMKEntityData, Attribute, Float> supplier) {
        this.supplier = supplier;
    }

    public static AbilityContext forTooltip(IMKEntityData casterData) {
        return new AbilityContext(casterData);
    }

    public static AbilityContext singleTarget(IMKEntityData casterData, LivingEntity target) {
        return new AbilityContext(casterData).withMemory(MKAbilityMemories.ABILITY_TARGET, Optional.ofNullable(target));
    }

    public static AbilityContext selfTarget(IMKEntityData targetData) {
        return singleTarget(targetData, targetData.getEntity());
    }

    public static AbilityContext singleOrPositionTarget(IMKEntityData casterData, TargetUtil.LivingOrPosition position) {
        return new AbilityContext(casterData).withMemory(MKAbilityMemories.ABILITY_POSITION_TARGET, Optional.ofNullable(position));
    }
}
