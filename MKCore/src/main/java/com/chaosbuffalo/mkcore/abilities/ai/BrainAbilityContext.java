package com.chaosbuffalo.mkcore.abilities.ai;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

public class BrainAbilityContext extends AbilityContext {
    private final Brain<?> brain;

    public BrainAbilityContext(LivingEntity entity) {
        brain = entity.getBrain();
    }

    public <U> void setMemory(MemoryModuleType<U> memoryType, Optional<U> value) {
        brain.setMemory(memoryType, value);
    }

    public <T> Optional<T> getMemory(MemoryModuleType<T> memory) {
        return brain.getMemory(memory);
    }
}
