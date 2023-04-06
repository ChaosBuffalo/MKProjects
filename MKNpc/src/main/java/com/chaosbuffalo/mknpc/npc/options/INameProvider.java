package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;


public interface INameProvider {

    MutableComponent getEntityName(NpcDefinition definition, Level world, UUID spawnId);

    @Nullable
    String getDisplayName();
}
