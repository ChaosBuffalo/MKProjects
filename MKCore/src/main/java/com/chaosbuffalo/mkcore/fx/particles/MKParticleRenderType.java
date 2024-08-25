package com.chaosbuffalo.mkcore.fx.particles;

import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;

public interface MKParticleRenderType extends ParticleRenderType {

    void end(TextureManager textureManager);
}
