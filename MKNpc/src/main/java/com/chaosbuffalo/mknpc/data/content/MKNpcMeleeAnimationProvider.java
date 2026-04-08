package com.chaosbuffalo.mknpc.data.content;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationProfile;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.PoseBuilder;
import com.chaosbuffalo.mknpc.client.render.animations.MKNpcMeleeAnimations;
import com.chaosbuffalo.mknpc.MKNpc;
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

import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.ageTerm;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.channel;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.inputTerm;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.pose;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.profile;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.strike;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.term;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.value;

public class MKNpcMeleeAnimationProvider implements DataProvider {
    private final PackOutput.PathProvider profilePathProvider;
    private final PackOutput.PathProvider posePathProvider;

    public MKNpcMeleeAnimationProvider(PackOutput output) {
        this.profilePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.PROFILE_FOLDER);
        this.posePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.POSE_FOLDER);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(saveProfile(output, MKNpc.id("golem_default"), golemDefaultProfile()));
        futures.add(savePose(output, MKNpc.id("golem_roundhouse_right_windup"), golemRoundhouseWindup(-1.0F, "rightArm", "leftArm")));
        futures.add(savePose(output, MKNpc.id("golem_roundhouse_left_windup"), golemRoundhouseWindup(1.0F, "leftArm", "rightArm")));
        futures.add(savePose(output, MKNpc.id("golem_double_smash_windup"), golemDoubleSmashWindup()));
        futures.add(savePose(output, MKNpc.id("golem_roundhouse_right"), golemRoundhouseAttack(-1.0F, "rightArm", "leftArm")));
        futures.add(savePose(output, MKNpc.id("golem_roundhouse_left"), golemRoundhouseAttack(1.0F, "leftArm", "rightArm")));
        futures.add(savePose(output, MKNpc.id("golem_double_smash"), golemDoubleSmashAttack()));

        futures.add(saveProfile(output, MKNpc.id("skull_default"), skullDefaultProfile()));
        futures.add(savePose(output, MKNpc.id("skull_default_idle"), skullIdle()));
        futures.add(savePose(output, MKNpc.id("skull_default_windup"), skullWindup()));
        futures.add(savePose(output, MKNpc.id("skull_default_bite"), skullBite()));
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

    private MeleeAnimationProfile golemDefaultProfile() {
        return profile(MKNpcMeleeAnimations.GOLEM_FAMILY)
                .windups(List.of(
                        MKNpc.id("golem_roundhouse_right_windup"),
                        MKNpc.id("golem_roundhouse_left_windup"),
                        MKNpc.id("golem_double_smash_windup")
                ))
                .strikes(List.of(
                        strike(MKNpc.id("golem_roundhouse_right"), 0.5F),
                        strike(MKNpc.id("golem_roundhouse_left"), 0.5F),
                        strike(MKNpc.id("golem_double_smash"), 0.55F)
                ))
                .build(MKNpc.id("golem_default"));
    }

    private MeleeAnimationProfile skullDefaultProfile() {
        return profile(MKNpcMeleeAnimations.SKULL_FAMILY)
                .windup(MKNpc.id("skull_default_windup"))
                .strike(strike(MKNpc.id("skull_default_bite"), 0.65F))
                .build(MKNpc.id("skull_default"));
    }

    private MeleeAnimationPose golemRoundhouseWindup(float sideSign, String strikingArm, String counterArm) {
        PoseBuilder pose = pose();
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", value(0.0F, term("windup_sin", sideSign * -0.34F))));
        channels.add(channel("body", "xRot", "set", value(0.0F, term("windup_sin", -0.08F))));
        channels.add(channel(strikingArm, "xRot", "set", value(0.0F, term("windup_sin", -2.15F))));
        channels.add(channel(strikingArm, "yRot", "set", value(0.0F, term("windup_sin", sideSign * -1.05F))));
        channels.add(channel(strikingArm, "zRot", "set", value(0.0F, term("windup_sin", sideSign * 0.28F))));
        channels.add(channel(counterArm, "xRot", "set", value(0.0F, term("windup_sin", -0.4F))));
        channels.add(channel(counterArm, "yRot", "set", value(0.0F, term("windup_sin", sideSign * 0.35F))));
        channels.add(channel(counterArm, "zRot", "set", value(0.0F, term("windup_sin", sideSign * 0.08F))));
        return pose.build();
    }

    private MeleeAnimationPose golemDoubleSmashWindup() {
        PoseBuilder pose = pose();
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "xRot", "set", value(0.0F, term("windup_sin", -0.2F))));
        channels.add(channel("rightArm", "xRot", "set", value(0.0F, term("windup_sin", -2.25F))));
        channels.add(channel("leftArm", "xRot", "set", value(0.0F, term("windup_sin", -2.25F))));
        channels.add(channel("rightArm", "yRot", "set", value(0.0F, term("windup_sin", 0.75F))));
        channels.add(channel("leftArm", "yRot", "set", value(0.0F, term("windup_sin", -0.75F))));
        channels.add(channel("rightArm", "zRot", "set", value(0.0F, term("windup_sin", 0.2F))));
        channels.add(channel("leftArm", "zRot", "set", value(0.0F, term("windup_sin", -0.2F))));
        return pose.build();
    }

    private MeleeAnimationPose golemRoundhouseAttack(float sideSign, String strikingArm, String counterArm) {
        PoseBuilder pose = pose();
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", swingValue(0.0F,
                term("swing_inverse", sideSign * -0.32F), term("swing_sin", sideSign * 0.5F), term("follow_through", sideSign * 0.34F))));
        channels.add(channel("body", "xRot", "set", value(0.0F, term("swing_sin", 0.06F))));
        channels.add(channel(strikingArm, "xRot", "set", swingValue(-1.65F,
                term("swing_inverse", -0.55F), term("swing_sin", 0.75F), term("follow_through", 0.35F))));
        channels.add(channel(strikingArm, "yRot", "set", swingValue(0.0F,
                term("swing_inverse", sideSign * -1.05F), term("swing_sin", sideSign * 1.25F), term("follow_through", sideSign * 0.85F))));
        channels.add(channel(strikingArm, "zRot", "set", swingValue(sideSign * -0.25F,
                term("swing_sin", sideSign * -0.35F), term("follow_through", sideSign * 0.1F))));
        channels.add(channel(counterArm, "xRot", "set", value(-0.15F, term("swing_sin", -0.1F))));
        channels.add(channel(counterArm, "yRot", "set", value(sideSign * -0.35F, term("swing_sin", sideSign * -0.15F))));
        channels.add(channel(counterArm, "zRot", "set", value(sideSign * -0.12F)));
        return pose.build();
    }

    private MeleeAnimationPose golemDoubleSmashAttack() {
        PoseBuilder pose = pose();
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "xRot", "set", swingValue(0.0F,
                term("swing_inverse", -0.2F), term("swing_sin", 0.12F), term("follow_through_short", 0.08F))));
        channels.add(channel("rightArm", "xRot", "set", swingValue(0.0F,
                term("swing_inverse", -2.25F), term("swing_sin", -0.75F), term("follow_through_short", -0.85F))));
        channels.add(channel("leftArm", "xRot", "set", swingValue(0.0F,
                term("swing_inverse", -2.25F), term("swing_sin", -0.75F), term("follow_through_short", -0.85F))));
        channels.add(channel("rightArm", "yRot", "set", swingValue(0.0F,
                term("swing_inverse", 0.75F), term("swing_sin", -0.18F), term("follow_through_short", -0.28F))));
        channels.add(channel("leftArm", "yRot", "set", swingValue(0.0F,
                term("swing_inverse", -0.75F), term("swing_sin", 0.18F), term("follow_through_short", 0.28F))));
        channels.add(channel("rightArm", "zRot", "set", swingValue(0.0F,
                term("swing_inverse", 0.2F), term("swing_sin", -0.05F), term("follow_through_short", -0.08F))));
        channels.add(channel("leftArm", "zRot", "set", swingValue(0.0F,
                term("swing_inverse", -0.2F), term("swing_sin", 0.05F), term("follow_through_short", 0.08F))));
        return pose.build();
    }

    private MeleeAnimationPose skullIdle() {
        PoseBuilder pose = pose();
        pose.channels().add(channel("jaw", "xRot", "max", value(0.0F, ageTerm(0.2F, 1.0F, 0.15F, false, ""))));
        return pose.build();
    }

    private MeleeAnimationPose skullWindup() {
        PoseBuilder pose = pose();
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("head", "xRot", "set", value(0.0F,
                inputTerm("headPitch", 1.0F, ""), inputTerm("headPitch", -1.0F, "windup_sin"), term("windup_sin", -0.7853982F))));
        channels.add(channel("head", "y", "set", value(20.0F, term("windup_sin", -1.0F))));
        channels.add(channel("head", "z", "set", value(0.0F, term("windup_sin", 1.75F))));
        channels.add(channel("jaw", "xRot", "max", value(0.0F, ageTerm(0.2F, 1.0F, 0.15F, false, ""))));
        channels.add(channel("jaw", "xRot", "max", value(0.0F,
                term("windup_sin", 0.45F), ageTerm(2.8F, 0.0F, 0.4F, true, "windup_sin"))));
        return pose.build();
    }

    private MeleeAnimationPose skullBite() {
        PoseBuilder pose = pose();
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("head", "xRot", "add", value(0.0F, term("swing_sin", 0.3F))));
        channels.add(channel("jaw", "xRot", "max", value(0.0F, ageTerm(0.2F, 1.0F, 0.15F, false, ""))));
        channels.add(channel("jaw", "xRot", "max", value(0.0F, term("swing_sin", 1.5F))));
        return pose.build();
    }

    private PoseChannel.PoseValue swingValue(float constant, PoseChannel.PoseTerm... terms) {
        return value(constant, terms);
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKNpc Melee Animation Assets";
    }
}
