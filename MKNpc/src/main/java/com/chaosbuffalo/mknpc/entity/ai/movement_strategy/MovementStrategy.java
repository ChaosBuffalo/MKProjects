package com.chaosbuffalo.mknpc.entity.ai.movement_strategy;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.server.level.ServerLevel;

public abstract class MovementStrategy {

    public abstract void update(ServerLevel world, MKEntity entity);
}
