package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.google.common.collect.Streams;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.stream.Stream;

@Mixin(MobEffectTextureManager.class)
public abstract class PotionSpriteUploaderMixins extends TextureAtlasHolder {

    public PotionSpriteUploaderMixins(TextureManager textureManagerIn, ResourceLocation atlasTextureLocation, String prefixIn) {
        super(textureManagerIn, atlasTextureLocation, prefixIn);
    }


    /**
     * @author ralekdev
     * @reason Ensure MKEffect textures are baked into the texture atlas
     */
    @Overwrite
    protected Stream<ResourceLocation> getResourcesToLoad() {
        // FIXME: Figure out why effects is null on first call of prepare now
        if (MKCoreRegistry.EFFECTS != null) {
            return Streams.concat(Registry.MOB_EFFECT.keySet().stream(), MKCoreRegistry.EFFECTS.getKeys().stream());
        } else {
            return Registry.MOB_EFFECT.keySet().stream();
        }

    }

    /**
     * @author ralekdev
     * @reason Allow texture lookup for MKActiveEffect-based effects
     */
    @Overwrite
    public TextureAtlasSprite get(MobEffect effectIn) {
        if (effectIn instanceof MKEffect.WrapperEffect) {
            MKEffect.WrapperEffect vanilla = (MKEffect.WrapperEffect) effectIn;

            ResourceLocation effectId = vanilla.getMKEffect().getId();
            return super.getSprite(effectId);
        }
        // Vanilla logic
        return super.getSprite(Registry.MOB_EFFECT.getKey(effectIn));
    }
}
