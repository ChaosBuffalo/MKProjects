package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.runtime.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class AbilityRuntimeService {
    private final AbilityStateStore stateStore;
    private final AbilityPowerResolver powerResolver;
    private final SimpleAbilityEngine engine;

    public AbilityRuntimeService(AbilityDefinitionResolver definitionResolver) {
        this.stateStore = new MemoryAbilityStateStore();
        this.powerResolver = new MKAbilityPowerResolver();
        this.engine = new SimpleAbilityEngine(definitionResolver, powerResolver, stateStore);
    }

    public AbilityStateStore getStateStore() {
        return stateStore;
    }

    public AbilityPowerResolver getPowerResolver() {
        return powerResolver;
    }

    public AbilityEngine getEngine() {
        return engine;
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide()) {
            engine.tickEntity(MKCore.getEntityDataOrThrow(living));
        }
    }
}
