package com.chaosbuffalo.mknpc.capabilities;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;

public class EntityNpcDataProvider extends NpcCapabilities.Provider<LivingEntity, IEntityNpcData> {

    public EntityNpcDataProvider(LivingEntity entity) {
        super(entity);
    }

    @Override
    IEntityNpcData makeData(LivingEntity attached) {
        return new EntityNpcDataHandler(attached);
    }

    @Override
    Capability<IEntityNpcData> getCapability() {
        return NpcCapabilities.ENTITY_NPC_DATA_CAPABILITY;
    }
}
