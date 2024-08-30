package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.tile_entities.MKPoiTileEntity;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKNpcTileEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MKNpc.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MKSpawnerTileEntity>> MK_SPAWNER_TILE_ENTITY_TYPE =
            TILES.register("mk_spawner", () ->
                    BlockEntityType.Builder.of(MKSpawnerTileEntity::new, MKNpcBlocks.MK_SPAWNER_BLOCK.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MKPoiTileEntity>> MK_POI_TILE_ENTITY_TYPE =
            TILES.register("mk_poi", () ->
                    BlockEntityType.Builder.of(MKPoiTileEntity::new, MKNpcBlocks.MK_POI_BLOCK.get())
                            .build(null));

    public static void register(IEventBus modBus) {
        TILES.register(modBus);
    }
}
