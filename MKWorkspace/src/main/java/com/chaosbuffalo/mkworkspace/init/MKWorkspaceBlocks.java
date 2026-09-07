package com.chaosbuffalo.mkworkspace.init;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.blocks.MKWorkspaceDevBlock;
import com.chaosbuffalo.mkworkspace.items.MKWorkspaceInsertToolItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKWorkspaceBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MKWorkspace.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MKWorkspace.MODID);

    public static final DeferredBlock<MKWorkspaceDevBlock> MK_WORKSPACE_DEV_BLOCK = BLOCKS.register("mk_workspace_dev",
            () -> new MKWorkspaceDevBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(2.0f).noOcclusion()));
    public static final DeferredItem<BlockItem> MK_WORKSPACE_DEV_ITEM = ITEMS.register("mk_workspace_dev",
            () -> new BlockItem(MK_WORKSPACE_DEV_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<MKWorkspaceInsertToolItem> MK_WORKSPACE_INSERT_TOOL =
            ITEMS.register("mk_workspace_insert_tool",
                    () -> new MKWorkspaceInsertToolItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }
}
