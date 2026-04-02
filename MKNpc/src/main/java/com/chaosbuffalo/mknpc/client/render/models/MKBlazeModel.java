package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelPart;

public class MKBlazeModel<T extends MKEntity> extends BlazeModel<T> {

    public MKBlazeModel(ModelPart root) {
        super(root);
    }
}
