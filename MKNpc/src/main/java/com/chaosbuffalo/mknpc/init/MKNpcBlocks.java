package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.blocks.MKPoiBlock;
import com.chaosbuffalo.mknpc.blocks.MKSpawnerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class MKNpcBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MKNpc.MODID);
    public static final RegistryObject<MKSpawnerBlock> MK_SPAWNER_BLOCK = BLOCKS.register("mk_spawner",
            () -> new MKSpawnerBlock(Block.Properties.of(MKSpawnerBlock.SPAWNER_MATERIAL).noOcclusion()));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MKNpc.MODID);
    public static final RegistryObject<BlockItem> MK_SPAWNER_ITEM = ITEMS.register("mk_spawner",
            () -> new BlockItem(MK_SPAWNER_BLOCK.get(),  new Item.Properties()));
    public static final RegistryObject<MKPoiBlock> MK_POI_BLOCK = BLOCKS.register("mk_poi",
            () -> new MKPoiBlock(BlockBehaviour.Properties.of(MKPoiBlock.MATERIAL).noOcclusion()
                    .isRedstoneConductor((BlockState state, BlockGetter reader, BlockPos pos) -> false)
                    .isViewBlocking((BlockState state, BlockGetter reader, BlockPos pos) -> false)));
    public static final RegistryObject<BlockItem> MK_POI_ITEM = ITEMS.register("mk_poi",
            () -> new BlockItem(MK_POI_BLOCK.get(),  new Item.Properties()));

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
