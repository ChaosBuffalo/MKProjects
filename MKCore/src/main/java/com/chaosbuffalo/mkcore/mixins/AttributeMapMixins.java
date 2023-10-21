package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.attributes.AttributeMapExtension;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(AttributeMap.class)
public class AttributeMapMixins implements AttributeMapExtension {

    @Nullable
    @Unique
    private Consumer<AttributeInstance> mkcore$onModification;

    @Inject(
            method = "onAttributeModified(Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;)V",
            at = @At("TAIL")
    )
    protected void mkcore$onAttributeModified(AttributeInstance instance, CallbackInfo ci) {
        if (mkcore$onModification != null) {
            mkcore$onModification.accept(instance);
        }
    }

    @Override
    public void mkcore$setAttributeModifiedHandler(Consumer<AttributeInstance> handler) {
        mkcore$onModification = handler;
    }
}
