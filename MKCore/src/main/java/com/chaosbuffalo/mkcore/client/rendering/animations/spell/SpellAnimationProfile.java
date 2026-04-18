package com.chaosbuffalo.mkcore.client.rendering.animations.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record SpellAnimationProfile(ResourceLocation id, ResourceLocation family, ResourceLocation category,
                                    ResourceLocation castingPose, ResourceLocation releasePose) {
    public static final Codec<SpellAnimationProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("family").forGetter(SpellAnimationProfile::family),
            ResourceLocation.CODEC.fieldOf("category").forGetter(SpellAnimationProfile::category),
            ResourceLocation.CODEC.fieldOf("casting_pose").forGetter(SpellAnimationProfile::castingPose),
            ResourceLocation.CODEC.fieldOf("release_pose").forGetter(SpellAnimationProfile::releasePose)
    ).apply(instance, (family, category, castingPose, releasePose) ->
            new SpellAnimationProfile(ResourceLocation.withDefaultNamespace("unknown"), family, category, castingPose, releasePose)));

    public SpellAnimationProfile withId(ResourceLocation id) {
        return new SpellAnimationProfile(id, family, category, castingPose, releasePose);
    }
}
