package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.Capability;

public class ChestNpcDataProvider extends NpcCapabilities.Provider<ChestBlockEntity, IChestNpcData> {

    public ChestNpcDataProvider(ChestBlockEntity entity) {
        super(entity);
    }

    @Override
    IChestNpcData makeData(ChestBlockEntity attached) {
        return new ChestNpcDataHandler(attached);
    }

    @Override
    Capability<IChestNpcData> getCapability() {
        return NpcCapabilities.CHEST_NPC_DATA_CAPABILITY;
    }
}
