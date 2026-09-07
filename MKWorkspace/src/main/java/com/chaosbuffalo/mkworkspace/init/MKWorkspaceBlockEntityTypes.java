package com.chaosbuffalo.mkworkspace.init;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.block_entities.MKWorkspaceDevBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MKWorkspaceBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TILES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MKWorkspace.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MKWorkspaceDevBlockEntity>> MK_WORKSPACE_DEV_BLOCK_ENTITY_TYPE =
            TILES.register("mk_workspace_dev", () ->
                    BlockEntityType.Builder.of(MKWorkspaceDevBlockEntity::new,
                                    MKWorkspaceBlocks.MK_WORKSPACE_DEV_BLOCK.get())
                            .build(null));

    public static void register(IEventBus modBus) {
        TILES.register(modBus);
    }
}
