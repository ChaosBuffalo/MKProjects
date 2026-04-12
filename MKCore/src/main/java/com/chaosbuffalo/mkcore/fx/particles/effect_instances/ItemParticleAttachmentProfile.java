package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ItemParticleAttachmentProfile(List<ItemParticleAttachment> attachments) {
    public static final Codec<ItemParticleAttachmentProfile> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ItemParticleAttachment.CODEC.listOf().fieldOf("attachments").forGetter(ItemParticleAttachmentProfile::attachments)
    ).apply(builder, ItemParticleAttachmentProfile::new));
}
