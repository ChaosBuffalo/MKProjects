package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.attributes.AttributeInstanceExtension;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AttributeInstance.class)
public abstract class AttributeInstanceMixins implements AttributeInstanceExtension {


    @Shadow protected abstract void setDirty();

    @Override
    public void mkcore$forceUpdate() {
        setDirty();
    }
}
