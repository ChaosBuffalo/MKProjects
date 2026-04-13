package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationProfile;
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
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.term;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.value;

public class CoreSpellAnimationProvider implements DataProvider {
    private final PackOutput.PathProvider profilePathProvider;
    private final PackOutput.PathProvider posePathProvider;

    public CoreSpellAnimationProvider(PackOutput output) {
        this.profilePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, SpellAnimationManager.PROFILE_FOLDER);
        this.posePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.POSE_FOLDER);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(saveProfile(output, MKCore.id("biped_default_cast"), bipedDefaultProfile()));
        futures.add(savePose(output, MKCore.id("biped_default_casting_pose"), bipedDefaultCastingPose()));
        futures.add(savePose(output, MKCore.id("biped_default_release_pose"), bipedDefaultReleasePose()));
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

    private SpellAnimationProfile bipedDefaultProfile() {
        return new SpellAnimationProfile(MKCore.id("biped_default_cast"), SpellAnimationManager.BIPED_FAMILY,
                SpellAnimationManager.DEFAULT_CATEGORY, MKCore.id("biped_default_casting_pose"),
                MKCore.id("biped_default_release_pose"));
    }

    private MeleeAnimationPose bipedDefaultCastingPose() {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("main_arm", "yRot", "set", "none", value(0.0F)));
        channels.add(channel("off_arm", "yRot", "set", "none", value(0.0F)));
        channels.add(channel("main_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.7853982F))));
        channels.add(channel("off_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", 0.7853982F))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(-1.5707964F, term("windup_sin", -0.2617994F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-1.5707964F, term("windup_sin", -0.2617994F))));
        channels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", 0.08F))));
        return pose.build();
    }

    private MeleeAnimationPose bipedDefaultReleasePose() {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("main_arm", "yRot", "set", "none", value(0.0F)));
        channels.add(channel("off_arm", "yRot", "set", "none", value(0.0F)));
        channels.add(channel("main_arm", "zRot", "set", "-handedness", value(0.0F, term("release_arc", 1.0F))));
        channels.add(channel("off_arm", "zRot", "set", "handedness", value(0.0F, term("release_arc", 1.0F))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(0.0F)));
        channels.add(channel("off_arm", "xRot", "set", "none", value(0.0F)));
        return pose.build();
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKCore Spell Animation Assets";
    }
}
