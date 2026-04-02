package com.chaosbuffalo.mknpc.entity;

import net.minecraft.resources.ResourceLocation;

public interface IModelLookProvider {

    ResourceLocation getCurrentModelLook();

    void setCurrentModelLook(ResourceLocation lookId);
}
