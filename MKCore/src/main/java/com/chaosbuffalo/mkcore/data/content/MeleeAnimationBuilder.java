package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationProfile;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MeleeAnimationBuilder {
    private MeleeAnimationBuilder() {
    }

    public static ProfileBuilder profile(ResourceLocation family) {
        return new ProfileBuilder(family);
    }

    public static ResourceLocation poseRef(ResourceLocation pose) {
        return pose;
    }

    public static MeleeAnimationProfile.Strike strike(ResourceLocation pose, float lunge) {
        return new MeleeAnimationProfile.Strike(pose, lunge);
    }

    public static PoseBuilder pose(boolean bobArms, float swingDirection) {
        return new PoseBuilder(new MeleeAnimationPose.PoseOptions(bobArms, swingDirection));
    }

    public static PoseBuilder pose() {
        return pose(false, 1.0F);
    }

    public static List<PoseChannel> channels(PoseBuilder pose) {
        return pose.channels();
    }

    public static PoseChannel channel(String target, String property, String operation, String sign, PoseChannel.PoseValue value) {
        return new PoseChannel(target, PoseChannel.Property.fromJson(property), PoseChannel.Operation.fromJson(operation),
                PoseChannel.Sign.fromJson(sign), value);
    }

    public static PoseChannel channel(String target, String property, String operation, PoseChannel.PoseValue value) {
        return channel(target, property, operation, "none", value);
    }

    public static PoseChannel.PoseValue value(float constant, PoseChannel.PoseTerm... terms) {
        return new PoseChannel.PoseValue(constant, List.of(terms));
    }

    public static PoseChannel.PoseTerm term(String curve, float scale) {
        return new PoseChannel.PoseTerm(PoseChannel.Curve.fromJson(curve), PoseChannel.Input.NONE,
                PoseChannel.Curve.NONE, scale, 1.0F, 0.0F, false);
    }

    public static PoseChannel.PoseTerm inputTerm(String input, float scale, String multiplierCurve) {
        return new PoseChannel.PoseTerm(PoseChannel.Curve.NONE, PoseChannel.Input.fromJson(input),
                PoseChannel.Curve.fromJson(multiplierCurve), scale, 1.0F, 0.0F, false);
    }

    public static PoseChannel.PoseTerm ageTerm(float frequency, float offset, float scale, boolean absolute, String multiplierCurve) {
        return new PoseChannel.PoseTerm(PoseChannel.Curve.AGE_SIN, PoseChannel.Input.NONE,
                PoseChannel.Curve.fromJson(multiplierCurve), scale, frequency, offset, absolute);
    }

    public static class ProfileBuilder {
        private final ResourceLocation family;
        private final List<ResourceLocation> windups = new ArrayList<>();
        private final List<MeleeAnimationProfile.Strike> strikes = new ArrayList<>();
        private MeleeAnimationProfile.Selection selection = MeleeAnimationProfile.Selection.CYCLE;

        public ProfileBuilder(ResourceLocation family) {
            this.family = family;
        }

        public ProfileBuilder windup(ResourceLocation windup) {
            windups.add(windup);
            return this;
        }

        public ProfileBuilder windups(List<ResourceLocation> windups) {
            this.windups.addAll(windups);
            return this;
        }

        public ProfileBuilder strike(MeleeAnimationProfile.Strike strike) {
            strikes.add(strike);
            return this;
        }

        public ProfileBuilder strikes(List<MeleeAnimationProfile.Strike> strikes) {
            this.strikes.addAll(strikes);
            return this;
        }

        public ProfileBuilder selection(MeleeAnimationProfile.Selection selection) {
            this.selection = selection;
            return this;
        }

        public MeleeAnimationProfile build(ResourceLocation id) {
            return new MeleeAnimationProfile(id, family, List.copyOf(windups), List.copyOf(strikes), selection);
        }
    }

    public static class PoseBuilder {
        private final String type;
        private final MeleeAnimationPose.PoseOptions options;
        private final List<PoseChannel> channels = new ArrayList<>();

        public PoseBuilder(MeleeAnimationPose.PoseOptions options) {
            this("mkcore:model_pose", options);
        }

        public PoseBuilder(String type, MeleeAnimationPose.PoseOptions options) {
            this.type = type;
            this.options = options;
        }

        public List<PoseChannel> channels() {
            return channels;
        }

        public MeleeAnimationPose build() {
            return new MeleeAnimationPose(type, List.copyOf(channels), options);
        }
    }
}
