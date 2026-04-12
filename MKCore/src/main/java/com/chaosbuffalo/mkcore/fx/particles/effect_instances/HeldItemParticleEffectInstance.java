package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.HeldItemParticleEffectRenderer;
import com.mojang.serialization.Codec;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class HeldItemParticleEffectInstance extends ParticleEffectInstance {
    public static final ResourceLocation TYPE = MKCore.makeRL("effect_instance.held_item");
    private static final Codec<InteractionHand> HAND_CODEC = Codec.STRING.xmap(InteractionHand::valueOf, InteractionHand::name);
    public static final MapCodec<HeldItemParticleEffectInstance> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            UUIDUtil.STRING_CODEC.fieldOf("instanceUUID").forGetter(ParticleEffectInstance::getInstanceUUID),
            ResourceLocation.CODEC.fieldOf("particleAnimName").forGetter(ParticleEffectInstance::getParticleAnimName),
            HAND_CODEC.fieldOf("hand").forGetter(HeldItemParticleEffectInstance::getHand)
    ).apply(builder, HeldItemParticleEffectInstance::new));

    private final InteractionHand hand;

    public HeldItemParticleEffectInstance(UUID instanceUUID, ResourceLocation particleName, InteractionHand hand) {
        super(TYPE, instanceUUID);
        this.particleAnimName = particleName;
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public void update(Entity entity, MCSkeleton skeleton, float partialTicks, Vec3 offset) {
        // Held item attachments need the exact item render pose, so they are spawned from render hooks instead.
    }
}
