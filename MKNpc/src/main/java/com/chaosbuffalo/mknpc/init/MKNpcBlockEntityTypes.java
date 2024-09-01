package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.block_entities.MKPoiBlockEntity;
import com.chaosbuffalo.mknpc.block_entities.MKSpawnerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKNpcBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MKNpc.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MKSpawnerBlockEntity>> MK_SPAWNER_BLOCK_ENTITY_TYPE =
            TILES.register("mk_spawner", () ->
                    BlockEntityType.Builder.of(MKSpawnerBlockEntity::new, MKNpcBlocks.MK_SPAWNER_BLOCK.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MKPoiBlockEntity>> MK_POI_BLOCK_ENTITY_TYPE =
            TILES.register("mk_poi", () ->
                    BlockEntityType.Builder.of(MKPoiBlockEntity::new, MKNpcBlocks.MK_POI_BLOCK.get())
                            .build(null));

    public static void register(IEventBus modBus) {
        TILES.register(modBus);
    }
}
