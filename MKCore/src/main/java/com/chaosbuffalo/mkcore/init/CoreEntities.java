package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CoreEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MKCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MKAreaEffectEntity>> AREA_EFFECT = ENTITIES.register("mk_area_effect",
            () -> EntityType.Builder.<MKAreaEffectEntity>of(MKAreaEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0, 0)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_area_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<LineEffectEntity>> LINE_EFFECT = ENTITIES.register("mk_line_effect",
            () -> EntityType.Builder.<LineEffectEntity>of(LineEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0, 0)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_line_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlockAnchoredLineEffectEntity>> BLOCK_ANCHORED_LINE_EFFECT = ENTITIES.register("mk_block_line_effect",
            () -> EntityType.Builder.<BlockAnchoredLineEffectEntity>of(BlockAnchoredLineEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0, 0)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_block_line_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<PointEffectEntity>> POINT_EFFECT = ENTITIES.register("mk_point_effect",
            () -> EntityType.Builder.<PointEffectEntity>of(PointEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1, 1)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_point_effect"));

    public static final DeferredHolder<EntityType<?>, EntityType<AbilityProjectileEntity>> ABILITY_PROJECTILE_TYPE = ENTITIES.register(
            "ability_projectile", () -> EntityType.Builder.of(AbilityProjectileEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.25f, 0.25f)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSave()
                    .build("ability_projectile"));

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
