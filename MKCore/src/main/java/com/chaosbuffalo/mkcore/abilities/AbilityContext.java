package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class AbilityContext {
    private final Map<MemoryModuleType<?>, Optional<?>> memories;
    private final IMKEntityData casterData;
    private final MKAbilityInfo abilityInfo;
    @Nullable
    private BiFunction<IMKEntityData, Attribute, Float> skillValueOverrideProvider;

    public AbilityContext(IMKEntityData entityData, MKAbility ability) {
        memories = new HashMap<>();
        this.casterData = entityData;
        this.abilityInfo = ability.getPortingInstance();
    }

    public AbilityContext(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        memories = new HashMap<>();
        this.casterData = entityData;
        this.abilityInfo = abilityInfo;
    }

    public IMKEntityData getCasterData() {
        return casterData;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
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

    public float getSkill(Attribute attribute) {
        if (skillValueOverrideProvider != null) {
            return skillValueOverrideProvider.apply(casterData, attribute);
        }
        return MKAbility.getSkillLevel(casterData.getEntity(), attribute);
    }

    public void setSkillResolver(BiFunction<IMKEntityData, Attribute, Float> supplier) {
        this.skillValueOverrideProvider = supplier;
    }

    public static AbilityContext forCaster(IMKEntityData casterData, MKAbilityInfo abilityInfo) {
        return new AbilityContext(casterData, abilityInfo);
    }

    public static AbilityContext singleTarget(IMKEntityData casterData, LivingEntity target, MKAbilityInfo abilityInfo) {
        return forCaster(casterData, abilityInfo).withMemory(MKAbilityMemories.ABILITY_TARGET, Optional.ofNullable(target));
    }

    public static AbilityContext selfTarget(IMKEntityData targetData, MKAbilityInfo abilityInfo) {
        return singleTarget(targetData, targetData.getEntity(), abilityInfo);
    }

    public static AbilityContext singleOrPositionTarget(IMKEntityData casterData, MKAbilityInfo abilityInfo, TargetUtil.LivingOrPosition position) {
        return forCaster(casterData, abilityInfo).withMemory(MKAbilityMemories.ABILITY_POSITION_TARGET, Optional.ofNullable(position));
    }
}
