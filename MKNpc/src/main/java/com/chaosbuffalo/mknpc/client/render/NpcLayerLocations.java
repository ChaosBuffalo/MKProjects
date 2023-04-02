package com.chaosbuffalo.mknpc.client.render;

import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class NpcLayerLocations {

    public static final ModelLayerLocation MAIN_SKELETON_LAYER = new ModelLayerLocation(MKNpcEntityTypes.SKELETON_TYPE.getId(), "main");
    public static final ModelLayerLocation INNER_ARMOR_SKELETON_LAYER = new ModelLayerLocation(MKNpcEntityTypes.SKELETON_TYPE.getId(), "inner_armor");
    public static final ModelLayerLocation OUTER_ARMOR_SKELETON_LAYER = new ModelLayerLocation(MKNpcEntityTypes.SKELETON_TYPE.getId(), "outer_armor");
}
