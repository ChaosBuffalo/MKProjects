package com.chaosbuffalo.mkcore.effects;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public abstract class ScalingDamageEffectState extends ScalingValueEffectState {
    @Nullable
    protected MKDamageType damageType = null;

    public void setDamageType(@Nullable MKDamageType damageType) {
        this.damageType = damageType;
    }

    @Nullable
    public MKDamageType getDamageType() {
        return damageType;
    }

    @Override
    public void serializeStorage(CompoundTag stateTag) {
        super.serializeStorage(stateTag);
        if (damageType != null) {
            MKNBTUtil.writeResourceLocation(stateTag, "damageType", damageType.getId());
        }
    }

    @Override
    public void deserializeStorage(CompoundTag stateTag) {
        super.deserializeStorage(stateTag);
        if (stateTag.contains("damageType")) {
            damageType = MKCoreRegistry.getDamageType(MKNBTUtil.readResourceLocation(stateTag, "damageType"));
        }
    }

}
