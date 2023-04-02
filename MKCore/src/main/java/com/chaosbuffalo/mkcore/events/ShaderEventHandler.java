package com.chaosbuffalo.mkcore.events;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid= MKCore.MOD_ID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ShaderEventHandler {
    @Nullable
    private static ShaderInstance rendertypeMagicParticle;

    @Nullable
    private static ShaderInstance rendertypeBlackMagicParticle;

    public static ShaderInstance getBlackMagicParticleShader() {
        return Objects.requireNonNull(rendertypeBlackMagicParticle, "Attempted to call getBlackMagicParticleShader before shaders have finished loading.");
    }

    public static ShaderInstance getMagicParticleShader()
    {
        return Objects.requireNonNull(rendertypeMagicParticle, "Attempted to call getMagicParticleShader before shaders have finished loading.");
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException
    {
        event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation(MKCore.MOD_ID,"magic_particle"),
                DefaultVertexFormat.PARTICLE), (p_172645_) -> {
            rendertypeMagicParticle = p_172645_;
        });
        event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation(MKCore.MOD_ID, "black_magic_particle"),
                DefaultVertexFormat.PARTICLE), (shader) -> {
            rendertypeBlackMagicParticle = shader;
        });
    }


}