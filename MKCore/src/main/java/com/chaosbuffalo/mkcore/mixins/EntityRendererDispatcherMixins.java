package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.client.rendering.MKPlayerRenderer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererDispatcherMixins {

    private static final Map<String, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS = ImmutableMap.of("default", (p_174098_) -> {
        return new MKPlayerRenderer(p_174098_, false);
    }, "slim", (p_174096_) -> {
        return new MKPlayerRenderer(p_174096_, true);
    });

    private static final Map<String, EntityRendererProvider<AbstractClientPlayer>> VANILLA_PROVIDERS = ImmutableMap.of("default", (p_174098_) -> {
        return new PlayerRenderer(p_174098_, false);
    }, "slim", (p_174096_) -> {
        return new PlayerRenderer(p_174096_, true);
    });

    // redirect player model registration to use ours
    @Redirect(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"),
            method = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V"
    )
    private Map<String, EntityRenderer<? extends Player>> proxyCreatePlayerRenderers(EntityRendererProvider.Context p_174052_) {
       ImmutableMap.Builder<String, EntityRenderer<? extends Player>> builder = ImmutableMap.builder();
        Map<String, EntityRendererProvider<AbstractClientPlayer>> providers;
        if (MKConfig.CLIENT.enablePlayerCastAnimations.get()) {
           providers = PLAYER_PROVIDERS;
       } else {
            providers = VANILLA_PROVIDERS;
        }
        providers.forEach((p_174047_, p_174048_) -> {
            try {
                builder.put(p_174047_, p_174048_.create(p_174052_));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create player model for " + p_174047_, exception);
            }
        });
        return builder.build();
    }
}
