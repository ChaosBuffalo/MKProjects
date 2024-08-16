package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;

public class CoreParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MKCore.MOD_ID);

    private static DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> register(String name) {
        return PARTICLES.register(name,
                () -> new ParticleType<>(false, MKParticleData.DESERIALIZER) {
                    @Nonnull
                    @Override
                    public Codec<MKParticleData> codec() {
                        return MKParticleData.typeCodec(this);
                    }
                });
    }

    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_CROSS = register("magic_cross");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_CLOVER = register("magic_clover");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_LINE = register("magic_line");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_CIRCLE = register("magic_circle");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_GRADIENT_SQUARE = register("magic_gradient_square");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INDICATOR_PARTICLE = PARTICLES.register("indicator_particle",
            () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_SIDEWAYS_LINE = register("magic_sideways_line");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> MAGIC_CHIP = register("magic_chip");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> BLACK_MAGIC_CROSS = register("black_magic_cross");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> BLACK_MAGIC_CLOVER = register("black_magic_clover");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> BLACK_MAGIC_LINE = register("black_magic_line");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> BLACK_MAGIC_CIRCLE = register("black_magic_circle");
    public static final DeferredHolder<ParticleType<?>, ParticleType<MKParticleData>> BLACK_MAGIC_GRADIENT_SQUARE = register("black_magic_gradient_square");

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }

    public static void handleEditorParticleRegistration() {
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_LINE.getId(), MAGIC_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_CIRCLE.getId(), MAGIC_CIRCLE);
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_CROSS.getId(), MAGIC_CROSS);
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_GRADIENT_SQUARE.getId(), MAGIC_GRADIENT_SQUARE);
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_CLOVER.getId(), MAGIC_CLOVER);
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_SIDEWAYS_LINE.getId(), MAGIC_SIDEWAYS_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(MAGIC_CHIP.getId(), MAGIC_CHIP);
        ParticleAnimationManager.putParticleTypeForEditor(BLACK_MAGIC_CROSS.getId(), BLACK_MAGIC_CROSS);
        ParticleAnimationManager.putParticleTypeForEditor(BLACK_MAGIC_CLOVER.getId(), BLACK_MAGIC_CLOVER);
        ParticleAnimationManager.putParticleTypeForEditor(BLACK_MAGIC_LINE.getId(), BLACK_MAGIC_LINE);
        ParticleAnimationManager.putParticleTypeForEditor(BLACK_MAGIC_CIRCLE.getId(), BLACK_MAGIC_CIRCLE);
        ParticleAnimationManager.putParticleTypeForEditor(BLACK_MAGIC_GRADIENT_SQUARE.getId(), BLACK_MAGIC_GRADIENT_SQUARE);
    }
}
