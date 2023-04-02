package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.events.ShaderEventHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;

public class ParticleRenderTypes {

    public static final ParticleRenderType MAGIC_RENDERER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.setShader(ShaderEventHandler::getMagicParticleShader);
            //There is a bug in particle engine / the shader system where particle engine disables blends at the end of
            //its rendering phase causing the checking to see if blend mode has already been enabled to have invalid state
            // and not know it needs to re-enable, for now lets just manually do it
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getParticleShader);
        }

        @Override
        public String toString() {
            return MKCore.MOD_ID + ":magic_render_type";
        }
    };


    public static final ParticleRenderType BLACK_MAGIC_RENDERER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {

            RenderSystem.setShader(ShaderEventHandler::getBlackMagicParticleShader);
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
            RenderSystem.enableBlend();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getParticleShader);
        }

        @Override
        public String toString() {
            return MKCore.MOD_ID + ":black_magic_render_type";
        }
    };


    public static final ParticleRenderType ALWAYS_VISIBLE_RENDERER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
//            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
////            RenderSystem.enableDepthTest();
////            RenderSystem.enableBlend();
////            RenderSystem.depthFunc(GL11.GL_LEQUAL);
////            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.001F);
//            RenderSystem.disableLighting();
//            textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
////            textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE).setBlurMipmap(true, false);
//            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.PARTICLE);
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
//            RenderSystem.enableBlend();
//            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();

//            Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE).restoreLastBlurMipmap();
//            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
//            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
//            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return MKCore.MOD_ID + ":always_visible";
        }
    };
}

