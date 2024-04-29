package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.nbt.CompoundTag;

public abstract class ScalingValueEffectState extends ParticleEffectState {
    protected float base = 0.0f;
    protected float scale = 0.0f;
    protected float modScale = 1.0f;

    public void setScalingParameters(float base, float scale) {
        setScalingParameters(base, scale, 1.0f);
    }

    public void setScalingParameters(float base, float scale, float modScale) {
        this.base = base;
        this.scale = scale;
        this.modScale = modScale;
    }

    public float getScaledValue(int stacks, float skillLevel) {
        return base + (stacks * (scale * skillLevel));
    }

    public float getModifierScale() {
        return modScale;
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        stateTag.putFloat("base", base);
        stateTag.putFloat("scale", scale);
        stateTag.putFloat("modScale", modScale);
    }

    @Override
    public void deserializeStorage(CompoundTag stateTag) {
        super.deserializeStorage(stateTag);
        base = stateTag.getFloat("base");
        scale = stateTag.getFloat("scale");
        modScale = stateTag.getFloat("modScale");
    }
}
