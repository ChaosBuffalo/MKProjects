package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MobEffectTextureManager.class)
public abstract class MobEffectTextureManagerMixins extends TextureAtlasHolder {

    public MobEffectTextureManagerMixins(TextureManager textureManagerIn, ResourceLocation atlasTextureLocation, ResourceLocation prefixIn) {
        super(textureManagerIn, atlasTextureLocation, prefixIn);
    }

//    /**
//     * @author ralekdev
//     * @reason Ensure MKEffect textures are baked into the texture atlas
//     */
//    @Overwrite
//    protected Stream<ResourceLocation> getResourcesToLoad() {
//        // FIXME: Figure out why effects is null on first call of prepare now
//        if (MKCoreRegistry.EFFECTS != null) {
//            return Streams.concat(ForgeRegistries.MOB_EFFECTS.getKeys().stream(), MKCoreRegistry.EFFECTS.getKeys().stream());
//        } else {
//            return ForgeRegistries.MOB_EFFECTS.getKeys().stream();
//        }
//
//    }

    /**
     * @author ralekdev
     * @reason Allow texture lookup for MKActiveEffect-based effects
     */
    @Overwrite
    public TextureAtlasSprite get(Holder<MobEffect> effect) {

        if (effect.value() instanceof MKEffect.WrapperEffect vanilla) {
            ResourceLocation effectId = vanilla.getMKEffect().getId();
            return super.getSprite(effectId);
        }
        // Vanilla logic
        return this.getSprite(effect.unwrapKey().map(ResourceKey::location).orElseGet(MissingTextureAtlasSprite::getLocation));
    }
}
