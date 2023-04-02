package com.chaosbuffalo.mknpc.client.render.models.styling;

import net.minecraft.client.model.geom.builders.CubeDeformation;

public class ModelArgs {
    public CubeDeformation deformation;
    public boolean overrideBaseParts;
    public float heightOffset;
    public CubeDeformation outerArmorDeformation;
    public CubeDeformation innerArmorDeformation;

    public ModelArgs(CubeDeformation deformation, boolean overrideBaseParts, float heightOffset, CubeDeformation outerArmorDeformation,
                     CubeDeformation innerArmorDeformation) {
        this.heightOffset = heightOffset;
        this.overrideBaseParts = overrideBaseParts;
        this.deformation = deformation;
        this.outerArmorDeformation = outerArmorDeformation;
        this.innerArmorDeformation = innerArmorDeformation;
    }

    public ModelArgs copyWithOverrides(boolean override, CubeDeformation deformation) {
        return new ModelArgs(deformation, override, heightOffset, outerArmorDeformation, innerArmorDeformation);
    }
}