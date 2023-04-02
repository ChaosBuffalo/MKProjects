package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;

public class WorldNpcDataProvider extends NpcCapabilities.Provider<Level, IWorldNpcData> {


    public WorldNpcDataProvider(Level world) {
        super(world);
    }

    @Override
    IWorldNpcData makeData(Level attached) {
        return new WorldNpcDataHandler(attached);
    }

    @Override
    Capability<IWorldNpcData> getCapability() {
        return NpcCapabilities.WORLD_NPC_DATA_CAPABILITY;
    }
}

