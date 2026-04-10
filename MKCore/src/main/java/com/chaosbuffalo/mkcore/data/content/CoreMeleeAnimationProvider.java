package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationProfile;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.PoseBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.channel;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.pose;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.strike;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.term;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.value;

public class CoreMeleeAnimationProvider implements DataProvider {
    private final PackOutput.PathProvider profilePathProvider;
    private final PackOutput.PathProvider posePathProvider;

    public CoreMeleeAnimationProvider(PackOutput output) {
        this.profilePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.PROFILE_FOLDER);
        this.posePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.POSE_FOLDER);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(saveProfile(output, MKCore.id("biped_default"), bipedDefaultProfile()));
        futures.add(savePose(output, MKCore.id("biped_default_windup"), bipedDefaultWindup()));
        futures.add(savePose(output, MKCore.id("biped_default_slash_0"), bipedDefaultSlash0()));
        futures.add(savePose(output, MKCore.id("biped_default_slash_1"), bipedDefaultSlash1()));
        futures.add(savePose(output, MKCore.id("biped_default_slash_2"), bipedDefaultSlash2()));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveProfile(CachedOutput output, ResourceLocation id, MeleeAnimationProfile profile) {
        Path path = profilePathProvider.json(id);
        return DataProvider.saveStable(output, MeleeAnimationProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile).getOrThrow(), path);
    }

    private CompletableFuture<?> savePose(CachedOutput output, ResourceLocation id, MeleeAnimationPose pose) {
        Path path = posePathProvider.json(id);
        return DataProvider.saveStable(output, MeleeAnimationPose.CODEC.encodeStart(JsonOps.INSTANCE, pose).getOrThrow(), path);
    }

    private MeleeAnimationProfile bipedDefaultProfile() {
        return MeleeAnimationBuilder.profile(MeleeAnimationManager.BIPED_FAMILY)
                .windup(MKCore.id("biped_default_windup"))
                .strikes(List.of(
                        strike(MKCore.id("biped_default_slash_0"), 0.5F),
                        strike(MKCore.id("biped_default_slash_1"), 0.5F),
                        strike(MKCore.id("biped_default_slash_2"), 0.5F)
                ))
                .build(MKCore.id("biped_default"));
    }

    private MeleeAnimationPose bipedDefaultWindup() {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("main_arm", "xRot", "set", "none", value(-0.25F, term("windup_sin", -2.75F))));
        channels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", 0.6F))));
        channels.add(channel("main_arm", "zRot", "set", "none", value(0.0F)));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-0.25F, term("windup_sin", -1.75F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", 0.4F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.08F))));
        channels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", -0.12F))));
        channels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", 0.05F))));
        channels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", 0.1F))));
        return pose.build();
    }

    private MeleeAnimationPose bipedDefaultSlash0() {
        return bipedSwingPose(1.0F,
                0.12F, 0.16F, 0.11F,
                0.24F, 0.22F, 0.12F,
                -1.7F, -0.85F, -0.55F, 2.45F,
                0.4F, 0.3F, 0.18F, "handedness",
                0.08F, 0.1F,
                -0.95F, 0.1F, -0.08F,
                0.06F, 0.06F, "-handedness",
                0.06F, 0.08F, 0.2F);
    }

    private MeleeAnimationPose bipedDefaultSlash1() {
        return bipedSwingPose(1.0F,
                0.03F, 0.05F, 0.04F,
                0.04F, 0.06F, 0.04F,
                -2.3F, -1.0F, -0.75F, 2.85F,
                0.08F, 0.06F, 0.06F, "handedness",
                0.05F, 0.08F,
                -1.05F, 0.05F, -0.04F,
                0.05F, 0.04F, "-handedness",
                0.04F, 0.1F, 0.12F);
    }

    private MeleeAnimationPose bipedDefaultSlash2() {
        return bipedSwingPose(-1.0F,
                0.12F, 0.16F, 0.11F,
                0.24F, 0.22F, 0.12F,
                -1.7F, -0.85F, -0.55F, 2.45F,
                0.4F, 0.3F, 0.18F, "-handedness",
                0.1F, 0.12F,
                -0.9F, 0.08F, -0.06F,
                0.08F, 0.06F, "handedness",
                0.06F, 0.08F, 0.24F);
    }

    private MeleeAnimationPose bipedSwingPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                      float mainYawBase, float mainYawSwing, float mainYawFollow,
                                      float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                      float mainRollBase, float mainRollSwing, float mainRollFollow, String mainRollSign,
                                      float offYawBase, float offYawSwing,
                                      float offPitchBase, float offPitchSwing, float offPitchImpact,
                                      float offRollBase, float offRollSwing, String offRollSign,
                                      float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection",
                swingValue(bodyBase, bodySwing, 0.0F, bodyFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection",
                swingValue(mainYawBase, mainYawSwing, 0.0F, mainYawFollow)));
        channels.add(channel("main_arm", "xRot", "set", "none",
                swingValue(mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow)));
        channels.add(channel("main_arm", "zRot", "set", mainRollSign,
                swingValue(mainRollBase, mainRollSwing, 0.0F, mainRollFollow)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection",
                swingValue(offYawBase, offYawSwing, 0.0F, 0.0F)));
        channels.add(channel("off_arm", "xRot", "set", "none",
                swingValue(offPitchBase, offPitchSwing, offPitchImpact, 0.0F)));
        channels.add(channel("off_arm", "zRot", "set", offRollSign,
                swingValue(offRollBase, offRollSwing, 0.0F, 0.0F)));
        channels.add(channel("head", "xRot", "add", "none",
                swingValue(0.0F, headPitchSwing, 0.0F, headPitchFollow)));
        channels.add(channel("head", "yRot", "add", "-handedness*swingDirection",
                swingValue(bodyBase * headYawFollowScale, bodySwing * headYawFollowScale, 0.0F, bodyFollow * headYawFollowScale)));
        return pose.build();
    }

    private PoseChannel.PoseValue swingValue(float constant, float swing, float impact, float follow) {
        List<PoseChannel.PoseTerm> terms = new ArrayList<>();
        if (swing != 0.0F) {
            terms.add(term("swing_sin", swing));
        }
        if (impact != 0.0F) {
            terms.add(term("impact", impact));
        }
        if (follow != 0.0F) {
            terms.add(term("follow_through", follow));
        }
        return new PoseChannel.PoseValue(constant, List.copyOf(terms));
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKCore Melee Animation Assets";
    }
}
