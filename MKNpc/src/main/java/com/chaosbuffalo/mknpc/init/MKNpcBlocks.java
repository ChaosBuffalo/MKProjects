package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.blocks.MKPoiBlock;
import com.chaosbuffalo.mknpc.blocks.MKSpawnerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MKNpcBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MKNpc.MODID);
    public static final DeferredBlock<MKSpawnerBlock> MK_SPAWNER_BLOCK = BLOCKS.register("mk_spawner",
            () -> new MKSpawnerBlock(Block.Properties.of().mapColor(MapColor.NONE)
                    .pushReaction(PushReaction.IGNORE).noOcclusion()));
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MKNpc.MODID);
    public static final DeferredItem<BlockItem> MK_SPAWNER_ITEM = ITEMS.register("mk_spawner",
            () -> new BlockItem(MK_SPAWNER_BLOCK.get(), new Item.Properties()));
    public static final DeferredBlock<MKPoiBlock> MK_POI_BLOCK = BLOCKS.register("mk_poi",
            () -> new MKPoiBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .pushReaction(PushReaction.IGNORE).noOcclusion()
                    .isRedstoneConductor((BlockState state, BlockGetter reader, BlockPos pos) -> false)
                    .isViewBlocking((BlockState state, BlockGetter reader, BlockPos pos) -> false)));
    public static final DeferredItem<BlockItem> MK_POI_ITEM = ITEMS.register("mk_poi",
            () -> new BlockItem(MK_POI_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
