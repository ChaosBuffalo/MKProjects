package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ProjectileCastBehaviorTypes {

    public static final DeferredRegister<ProjectileCastBehaviorType<?>> REGISTRY = DeferredRegister.create(MKCoreRegistry.CAST_BEHAVIOR_TYPES_NAME, MKCore.MOD_ID);
    public static final Supplier<ProjectileCastBehaviorType<SingleProjectileBehavior>> SINGLE = REGISTRY.register("single", () -> () -> SingleProjectileBehavior.CODEC);

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
