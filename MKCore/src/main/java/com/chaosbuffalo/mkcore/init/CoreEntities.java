package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.entities.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CoreEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MKCore.MOD_ID);

    public static final RegistryObject<EntityType<MKAreaEffectEntity>> AREA_EFFECT = ENTITIES.register("mk_area_effect",
            () -> EntityType.Builder.<MKAreaEffectEntity>of(MKAreaEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0, 0)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_area_effect"));

    public static final RegistryObject<EntityType<LineEffectEntity>> LINE_EFFECT = ENTITIES.register("mk_line_effect",
            () -> EntityType.Builder.<LineEffectEntity>of(LineEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0, 0)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_line_effect"));

    public static final RegistryObject<EntityType<BlockAnchoredLineEffectEntity>> BLOCK_ANCHORED_LINE_EFFECT = ENTITIES.register("mk_block_line_effect",
            () -> EntityType.Builder.<BlockAnchoredLineEffectEntity>of(BlockAnchoredLineEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0, 0)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_block_line_effect"));

    public static final RegistryObject<EntityType<PointEffectEntity>> POINT_EFFECT = ENTITIES.register("mk_point_effect",
            () -> EntityType.Builder.<PointEffectEntity>of(PointEffectEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1, 1)
                    .setTrackingRange(5)
                    .setUpdateInterval(10)
                    .setShouldReceiveVelocityUpdates(true)
                    .noSummon()
                    .noSave()
                    .build("mk_point_effect"));

    public static final net.minecraftforge.registries.RegistryObject<EntityType<AbilityProjectileEntity>> ABILITY_PROJECTILE_TYPE = ENTITIES.register(
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
