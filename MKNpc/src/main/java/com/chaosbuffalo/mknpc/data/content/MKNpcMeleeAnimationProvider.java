package com.chaosbuffalo.mknpc.data.content;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationProfile;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.PoseBuilder;
import com.chaosbuffalo.mknpc.client.render.animations.MKNpcMeleeAnimations;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
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
        registerGolemWeaponProfiles(output, futures);
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
                term("swing_inverse", sideSign * -0.32F), term("swing_sin", sideSign * 0.5F), term("follow_through", sideSign * 0.20F))));
        channels.add(channel("body", "xRot", "set", value(0.0F, term("swing_sin", 0.06F))));
        channels.add(channel(strikingArm, "xRot", "set", swingValue(-1.65F,
                term("swing_inverse", -0.55F), term("swing_sin", 0.75F), term("follow_through", 0.18F))));
        channels.add(channel(strikingArm, "yRot", "set", swingValue(0.0F,
                term("swing_inverse", sideSign * -1.05F), term("swing_sin", sideSign * 1.25F), term("follow_through", sideSign * 0.42F))));
        channels.add(channel(strikingArm, "zRot", "set", swingValue(sideSign * -0.25F,
                term("swing_sin", sideSign * -0.35F), term("follow_through", sideSign * 0.04F))));
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
                term("swing_inverse", -2.25F), term("swing_sin", -0.75F), term("follow_through_short", -0.55F))));
        channels.add(channel("leftArm", "xRot", "set", swingValue(0.0F,
                term("swing_inverse", -2.25F), term("swing_sin", -0.75F), term("follow_through_short", -0.55F))));
        channels.add(channel("rightArm", "yRot", "set", swingValue(0.0F,
                term("swing_inverse", 0.75F), term("swing_sin", -0.18F), term("follow_through_short", -0.14F))));
        channels.add(channel("leftArm", "yRot", "set", swingValue(0.0F,
                term("swing_inverse", -0.75F), term("swing_sin", 0.18F), term("follow_through_short", 0.14F))));
        channels.add(channel("rightArm", "zRot", "set", swingValue(0.0F,
                term("swing_inverse", 0.2F), term("swing_sin", -0.05F), term("follow_through_short", -0.03F))));
        channels.add(channel("leftArm", "zRot", "set", swingValue(0.0F,
                term("swing_inverse", -0.2F), term("swing_sin", 0.05F), term("follow_through_short", 0.03F))));
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

    private void registerGolemWeaponProfiles(CachedOutput output, List<CompletableFuture<?>> futures) {
        registerGolemLongsword(output, futures);
        registerGolemKatana(output, futures);
        registerGolemDagger(output, futures);
        registerGolemSpear(output, futures);
        registerGolemStaff(output, futures);
        registerGolemMace(output, futures);
        futures.add(saveProfile(output, golemWeaponProfileId(MeleeWeaponTypes.GREATSWORD_TYPE.getName()),
                aliasGolemWeaponProfile(golemWeaponProfileId(MeleeWeaponTypes.GREATSWORD_TYPE.getName()),
                        golemWeaponProfileId(MeleeWeaponTypes.LONGSWORD_TYPE.getName()), 0.56F)));
        futures.add(saveProfile(output, golemWeaponProfileId(MeleeWeaponTypes.WARHAMMER_TYPE.getName()),
                aliasGolemWeaponProfile(golemWeaponProfileId(MeleeWeaponTypes.WARHAMMER_TYPE.getName()),
                        golemWeaponProfileId(MeleeWeaponTypes.MACE_TYPE.getName()), 0.42F)));
        futures.add(saveProfile(output, golemWeaponProfileId(MeleeWeaponTypes.BATTLEAXE_TYPE.getName()),
                aliasGolemWeaponProfile(golemWeaponProfileId(MeleeWeaponTypes.BATTLEAXE_TYPE.getName()),
                        golemWeaponProfileId(MeleeWeaponTypes.MACE_TYPE.getName()), 0.44F)));
    }

    private void registerGolemLongsword(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation profileId = golemWeaponProfileId(MeleeWeaponTypes.LONGSWORD_TYPE.getName());
        ResourceLocation windup0 = poseId(profileId, "windup_0");
        ResourceLocation windup1 = poseId(profileId, "windup_1");
        ResourceLocation strike0 = poseId(profileId, "strike_0");
        ResourceLocation strike1 = poseId(profileId, "strike_1");
        ResourceLocation strike2 = poseId(profileId, "strike_2");
        futures.add(savePose(output, windup0, golemWeaponWindup(-1.85F, 0.42F, -0.78F, -0.14F, -0.10F, 0.08F, 0.06F, 0.18F)));
        futures.add(savePose(output, windup1, golemWeaponWindup(-1.72F, -0.38F, -0.92F, 0.16F, -0.08F, 0.08F, -0.04F, -0.16F)));
        futures.add(savePose(output, strike0, golemHorizontalSlash(1.0F, -1.42F, -0.72F, 1.72F, -0.78F, 0.22F)));
        futures.add(savePose(output, strike1, golemHorizontalSlash(-1.0F, -1.36F, -0.68F, 1.64F, -0.74F, 0.18F)));
        futures.add(savePose(output, strike2, golemOverheadStrike(-1.86F, -0.62F, 1.94F, -0.82F)));
        futures.add(saveProfile(output, profileId, profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(windup0, windup1))
                .strikes(List.of(strike(strike0, 0.42F), strike(strike1, 0.42F), strike(strike2, 0.48F)))
                .build(profileId)));
    }

    private void registerGolemKatana(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation profileId = golemWeaponProfileId(MeleeWeaponTypes.KATANA_TYPE.getName());
        ResourceLocation windup0 = poseId(profileId, "windup_0");
        ResourceLocation windup1 = poseId(profileId, "windup_1");
        ResourceLocation strike0 = poseId(profileId, "strike_0");
        ResourceLocation strike1 = poseId(profileId, "strike_1");
        ResourceLocation strike2 = poseId(profileId, "strike_2");
        futures.add(savePose(output, windup0, golemWeaponWindup(-1.96F, 0.34F, -0.86F, 0.12F, -0.08F, 0.10F, 0.04F, 0.14F)));
        futures.add(savePose(output, windup1, golemWeaponWindup(-1.82F, -0.28F, -0.82F, 0.18F, -0.06F, 0.08F, -0.02F, -0.12F)));
        futures.add(savePose(output, strike0, golemDrawCut(1.0F, -1.68F, 1.86F, -0.78F)));
        futures.add(savePose(output, strike1, golemDrawCut(-1.0F, -1.64F, 1.80F, -0.74F)));
        futures.add(savePose(output, strike2, golemThrust(1.0F, -1.26F, 1.58F, -0.58F)));
        futures.add(saveProfile(output, profileId, profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(windup0, windup1))
                .strikes(List.of(strike(strike0, 0.44F), strike(strike1, 0.44F), strike(strike2, 0.52F)))
                .build(profileId)));
    }

    private void registerGolemDagger(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation profileId = golemWeaponProfileId(MeleeWeaponTypes.DAGGER_TYPE.getName());
        ResourceLocation windup0 = poseId(profileId, "windup_0");
        ResourceLocation windup1 = poseId(profileId, "windup_1");
        ResourceLocation strike0 = poseId(profileId, "strike_0");
        ResourceLocation strike1 = poseId(profileId, "strike_1");
        ResourceLocation strike2 = poseId(profileId, "strike_2");
        futures.add(savePose(output, windup0, golemDaggerWindup(1.02F, 0.34F, -0.34F, -0.04F, 0.06F, 0.04F, -0.02F, 0.08F)));
        futures.add(savePose(output, windup1, golemDaggerWindup(0.92F, -0.30F, -0.38F, 0.04F, -0.05F, 0.04F, -0.02F, -0.08F)));
        futures.add(savePose(output, strike0, golemStab(1.0F, 0.24F, -1.48F, -0.58F)));
        futures.add(savePose(output, strike1, golemStab(-1.0F, 0.20F, -1.54F, -0.62F)));
        futures.add(savePose(output, strike2, golemStab(1.0F, 0.28F, -1.62F, -0.68F)));
        futures.add(saveProfile(output, profileId, profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(windup0, windup1))
                .strikes(List.of(strike(strike0, 0.50F), strike(strike1, 0.34F), strike(strike2, 0.56F)))
                .build(profileId)));
    }

    private void registerGolemSpear(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation profileId = golemWeaponProfileId(MeleeWeaponTypes.SPEAR_TYPE.getName());
        ResourceLocation windup0 = poseId(profileId, "windup_0");
        ResourceLocation windup1 = poseId(profileId, "windup_1");
        ResourceLocation strike0 = poseId(profileId, "strike_0");
        ResourceLocation strike1 = poseId(profileId, "strike_1");
        ResourceLocation strike2 = poseId(profileId, "strike_2");
        futures.add(savePose(output, windup0, golemSpearWindup(0.96F, 0.28F, -0.88F, -0.18F, 0.06F, 0.04F, -0.02F, 0.05F)));
        futures.add(savePose(output, windup1, golemSpearWindup(1.02F, -0.24F, -0.92F, 0.16F, -0.05F, 0.04F, -0.02F, -0.05F)));
        futures.add(savePose(output, strike0, golemPolearmThrust(1.0F, -1.72F, -1.26F, 1.64F)));
        futures.add(savePose(output, strike1, golemPolearmThrust(-1.0F, -1.78F, -1.32F, 1.70F)));
        futures.add(savePose(output, strike2, golemPolearmSweep(1.0F, -1.54F, -0.72F, 1.76F)));
        futures.add(saveProfile(output, profileId, profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(windup0, windup1))
                .strikes(List.of(strike(strike0, 0.66F), strike(strike1, 0.72F), strike(strike2, 0.58F)))
                .build(profileId)));
    }

    private void registerGolemStaff(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation profileId = golemWeaponProfileId(MeleeWeaponTypes.STAFF_TYPE.getName());
        ResourceLocation windup0 = poseId(profileId, "windup_0");
        ResourceLocation windup1 = poseId(profileId, "windup_1");
        ResourceLocation strike0 = poseId(profileId, "strike_0");
        ResourceLocation strike1 = poseId(profileId, "strike_1");
        ResourceLocation strike2 = poseId(profileId, "strike_2");
        futures.add(savePose(output, windup0, golemPolearmWindup(-1.86F, 0.48F, -1.18F, -0.34F, -0.10F, 0.06F)));
        futures.add(savePose(output, windup1, golemPolearmWindup(-1.86F, -0.48F, -1.18F, 0.34F, -0.10F, 0.06F)));
        futures.add(savePose(output, strike0, golemPolearmSweep(1.0F, -1.42F, -0.66F, 1.72F)));
        futures.add(savePose(output, strike1, golemPolearmSweep(-1.0F, -1.42F, -0.66F, 1.72F)));
        futures.add(savePose(output, strike2, golemOverheadStrike(-1.92F, -0.66F, 1.88F, -0.78F)));
        futures.add(saveProfile(output, profileId, profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(windup0, windup1))
                .strikes(List.of(strike(strike0, 0.48F), strike(strike1, 0.48F), strike(strike2, 0.52F)))
                .build(profileId)));
    }

    private void registerGolemMace(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation profileId = golemWeaponProfileId(MeleeWeaponTypes.MACE_TYPE.getName());
        ResourceLocation windup0 = poseId(profileId, "windup_0");
        ResourceLocation windup1 = poseId(profileId, "windup_1");
        ResourceLocation strike0 = poseId(profileId, "strike_0");
        ResourceLocation strike1 = poseId(profileId, "strike_1");
        ResourceLocation strike2 = poseId(profileId, "strike_2");
        futures.add(savePose(output, windup0, golemWeaponWindup(-1.92F, 0.24F, -0.88F, 0.10F, -0.08F, 0.08F, 0.04F, 0.12F)));
        futures.add(savePose(output, windup1, golemWeaponWindup(-2.04F, -0.18F, -0.92F, -0.10F, -0.06F, 0.06F, -0.02F, -0.12F)));
        futures.add(savePose(output, strike0, golemCrushStrike(1.0F, -1.98F, 1.96F, -0.72F)));
        futures.add(savePose(output, strike1, golemHorizontalSlash(-1.0F, -1.28F, -0.54F, 1.48F, -0.62F, 0.14F)));
        futures.add(savePose(output, strike2, golemCrushStrike(1.0F, -1.82F, 1.84F, -0.66F)));
        futures.add(saveProfile(output, profileId, profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(windup0, windup1))
                .strikes(List.of(strike(strike0, 0.40F), strike(strike1, 0.34F), strike(strike2, 0.42F)))
                .build(profileId)));
    }

    private ResourceLocation golemWeaponProfileId(ResourceLocation weaponTypeId) {
        return MKNpc.id("golem_" + weaponTypeId.getPath());
    }

    private ResourceLocation poseId(ResourceLocation profileId, String suffix) {
        return MKNpc.id(profileId.getPath() + "_" + suffix);
    }

    private MeleeAnimationProfile aliasGolemWeaponProfile(ResourceLocation id, ResourceLocation sourceId, float finalLunge) {
        return profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(List.of(poseId(sourceId, "windup_0"), poseId(sourceId, "windup_1")))
                .strikes(List.of(
                        strike(poseId(sourceId, "strike_0"), finalLunge - 0.14F),
                        strike(poseId(sourceId, "strike_1"), finalLunge - 0.14F),
                        strike(poseId(sourceId, "strike_2"), finalLunge)
                ))
                .build(id);
    }

    private MeleeAnimationPose golemWeaponWindup(float mainArmX, float mainArmY, float offArmX, float offArmY,
                                                 float bodyY, float bodyX, float headX, float mainArmZ) {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY * 1.35F))));
        channels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(-0.42F, term("windup_sin", mainArmX * 1.10F))));
        channels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY * 0.55F))));
        channels.add(channel("main_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmZ * 0.40F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-0.10F, term("windup_sin", offArmX * 0.55F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.02F, term("windup_sin", offArmY * 0.28F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.06F))));
        channels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return pose.build();
    }

    private MeleeAnimationPose golemDaggerWindup(float mainArmX, float mainArmY, float offArmX, float offArmY,
                                                 float bodyY, float bodyX, float headX, float mainArmZ) {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY * 1.25F))));
        channels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(0.16F, term("windup_sin", mainArmX * 1.05F))));
        channels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY * 0.48F))));
        channels.add(channel("main_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmZ * 0.34F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-0.08F, term("windup_sin", offArmX * 0.42F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", offArmY * 0.18F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.03F))));
        channels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return pose.build();
    }

    private MeleeAnimationPose golemPolearmWindup(float mainArmX, float mainArmY, float offArmX, float offArmY,
                                                  float bodyY, float headX) {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY * 1.45F))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(-0.38F, term("windup_sin", mainArmX * 1.05F))));
        channels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY * 0.40F))));
        channels.add(channel("main_arm", "zRot", "set", "handedness", value(0.03F, term("windup_sin", 0.05F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-0.14F, term("windup_sin", offArmX * 0.52F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.04F, term("windup_sin", offArmY * 0.24F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.02F, term("windup_sin", 0.04F))));
        channels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return pose.build();
    }

    private MeleeAnimationPose golemSpearWindup(float mainArmX, float mainArmY, float offArmX, float offArmY,
                                                float bodyY, float bodyX, float headX, float mainArmZ) {
        PoseBuilder pose = pose(false, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY * 1.20F))));
        channels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        channels.add(channel("main_arm", "xRot", "set", "none", value(0.10F, term("windup_sin", mainArmX * 0.96F))));
        channels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY * 0.40F))));
        channels.add(channel("main_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmZ * 0.28F))));
        channels.add(channel("off_arm", "xRot", "set", "none", value(-0.06F, term("windup_sin", offArmX * 0.50F))));
        channels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", offArmY * 0.20F))));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.03F))));
        channels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return pose.build();
    }

    private MeleeAnimationPose golemHorizontalSlash(float swingDirection, float mainPitchBase, float mainYawBase,
                                                    float mainPitchFollow, float offPitchBase, float bodyFollow, float mainRollBase) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.20F, 0.32F, 0.0F, bodyFollow + 0.10F)));
        channels.add(channel("body", "zRot", "set", "handedness*swingDirection", swingValue(0.01F, 0.04F, 0.0F, 0.03F)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(mainYawBase * 0.22F, 0.18F, 0.0F, 0.10F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.28F, 0.60F, mainPitchFollow)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(mainRollBase * 0.42F, -0.08F, 0.0F, 0.05F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.10F, 0.12F, 0.0F, 0.05F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.16F, -0.10F, 0.08F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.03F, 0.05F, 0.0F, 0.02F)));
        channels.add(channel("head", "xRot", "add", "none", swingValue(0.0F, 0.04F, 0.0F, 0.06F)));
        return pose.build();
    }

    private MeleeAnimationPose golemHorizontalSlash(float swingDirection, float mainPitchBase, float mainYawBase,
                                                    float mainPitchFollow, float offPitchBase, float bodyFollow) {
        return golemHorizontalSlash(swingDirection, mainPitchBase, mainYawBase, mainPitchFollow, offPitchBase, bodyFollow, 0.18F);
    }

    private MeleeAnimationPose golemOverheadStrike(float mainPitchBase, float mainPitchSwing, float mainPitchFollow, float offPitchBase) {
        PoseBuilder pose = pose(true, 1.0F);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.12F, 0.18F, 0.0F, 0.18F)));
        channels.add(channel("body", "xRot", "add", "none", swingValue(0.0F, 0.08F, 0.0F, -0.06F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, mainPitchSwing, -0.46F, mainPitchFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(0.05F, 0.08F, 0.0F, 0.08F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.05F, 0.05F, 0.0F, 0.03F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.16F, -0.08F, 0.08F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.08F, 0.08F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.02F, 0.03F, 0.0F, 0.02F)));
        channels.add(channel("head", "xRot", "add", "none", swingValue(0.0F, 0.06F, 0.0F, 0.08F)));
        return pose.build();
    }

    private MeleeAnimationPose golemDrawCut(float swingDirection, float mainPitchBase, float mainPitchFollow, float offPitchBase) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.18F, 0.24F, 0.0F, 0.18F)));
        channels.add(channel("body", "zRot", "set", "handedness*swingDirection", swingValue(0.00F, 0.03F, 0.0F, 0.02F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.22F, 0.54F, mainPitchFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(0.04F, 0.09F, 0.0F, 0.08F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.06F, -0.06F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.14F, -0.08F, 0.06F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.08F, 0.08F, 0.0F, 0.04F)));
        channels.add(channel("head", "xRot", "add", "none", swingValue(0.0F, 0.04F, 0.0F, 0.06F)));
        return pose.build();
    }

    private MeleeAnimationPose golemThrust(float swingDirection, float mainPitchBase, float mainPitchFollow, float offPitchBase) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.14F, 0.18F, 0.0F, 0.18F)));
        channels.add(channel("body", "xRot", "add", "none", swingValue(0.0F, -0.06F, 0.0F, -0.06F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.18F, -0.52F, mainPitchFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(0.03F, 0.05F, 0.0F, 0.05F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.03F, 0.03F, 0.0F, 0.02F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.18F, -0.10F, 0.06F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.08F, 0.07F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.02F, 0.03F, 0.0F, 0.02F)));
        return pose.build();
    }

    private MeleeAnimationPose golemStab(float swingDirection, float bodySwing, float mainPitchBase, float offPitchBase) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.16F, bodySwing * 1.35F, 0.0F, 0.16F)));
        channels.add(channel("body", "xRot", "add", "none", swingValue(0.02F, 0.06F, 0.0F, -0.04F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.10F, -0.42F, 1.42F)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(0.07F, 0.10F, 0.0F, 0.08F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.06F, 0.06F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.16F, -0.12F, 0.08F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.09F, 0.08F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.02F, 0.03F, 0.0F, 0.02F)));
        return pose.build();
    }

    private MeleeAnimationPose golemPolearmThrust(float swingDirection, float mainPitchBase, float offPitchBase, float mainPitchFollow) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.16F, 0.26F, 0.0F, 0.22F)));
        channels.add(channel("body", "xRot", "add", "none", swingValue(0.02F, -0.08F, 0.0F, -0.08F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.16F, -0.58F, mainPitchFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(0.03F, 0.06F, 0.0F, 0.06F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.04F, 0.04F, 0.0F, 0.03F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.22F, -0.14F, 0.08F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.10F, 0.10F, 0.0F, 0.05F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.05F, 0.05F, 0.0F, 0.02F)));
        return pose.build();
    }

    private MeleeAnimationPose golemPolearmSweep(float swingDirection, float mainPitchBase, float offPitchBase, float mainPitchFollow) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.24F, 0.36F, 0.0F, 0.28F)));
        channels.add(channel("body", "zRot", "set", "handedness*swingDirection", swingValue(0.02F, 0.05F, 0.0F, 0.04F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.26F, 0.36F, mainPitchFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(-0.14F, 0.22F, 0.0F, 0.12F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.05F, -0.07F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.20F, -0.10F, 0.08F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.10F, 0.10F, 0.0F, 0.05F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.05F, 0.05F, 0.0F, 0.02F)));
        channels.add(channel("head", "xRot", "add", "none", swingValue(0.0F, 0.05F, 0.0F, 0.06F)));
        return pose.build();
    }

    private MeleeAnimationPose golemCrushStrike(float swingDirection, float mainPitchBase, float mainPitchFollow, float offPitchBase) {
        PoseBuilder pose = pose(true, swingDirection);
        List<PoseChannel> channels = pose.channels();
        channels.add(channel("body", "yRot", "set", "-handedness*swingDirection", swingValue(0.18F, 0.22F, 0.0F, 0.18F)));
        channels.add(channel("body", "xRot", "add", "none", swingValue(0.0F, 0.08F, 0.0F, -0.06F)));
        channels.add(channel("main_arm", "xRot", "set", "none", swingValue(mainPitchBase, -0.28F, -0.48F, mainPitchFollow)));
        channels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection", swingValue(0.05F, 0.08F, 0.0F, 0.07F)));
        channels.add(channel("main_arm", "zRot", "set", "handedness", swingValue(0.05F, 0.05F, 0.0F, 0.03F)));
        channels.add(channel("off_arm", "xRot", "set", "none", swingValue(offPitchBase, 0.16F, -0.08F, 0.06F)));
        channels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection", swingValue(0.09F, 0.08F, 0.0F, 0.04F)));
        channels.add(channel("off_arm", "zRot", "set", "-handedness", swingValue(0.03F, 0.03F, 0.0F, 0.02F)));
        return pose.build();
    }

    private PoseChannel.PoseValue swingValue(float constant, PoseChannel.PoseTerm... terms) {
        return value(constant, terms);
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
        return "MKNpc Melee Animation Assets";
    }
}
