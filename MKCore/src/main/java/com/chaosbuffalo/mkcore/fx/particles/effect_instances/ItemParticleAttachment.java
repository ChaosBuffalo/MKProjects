package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record ItemParticleAttachment(Vec3 start, Vec3 end) {
    public static final Codec<ItemParticleAttachment> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Vec3.CODEC.fieldOf("start").forGetter(ItemParticleAttachment::start),
            Vec3.CODEC.fieldOf("end").forGetter(ItemParticleAttachment::end)
    ).apply(builder, ItemParticleAttachment::new));
}
