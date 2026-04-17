package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationProfile;
import com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.PoseBuilder;
import com.chaosbuffalo.mkultra.MKUltra;
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
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.term;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.value;

public class MKUSpellAnimationProvider implements DataProvider {
    private static final ResourceLocation CAST_SLAM_CATEGORY = MKUltra.id("cast_slam");
    private static final ResourceLocation CAST_SLAM_PROFILE = MKUltra.id("fire_elemental_slam_cast");
    private static final ResourceLocation CAST_SLAM_CASTING_POSE = MKUltra.id("fire_elemental_slam_casting_pose");
    private static final ResourceLocation CAST_SLAM_RELEASE_POSE = MKUltra.id("fire_elemental_slam_release_pose");

    private final PackOutput.PathProvider profilePathProvider;
    private final PackOutput.PathProvider posePathProvider;

    public MKUSpellAnimationProvider(PackOutput output) {
        this.profilePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, SpellAnimationManager.PROFILE_FOLDER);
        this.posePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.POSE_FOLDER);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(saveProfile(output, CAST_SLAM_PROFILE, castSlamProfile()));
        futures.add(savePose(output, CAST_SLAM_CASTING_POSE, castSlamCastingPose()));
        futures.add(savePose(output, CAST_SLAM_RELEASE_POSE, castSlamReleasePose()));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveProfile(CachedOutput output, ResourceLocation id, SpellAnimationProfile profile) {
        Path path = profilePathProvider.json(id);
        return DataProvider.saveStable(output, SpellAnimationProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile).getOrThrow(), path);
    }

    private CompletableFuture<?> savePose(CachedOutput output, ResourceLocation id, MeleeAnimationPose pose) {
        Path path = posePathProvider.json(id);
        return DataProvider.saveStable(output, MeleeAnimationPose.CODEC.encodeStart(JsonOps.INSTANCE, pose).getOrThrow(), path);
    }

    private SpellAnimationProfile castSlamProfile() {
        return new SpellAnimationProfile(CAST_SLAM_PROFILE, SpellAnimationManager.BIPED_FAMILY, CAST_SLAM_CATEGORY,
                CAST_SLAM_CASTING_POSE, CAST_SLAM_RELEASE_POSE);
    }

    private MeleeAnimationPose castSlamCastingPose() {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "xRot", "add", "none", value(0.06F, term("windup_sin", -0.24F))));
        channels.add(channel("body", "y", "add", "none", value(0.0F, term("windup_sin", -0.45F))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(0.0F, term("windup_sin", -2.55F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(0.0F, term("windup_sin", -2.55F))));
        channels.add(channel("main_arm", "yRot", "set", "-handedness", value(0.12F, term("windup_sin", 0.42F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.12F, term("windup_sin", 0.42F))));
        channels.add(channel("main_arm", "zRot", "set", "handedness", value(0.04F, term("windup_sin", 0.08F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.04F, term("windup_sin", 0.08F))));
        channels.add(channel("neck", "xRot", "add", "none", value(-0.02F, term("windup_sin", -0.14F))));
        return pose.build();
    }

    private MeleeAnimationPose castSlamReleasePose() {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "xRot", "set", "none", value(0.22F, term("release_arc", -0.26F))));
        channels.add(channel("body", "y", "add", "none", value(5.2F, term("release_arc", -2.1F))));
        channels.add(channel("body", "z", "add", "none", value(0.55F, term("release_arc", -1.1F))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(-1.58F, term("release_arc", 0.24F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-1.58F, term("release_arc", 0.24F))));
        channels.add(channel("main_arm", "yRot", "set", "-handedness", value(0.18F, term("release_arc", -0.03F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.18F, term("release_arc", -0.03F))));
        channels.add(channel("main_arm", "zRot", "set", "handedness", value(0.03F, term("release_arc", -0.02F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.03F, term("release_arc", -0.02F))));
        channels.add(channel("main_arm", "y", "add", "none", value(4.8F, term("release_arc", -1.7F))));
        channels.add(channel("off_arm", "y", "add", "none", value(4.8F, term("release_arc", -1.7F))));
        channels.add(channel("neck", "xRot", "add", "none", value(0.52F, term("release_arc", -0.26F))));
        channels.add(channel("neck", "y", "add", "none", value(7.5F, term("release_arc", -2.2F))));
        channels.add(channel("neck", "z", "add", "none", value(0.18F, term("release_arc", -0.12F))));
        channels.add(channel("vortex_top", "xRot", "add", "none", value(0.18F, term("release_arc", -0.12F))));
        channels.add(channel("vortex_mid", "xRot", "add", "none", value(0.08F, term("release_arc", -0.04F))));
        channels.add(channel("vortex_top", "y", "add", "none", value(4.55F, term("release_arc", -1.8F))));
        channels.add(channel("vortex_top", "z", "add", "none", value(2.92F, term("release_arc", -1.42F))));
        channels.add(channel("vortex_mid", "y", "add", "none", value(3.15F, term("release_arc", -0.58F))));
        channels.add(channel("vortex_mid", "z", "add", "none", value(0.34F, term("release_arc", -0.52F))));
        return pose.build();
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKUltra Spell Animation Assets";
    }
}
