package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffectTextureManager.class)
public abstract class MobEffectTextureManagerMixins extends TextureAtlasHolder {

    public MobEffectTextureManagerMixins(TextureManager textureManagerIn, ResourceLocation atlasTextureLocation, ResourceLocation prefixIn) {
        super(textureManagerIn, atlasTextureLocation, prefixIn);
    }

    /**
     * @author ralekdev
     * @reason Allow texture lookup for MKActiveEffect-based effects
     */
    @WrapMethod(method = "get")
    public TextureAtlasSprite mkcore$get(Holder<MobEffect> effect, Operation<TextureAtlasSprite> original) {
        if (effect.value() instanceof MKEffect.WrapperEffect vanilla) {
            ResourceLocation effectId = vanilla.getMKEffect().getId();
            return super.getSprite(effectId);
        }
        return original.call(effect);
    }
}
