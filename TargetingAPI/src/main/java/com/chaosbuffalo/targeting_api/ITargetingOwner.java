package com.chaosbuffalo.targeting_api;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public interface ITargetingOwner {

    @Nullable
    Entity getTargetingOwner();
}
