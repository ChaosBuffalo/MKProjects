package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKBlazeEntity;
import com.chaosbuffalo.mknpc.entity.MKFlyingSkeletonEntity;
import com.chaosbuffalo.mknpc.entity.MKSkeletonEntity;
import com.chaosbuffalo.mknpc.entity.MKZombifiedPiglinEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = MKNpc.MODID)
public class MKNpcEntityTypes {
    public static final String SKELETON_NAME = "skeleton";
    public static final String ZOMBIFIED_PIGLIN_NAME = "zombified_piglin";
    public static final String BLAZE_NAME = "blaze";
    public static final String FLYING_SKELETON_NAME = "flying_skeleton";

    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(SKELETON_TYPE.get(), MKSkeletonEntity.registerAttributes(1.0, 0.3).build());
        event.put(ZOMBIE_PIGLIN_TYPE.get(), MKZombifiedPiglinEntity.registerAttributes(1.0, 0.2).build());
        event.put(BLAZE_TYPE.get(), MKBlazeEntity.registerAttributes(1.0, 0.38).build());
        event.put(FLYING_SKELETON_TYPE.get(), MKFlyingSkeletonEntity.registerAttributes(1.0, 0.3).build());
    }

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MKNpc.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MKSkeletonEntity>> SKELETON_TYPE = ENTITIES.register(SKELETON_NAME,
            () -> EntityType.Builder.of(MKSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.SKELETON.getWidth(), EntityType.SKELETON.getHeight())
                    .build(SKELETON_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<MKZombifiedPiglinEntity>> ZOMBIE_PIGLIN_TYPE = ENTITIES.register(ZOMBIFIED_PIGLIN_NAME,
            () -> EntityType.Builder.of(MKZombifiedPiglinEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.ZOMBIFIED_PIGLIN.getWidth(), EntityType.ZOMBIFIED_PIGLIN.getHeight())
                    .build(ZOMBIFIED_PIGLIN_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<MKBlazeEntity>> BLAZE_TYPE = ENTITIES.register(BLAZE_NAME,
            () -> EntityType.Builder.of(MKBlazeEntity::new, MobCategory.MONSTER)
                    .fireImmune()
                    .sized(EntityType.BLAZE.getWidth(), EntityType.BLAZE.getHeight())
                    .build(BLAZE_NAME));

    public static final DeferredHolder<EntityType<?>, EntityType<MKFlyingSkeletonEntity>> FLYING_SKELETON_TYPE = ENTITIES.register(FLYING_SKELETON_NAME,
            () -> EntityType.Builder.of(MKFlyingSkeletonEntity::new, MobCategory.MONSTER)
                    .sized(EntityType.SKELETON.getWidth(), EntityType.SKELETON.getHeight())
                    .build(SKELETON_NAME));




    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }

}