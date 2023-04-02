package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public interface INotifyOnEntityDeath {

    void onEntityDeath(IEntityNpcData npcData, LivingDeathEvent event);
}
