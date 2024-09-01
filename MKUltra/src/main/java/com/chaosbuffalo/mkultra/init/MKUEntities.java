package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mknpc.entity.MKGolemEntity;
import com.chaosbuffalo.mknpc.entity.MKSkeletonEntity;
import com.chaosbuffalo.mknpc.entity.MKZombifiedPiglinEntity;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.entities.humans.HumanEntity;
import com.chaosbuffalo.mkultra.entities.humans.HumanGhostEntity;
import com.chaosbuffalo.mkultra.entities.orcs.OrcEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


@EventBusSubscriber(modid = MKUltra.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MKUEntities {

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MKUltra.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MKGolemEntity>> GOLEM_TYPE = REGISTRY.register("golem",
            () -> EntityType.Builder.of(MKGolemEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.IRON_GOLEM.getWidth(), EntityType.IRON_GOLEM.getHeight())
                    .build(MKUltra.id("golem").toString()));


    public static final String ORC_NAME = "orc";
    public static final DeferredHolder<EntityType<?>, EntityType<OrcEntity>> ORC_TYPE = REGISTRY.register(ORC_NAME,
            () -> EntityType.Builder.of(OrcEntity::new, MobCategory.CREATURE)
                    .sized(EntityType.ZOMBIE.getWidth(), EntityType.ZOMBIE.getHeight())
                    .build(MKUltra.id(ORC_NAME).toString()));

    public static final String HYBOREAN_SKELETON_NAME = "hyborean_skeleton";
    public static DeferredHolder<EntityType<?>, EntityType<MKSkeletonEntity>> HYBOREAN_SKELETON_TYPE = REGISTRY.register(HYBOREAN_SKELETON_NAME,
            () -> EntityType.Builder.of(MKSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.SKELETON.getWidth(), EntityType.SKELETON.getHeight())
                    .build(MKUltra.id(HYBOREAN_SKELETON_NAME).toString()));


    public static final String ZOMBIFIED_PIGLIN_NAME = "zombified_piglin";
    public static DeferredHolder<EntityType<?>, EntityType<MKZombifiedPiglinEntity>> ZOMBIFIED_PIGLIN_TYPE = REGISTRY.register(ZOMBIFIED_PIGLIN_NAME,
            () -> EntityType.Builder.of(MKZombifiedPiglinEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.ZOMBIFIED_PIGLIN.getWidth(), EntityType.ZOMBIFIED_PIGLIN.getHeight())
                    .build(MKUltra.id(ZOMBIFIED_PIGLIN_NAME).toString()));

    public static final String HUMAN_NAME = "human";
    public static DeferredHolder<EntityType<?>, EntityType<HumanEntity>> HUMAN_TYPE = REGISTRY.register(HUMAN_NAME,
            () -> EntityType.Builder.of(HumanEntity::new, MobCategory.CREATURE)
                    .sized(EntityType.ZOMBIE.getWidth(), EntityType.ZOMBIE.getHeight())
                    .build(MKUltra.id(HUMAN_NAME).toString()));

    public static final String HUMAN_GHOST_NAME = "human_ghost";
    public static DeferredHolder<EntityType<?>, EntityType<HumanGhostEntity>> HUMAN_GHOST_TYPE = REGISTRY.register(HUMAN_GHOST_NAME,
            () -> EntityType.Builder.of(HumanGhostEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.ZOMBIE.getWidth(), EntityType.ZOMBIE.getHeight())
                    .build(MKUltra.id(HUMAN_GHOST_NAME).toString()));



    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }


    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(HYBOREAN_SKELETON_TYPE.get(), MKSkeletonEntity.registerAttributes(2.0, 0.22)
                .add(Attributes.ARMOR, 5.0).build());
        event.put(ORC_TYPE.get(), OrcEntity.registerAttributes(2.0, 0.35).build());
        event.put(ZOMBIFIED_PIGLIN_TYPE.get(), MKZombifiedPiglinEntity.registerAttributes(2.0, 0.2).build());
        event.put(HUMAN_TYPE.get(), HumanEntity.registerAttributes(2.0, 0.35).build());
        event.put(GOLEM_TYPE.get(), MKGolemEntity.registerAttributes(4.0, 0.3).build());
        event.put(HUMAN_GHOST_TYPE.get(), HumanEntity.registerAttributes(2.0, 0.3).build());
    }
}
