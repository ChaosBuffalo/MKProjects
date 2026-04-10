package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.entities.SpriteProjectileRenderer;
import com.chaosbuffalo.mkcore.fx.particles.IndicatorParticle;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleRenderTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = MKCore.MOD_ID, value = Dist.CLIENT)
public class MKRenderers {

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CoreEntities.AREA_EFFECT.get(), EntityMKAreaEffectRenderer::new);
        event.registerEntityRenderer(CoreEntities.LINE_EFFECT.get(), BaseEffectEntityRenderer::new);
        event.registerEntityRenderer(CoreEntities.POINT_EFFECT.get(), BaseEffectEntityRenderer::new);
        event.registerEntityRenderer(CoreEntities.BLOCK_ANCHORED_LINE_EFFECT.get(), BaseEffectEntityRenderer::new);
        event.registerEntityRenderer(CoreEntities.CONE_AREA_EFFECT.get(), BaseEffectEntityRenderer::new);
        event.registerEntityRenderer(CoreEntities.ABILITY_PROJECTILE_TYPE.get(),
                (context) -> new SpriteProjectileRenderer<>(context, 1.0f, true));
    }

    @SubscribeEvent
    public static void registerParticleFactory(RegisterParticleProvidersEvent evt) {
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CROSS.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CLOVER.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_LINE.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CIRCLE.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_GRADIENT_SQUARE.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_SIDEWAYS_LINE.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.MAGIC_CHIP.get(),
                defaultMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.INDICATOR_PARTICLE.get(),
                IndicatorParticle.IndicatorFactory::new);
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_CROSS.get(),
                defaultBlackMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_CLOVER.get(),
                defaultBlackMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_CIRCLE.get(),
                defaultBlackMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_LINE.get(),
                defaultBlackMagicFactory());
        Minecraft.getInstance().particleEngine.register(CoreParticles.BLACK_MAGIC_GRADIENT_SQUARE.get(),
                defaultBlackMagicFactory());
    }

    @NotNull
    private static ParticleEngine.SpriteParticleRegistration<MKParticleData> defaultBlackMagicFactory() {
        return (spriteSet) -> new MKParticle.MKParticleFactory(
                spriteSet, -0.0001f, 0.05f,
                0.05f, 80, true, ParticleRenderTypes.BLACK_MAGIC_RENDERER,
                null);
    }

    @NotNull
    private static ParticleEngine.SpriteParticleRegistration<MKParticleData> defaultMagicFactory() {
        return (spriteSet) -> new MKParticle.MKParticleFactory(
                spriteSet, -0.0001f, 0.05f,
                0.05f, 80, true,
                null);
    }
}
