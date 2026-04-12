package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public record MeleeAnimationProfile(ResourceLocation id, ResourceLocation family, List<ResourceLocation> windups,
                                    List<Strike> strikes, Selection selection) {
    public static final Codec<MeleeAnimationProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("family").forGetter(MeleeAnimationProfile::family),
            ResourceLocation.CODEC.listOf().optionalFieldOf("windups", List.of()).forGetter(MeleeAnimationProfile::windups),
            Strike.CODEC.listOf().fieldOf("strikes").forGetter(MeleeAnimationProfile::strikes),
            Selection.CODEC.optionalFieldOf("selection", Selection.CYCLE).forGetter(MeleeAnimationProfile::selection)
    ).apply(instance, (family, windups, strikes, selection) ->
            new MeleeAnimationProfile(ResourceLocation.withDefaultNamespace("unknown"), family, windups, strikes, selection)));

    public record Strike(ResourceLocation pose, float lunge) {
        public static final Codec<Strike> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("pose").forGetter(Strike::pose),
                Codec.FLOAT.optionalFieldOf("lunge", 0.0F).forGetter(Strike::lunge)
        ).apply(instance, Strike::new));
    }

    public enum Selection {
        CYCLE;

        public static final Codec<Selection> CODEC = Codec.STRING.xmap(x -> Selection.valueOf(x.toUpperCase()),
                x -> x.name().toLowerCase());
    }

    @Nullable
    public ResourceLocation getWindup(int variant) {
        if (windups.isEmpty()) {
            return null;
        }
        return windups.get(Math.floorMod(variant, windups.size()));
    }

    @Nullable
    public Strike getStrike(int variant) {
        if (strikes.isEmpty()) {
            return null;
        }
        return strikes.get(Math.floorMod(variant, strikes.size()));
    }

    public MeleeAnimationProfile withId(ResourceLocation id) {
        return new MeleeAnimationProfile(id, family, windups, strikes, selection);
    }
}
