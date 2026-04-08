package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MeleeAnimationPose(String type, List<PoseChannel> channels, PoseOptions options) {
    public static final Codec<MeleeAnimationPose> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("type", "mkcore:model_pose").forGetter(MeleeAnimationPose::type),
            PoseChannel.CODEC.listOf().optionalFieldOf("channels", List.of()).forGetter(MeleeAnimationPose::channels),
            PoseOptions.CODEC.optionalFieldOf("options", PoseOptions.DEFAULT).forGetter(MeleeAnimationPose::options)
    ).apply(instance, MeleeAnimationPose::new));

    public record PoseOptions(boolean bobArms, float swingDirection) {
        public static final PoseOptions DEFAULT = new PoseOptions(false, 1.0F);
        public static final Codec<PoseOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("bobArms", false).forGetter(PoseOptions::bobArms),
                Codec.FLOAT.optionalFieldOf("swingDirection", 1.0F).forGetter(PoseOptions::swingDirection)
        ).apply(instance, PoseOptions::new));
    }
}
