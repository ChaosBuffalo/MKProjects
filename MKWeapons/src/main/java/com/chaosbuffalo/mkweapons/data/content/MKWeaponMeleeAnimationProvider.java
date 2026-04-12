package com.chaosbuffalo.mkweapons.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationProfile;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.PoseChannel;
import com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.PoseBuilder;
import com.chaosbuffalo.mkweapons.MKWeapons;
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

import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.channel;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.pose;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.profile;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.strike;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.term;
import static com.chaosbuffalo.mkcore.data.content.MeleeAnimationBuilder.value;

public class MKWeaponMeleeAnimationProvider implements DataProvider {
    private final PackOutput.PathProvider profilePathProvider;
    private final PackOutput.PathProvider posePathProvider;

    public MKWeaponMeleeAnimationProvider(PackOutput output) {
        this.profilePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.PROFILE_FOLDER);
        this.posePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, MeleeAnimationManager.POSE_FOLDER);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        registerLongsword(output, futures);
        registerKatana(output, futures);
        registerDagger(output, futures);
        registerSpear(output, futures);
        registerStaff(output, futures);
        registerMace(output, futures);

        futures.add(saveProfile(output, MeleeWeaponTypes.GREATSWORD_TYPE.getName(), defaultProfile(MeleeWeaponTypes.GREATSWORD_TYPE.getName(), 3)));
        futures.add(saveProfile(output, MeleeWeaponTypes.WARHAMMER_TYPE.getName(), defaultProfile(MeleeWeaponTypes.WARHAMMER_TYPE.getName(), 3)));
        futures.add(saveProfile(output, MeleeWeaponTypes.BATTLEAXE_TYPE.getName(), defaultProfile(MeleeWeaponTypes.BATTLEAXE_TYPE.getName(), 3)));
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

    private MeleeAnimationProfile defaultProfile(ResourceLocation id, int strikeCount) {
        ResourceLocation[] defaults = new ResourceLocation[]{
                MKCore.id("biped_default_slash_0"),
                MKCore.id("biped_default_slash_1"),
                MKCore.id("biped_default_slash_2")
        };
        List<MeleeAnimationProfile.Strike> strikes = new ArrayList<>();
        for (int i = 0; i < strikeCount; i++) {
            strikes.add(strike(defaults[i % defaults.length], 0.5F));
        }
        return profile(MeleeAnimationManager.BIPED_FAMILY)
                .windup(MKCore.id("biped_default_windup"))
                .strikes(strikes)
                .build(id);
    }

    private void registerLongsword(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation windup0 = MKWeapons.id("longsword_windup_0");
        ResourceLocation windup1 = MKWeapons.id("longsword_windup_1");
        ResourceLocation strike0 = MKWeapons.id("longsword_strike_0");
        ResourceLocation strike1 = MKWeapons.id("longsword_strike_1");
        ResourceLocation strike2 = MKWeapons.id("longsword_strike_2");
        ResourceLocation strike3 = MKWeapons.id("longsword_strike_3");
        ResourceLocation strike4 = MKWeapons.id("longsword_strike_4");

        futures.add(savePose(output, windup0, weaponWindup(-2.35F, 0.80F, -0.90F, -0.15F, 0.14F, -0.08F, 0.08F, 0.0F, -0.04F)));
        futures.add(savePose(output, windup1, weaponWindup(-2.10F, -0.70F, -1.00F, 0.12F, -0.12F, -0.08F, 0.06F, 0.0F, 0.02F)));
        futures.add(savePose(output, strike0, slashPose(1.0F, 0.16F, 0.30F, 0.12F, 0.34F, 0.34F, 0.16F,
                -1.75F, -0.78F, -0.46F, 2.10F, 0.34F, 0.28F, 0.14F, "handedness",
                0.12F, 0.14F, -0.96F, 0.08F, -0.06F, 0.05F, 0.05F, "-handedness",
                0.05F, 0.08F, 0.24F)));
        futures.add(savePose(output, strike1, slashPose(-1.0F, 0.16F, 0.30F, 0.12F, 0.34F, 0.34F, 0.16F,
                -1.75F, -0.78F, -0.46F, 2.10F, 0.34F, 0.28F, 0.14F, "-handedness",
                0.12F, 0.14F, -0.96F, 0.08F, -0.06F, 0.05F, 0.05F, "handedness",
                0.05F, 0.08F, 0.24F)));
        futures.add(savePose(output, strike2, verticalCutPose(1.0F, -2.15F, -0.95F, -0.60F, 2.55F, 0.12F, 0.10F, "handedness",
                -1.00F, 0.08F, -0.05F, 0.05F, 0.04F, "-handedness", 0.06F, 0.08F)));
        futures.add(savePose(output, strike3, slashPose(1.0F, 0.12F, 0.26F, 0.14F, 0.28F, 0.30F, 0.18F,
                -1.62F, -0.70F, -0.36F, 1.92F, 0.28F, 0.24F, 0.14F, "-handedness",
                0.10F, 0.12F, -0.90F, 0.08F, -0.05F, 0.05F, 0.05F, "handedness",
                0.04F, 0.08F, 0.22F)));
        futures.add(savePose(output, strike4, thrustPose(1.0F, 0.04F, 0.10F, 0.12F, -1.30F, -0.40F, -1.10F, 1.65F,
                0.04F, 0.06F, "handedness", -0.70F, 0.04F, 0.06F, 0.02F, "-handedness", 0.02F, 0.04F)));

        futures.add(saveProfile(output, MeleeWeaponTypes.LONGSWORD_TYPE.getName(),
                weaponProfile(MeleeWeaponTypes.LONGSWORD_TYPE.getName(), List.of(windup0, windup1), List.of(
                        new StrikeDef(strike0, 0.42F),
                        new StrikeDef(strike1, 0.42F),
                        new StrikeDef(strike2, 0.48F),
                        new StrikeDef(strike3, 0.40F),
                        new StrikeDef(strike4, 0.56F)
                ))));
    }

    private void registerKatana(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation windup0 = MKWeapons.id("katana_windup_0");
        ResourceLocation windup1 = MKWeapons.id("katana_windup_1");
        ResourceLocation strike0 = MKWeapons.id("katana_strike_0");
        ResourceLocation strike1 = MKWeapons.id("katana_strike_1");
        ResourceLocation strike2 = MKWeapons.id("katana_strike_2");
        ResourceLocation strike3 = MKWeapons.id("katana_strike_3");
        ResourceLocation strike4 = MKWeapons.id("katana_strike_4");

        futures.add(savePose(output, windup0, weaponWindup(-2.60F, 0.55F, -1.20F, 0.18F, 0.08F, -0.10F, 0.10F, 0.0F, -0.02F)));
        futures.add(savePose(output, windup1, weaponWindup(-2.40F, -0.45F, -1.15F, 0.25F, -0.06F, -0.08F, 0.08F, 0.0F, 0.0F)));
        futures.add(savePose(output, strike0, drawCutPose(1.0F, -2.05F, -1.05F, -0.55F, 2.65F, 0.12F, 0.10F, "handedness",
                -1.10F, 0.06F, -0.04F, 0.04F, 0.03F, "-handedness", 0.06F, 0.08F)));
        futures.add(savePose(output, strike1, drawCutPose(-1.0F, -2.00F, -1.00F, -0.48F, 2.50F, 0.12F, 0.10F, "-handedness",
                -1.05F, 0.06F, -0.04F, 0.04F, 0.03F, "handedness", 0.06F, 0.08F)));
        futures.add(savePose(output, strike2, verticalCutPose(1.0F, -2.30F, -1.10F, -0.60F, 2.80F, 0.08F, 0.08F, "handedness",
                -1.10F, 0.05F, -0.04F, 0.04F, 0.03F, "-handedness", 0.08F, 0.10F)));
        futures.add(savePose(output, strike3, drawCutPose(-1.0F, -1.90F, -0.95F, -0.46F, 2.35F, 0.10F, 0.08F, "handedness",
                -1.00F, 0.05F, -0.04F, 0.04F, 0.03F, "-handedness", 0.05F, 0.08F)));
        futures.add(savePose(output, strike4, thrustPose(1.0F, 0.02F, 0.08F, 0.10F, -1.40F, -0.45F, -1.20F, 1.75F,
                0.03F, 0.04F, "handedness", -0.82F, 0.04F, 0.05F, 0.02F, "-handedness", 0.03F, 0.05F)));

        futures.add(saveProfile(output, MeleeWeaponTypes.KATANA_TYPE.getName(),
                weaponProfile(MeleeWeaponTypes.KATANA_TYPE.getName(), List.of(windup0, windup1), List.of(
                        new StrikeDef(strike0, 0.44F),
                        new StrikeDef(strike1, 0.44F),
                        new StrikeDef(strike2, 0.48F),
                        new StrikeDef(strike3, 0.42F),
                        new StrikeDef(strike4, 0.52F)
                ))));
    }

    private void registerDagger(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation windup0 = MKWeapons.id("dagger_windup_0");
        ResourceLocation windup1 = MKWeapons.id("dagger_windup_1");
        ResourceLocation strike0 = MKWeapons.id("dagger_strike_0");
        ResourceLocation strike1 = MKWeapons.id("dagger_strike_1");
        ResourceLocation strike2 = MKWeapons.id("dagger_strike_2");

        futures.add(savePose(output, windup0, daggerWindup(0.88F, 0.46F, -0.44F, -0.06F, 0.08F, 0.04F, -0.02F, 0.10F, 0.03F)));
        futures.add(savePose(output, windup1, daggerWindup(0.78F, -0.40F, -0.48F, 0.04F, -0.06F, 0.04F, -0.01F, -0.10F, -0.03F)));
        futures.add(savePose(output, strike0, underhandStabPose(1.0F, 0.06F, 0.16F, 0.08F,
                0.16F, 0.18F, 0.10F, 0.60F, 0.55F, -1.70F, -0.65F,
                0.22F, 0.18F, "handedness", -0.95F, 0.10F, 0.10F, 0.06F,
                "-handedness", -0.03F, 0.03F, 0.14F)));
        futures.add(savePose(output, strike1, underhandStabPose(-1.0F, 0.08F, 0.18F, 0.10F,
                0.22F, 0.20F, 0.12F, 0.70F, 0.60F, -1.85F, -0.70F,
                0.24F, 0.20F, "-handedness", -1.00F, 0.12F, 0.12F, 0.06F,
                "handedness", -0.02F, 0.04F, 0.16F)));
        futures.add(savePose(output, strike2, underhandStabPose(1.0F, 0.10F, 0.22F, 0.12F,
                0.28F, 0.22F, 0.14F, 0.78F, 0.70F, -2.00F, -0.82F,
                0.28F, 0.24F, "handedness", -1.05F, 0.14F, 0.12F, 0.08F,
                "-handedness", -0.01F, 0.05F, 0.18F)));

        futures.add(saveProfile(output, MeleeWeaponTypes.DAGGER_TYPE.getName(),
                weaponProfile(MeleeWeaponTypes.DAGGER_TYPE.getName(), List.of(windup0, windup1), List.of(
                        new StrikeDef(strike0, 0.50F),
                        new StrikeDef(strike1, 0.34F),
                        new StrikeDef(strike2, 0.56F)
                ))));
    }

    private void registerSpear(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation windup0 = MKWeapons.id("spear_windup_0");
        ResourceLocation windup1 = MKWeapons.id("spear_windup_1");
        ResourceLocation strike0 = MKWeapons.id("spear_strike_0");
        ResourceLocation strike1 = MKWeapons.id("spear_strike_1");
        ResourceLocation strike2 = MKWeapons.id("spear_strike_2");

        futures.add(savePose(output, windup0, spearWindup(0.82F, 0.34F, -0.96F, -0.26F, 0.08F, 0.04F, -0.02F, 0.06F)));
        futures.add(savePose(output, windup1, spearWindup(0.90F, -0.30F, -1.02F, 0.22F, -0.06F, 0.04F, -0.01F, -0.06F)));
        futures.add(savePose(output, strike0, underhandPolearmThrustPose(1.0F, 0.10F, 0.18F, 0.12F,
                0.20F, 0.18F, 0.12F, 0.72F, 0.58F, -1.95F, -0.78F,
                0.24F, 0.20F, -1.45F, 0.14F, 0.10F, 0.08F,
                "-handedness", -0.04F, 0.04F, 0.18F)));
        futures.add(savePose(output, strike1, underhandPolearmThrustPose(-1.0F, 0.12F, 0.20F, 0.14F,
                0.24F, 0.20F, 0.14F, 0.82F, 0.62F, -2.10F, -0.86F,
                0.28F, 0.22F, -1.55F, 0.16F, 0.12F, 0.08F,
                "handedness", -0.03F, 0.05F, 0.20F)));
        futures.add(savePose(output, strike2, overheadPolearmPose(1.0F, -2.10F, -0.85F, -0.45F, 2.25F,
                -1.15F, 0.08F, -0.06F, 0.04F, 0.03F, 0.08F, 0.10F, 0.08F)));

        futures.add(saveProfile(output, MeleeWeaponTypes.SPEAR_TYPE.getName(),
                weaponProfile(MeleeWeaponTypes.SPEAR_TYPE.getName(), List.of(windup0, windup1), List.of(
                        new StrikeDef(strike0, 0.66F),
                        new StrikeDef(strike1, 0.72F),
                        new StrikeDef(strike2, 0.58F)
                ))));
    }

    private void registerStaff(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation windup0 = MKWeapons.id("staff_windup_0");
        ResourceLocation windup1 = MKWeapons.id("staff_windup_1");
        ResourceLocation strike0 = MKWeapons.id("staff_strike_0");
        ResourceLocation strike1 = MKWeapons.id("staff_strike_1");
        ResourceLocation strike2 = MKWeapons.id("staff_strike_2");
        ResourceLocation strike3 = MKWeapons.id("staff_strike_3");
        ResourceLocation strike4 = MKWeapons.id("staff_strike_4");

        futures.add(savePose(output, windup0, polearmWindup(-2.20F, 0.70F, -1.60F, -0.60F, 0.16F, -0.10F, 0.06F)));
        futures.add(savePose(output, windup1, polearmWindup(-2.20F, -0.70F, -1.60F, 0.60F, -0.16F, -0.10F, 0.06F)));
        futures.add(savePose(output, strike0, spinSlashPose(1.0F, 0.18F, 0.28F, 0.12F, 0.30F, 0.30F, 0.14F,
                -1.75F, -0.80F, -0.45F, 2.05F, 0.28F, 0.24F, 0.12F, "handedness",
                0.18F, 0.16F, -1.10F, 0.10F, -0.08F, 0.08F, 0.06F, "-handedness",
                0.05F, 0.08F, 0.24F)));
        futures.add(savePose(output, strike1, spinSlashPose(-1.0F, 0.18F, 0.30F, 0.12F, 0.32F, 0.32F, 0.14F,
                -1.75F, -0.80F, -0.45F, 2.05F, 0.28F, 0.24F, 0.12F, "-handedness",
                0.18F, 0.16F, -1.10F, 0.10F, -0.08F, 0.08F, 0.06F, "handedness",
                0.05F, 0.08F, 0.24F)));
        futures.add(savePose(output, strike2, overheadPolearmPose(1.0F, -2.20F, -0.90F, -0.50F, 2.35F,
                -1.20F, 0.10F, -0.08F, 0.05F, 0.04F, 0.08F, 0.12F, 0.10F)));
        futures.add(savePose(output, strike3, spinSlashPose(1.0F, 0.08F, 0.24F, 0.10F, 0.28F, 0.28F, 0.12F,
                -1.60F, -0.75F, -0.40F, 1.90F, 0.22F, 0.20F, 0.10F, "-handedness",
                0.16F, 0.14F, -1.00F, 0.08F, -0.06F, 0.08F, 0.05F, "handedness",
                0.04F, 0.06F, 0.20F)));
        futures.add(savePose(output, strike4, lowSweepPose(-1.0F, 0.12F, 0.22F, 0.12F, 0.30F, 0.26F, 0.16F,
                -1.20F, -0.55F, -0.35F, 1.60F, 0.24F, 0.20F, 0.12F, "handedness",
                0.16F, 0.14F, -0.95F, 0.08F, -0.06F, 0.08F, 0.05F, "-handedness",
                0.02F, 0.04F, 0.20F)));

        futures.add(saveProfile(output, MeleeWeaponTypes.STAFF_TYPE.getName(),
                weaponProfile(MeleeWeaponTypes.STAFF_TYPE.getName(), List.of(windup0, windup1), List.of(
                        new StrikeDef(strike0, 0.48F),
                        new StrikeDef(strike1, 0.48F),
                        new StrikeDef(strike2, 0.52F),
                        new StrikeDef(strike3, 0.46F),
                        new StrikeDef(strike4, 0.42F)
                ))));
    }

    private void registerMace(CachedOutput output, List<CompletableFuture<?>> futures) {
        ResourceLocation windup0 = MKWeapons.id("mace_windup_0");
        ResourceLocation windup1 = MKWeapons.id("mace_windup_1");
        ResourceLocation strike0 = MKWeapons.id("mace_strike_0");
        ResourceLocation strike1 = MKWeapons.id("mace_strike_1");
        ResourceLocation strike2 = MKWeapons.id("mace_strike_2");

        futures.add(savePose(output, windup0, weaponWindup(-2.10F, 0.45F, -0.95F, 0.04F, 0.08F, -0.10F, 0.06F, 0.02F, -0.03F)));
        futures.add(savePose(output, windup1, weaponWindup(-2.20F, -0.28F, -0.98F, -0.04F, -0.06F, -0.10F, 0.05F, -0.02F, 0.03F)));
        futures.add(savePose(output, strike0, crushPose(1.0F, -2.25F, -0.95F, -0.55F, 2.50F,
                0.14F, 0.12F, "handedness", -0.95F, 0.06F, -0.05F, 0.04F, 0.03F, "-handedness", 0.06F, 0.10F)));
        futures.add(savePose(output, strike1, compactSlashPose(-1.0F, 0.08F, 0.16F, 0.10F, 0.18F, 0.16F, 0.12F,
                -1.70F, -0.70F, -0.40F, 1.95F, 0.16F, 0.14F, 0.10F, "-handedness",
                0.05F, 0.08F, -0.82F, 0.04F, -0.03F, 0.03F, 0.02F, "handedness",
                0.04F, 0.06F, 0.14F)));
        futures.add(savePose(output, strike2, crushPose(1.0F, -2.05F, -0.90F, -0.50F, 2.30F,
                0.10F, 0.10F, "handedness", -0.90F, 0.05F, -0.04F, 0.03F, 0.03F, "-handedness", 0.05F, 0.08F)));

        futures.add(saveProfile(output, MeleeWeaponTypes.MACE_TYPE.getName(),
                weaponProfile(MeleeWeaponTypes.MACE_TYPE.getName(), List.of(windup0, windup1), List.of(
                        new StrikeDef(strike0, 0.40F),
                        new StrikeDef(strike1, 0.34F),
                        new StrikeDef(strike2, 0.42F)
                ))));
    }

    private MeleeAnimationProfile weaponProfile(ResourceLocation id, List<ResourceLocation> windups, List<StrikeDef> strikesIn) {
        List<MeleeAnimationProfile.Strike> strikes = new ArrayList<>();
        for (StrikeDef strikeDef : strikesIn) {
            strikes.add(strike(strikeDef.pose(), strikeDef.lunge()));
        }
        return profile(MeleeAnimationManager.BIPED_FAMILY)
                .windups(windups)
                .strikes(strikes)
                .build(id);
    }

    private MeleeAnimationPose weaponWindup(float mainArmX, float mainArmY, float offArmX, float offArmY, float bodyY,
                                    float bodyX, float headX, float mainArmZ, float offArmZ) {
        PoseBuilder poseJson = pose(false, 1.0F);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("main_arm", "xRot", "set", "none", value(-0.25F, term("windup_sin", mainArmX))));
        poseChannels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY))));
        poseChannels.add(channel("main_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmZ))));
        poseChannels.add(channel("off_arm", "xRot", "set", "none", value(-0.20F, term("windup_sin", offArmX))));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", offArmY))));
        poseChannels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", offArmZ))));
        poseChannels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        poseChannels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY))));
        poseChannels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return poseJson.build();
    }

    private MeleeAnimationPose daggerWindup(float mainArmX, float mainArmY, float offArmX, float offArmY, float bodyY,
                                            float bodyX, float headX, float mainArmZ, float offArmZ) {
        PoseBuilder poseJson = pose(false, 1.0F);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("main_arm", "xRot", "set", "none", value(0.18F, term("windup_sin", mainArmX))));
        poseChannels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY))));
        poseChannels.add(channel("main_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmZ))));
        poseChannels.add(channel("off_arm", "xRot", "set", "none", value(-0.16F, term("windup_sin", offArmX))));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", offArmY))));
        poseChannels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", offArmZ))));
        poseChannels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        poseChannels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY))));
        poseChannels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return poseJson.build();
    }

    private MeleeAnimationPose polearmWindup(float mainArmX, float mainArmY, float offArmX, float offArmY,
                                     float bodyY, float bodyX, float headX) {
        PoseBuilder poseJson = pose(false, 1.0F);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("main_arm", "xRot", "set", "none", value(-0.18F, term("windup_sin", mainArmX))));
        poseChannels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY))));
        poseChannels.add(channel("off_arm", "xRot", "set", "none", value(-0.20F, term("windup_sin", offArmX))));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", offArmY))));
        poseChannels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.06F))));
        poseChannels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        poseChannels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY))));
        poseChannels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return poseJson.build();
    }

    private MeleeAnimationPose spearWindup(float mainArmX, float mainArmY, float offArmX, float offArmY,
                                           float bodyY, float bodyX, float headX, float mainArmZ) {
        PoseBuilder poseJson = pose(false, 1.0F);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("main_arm", "xRot", "set", "none", value(0.12F, term("windup_sin", mainArmX))));
        poseChannels.add(channel("main_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmY))));
        poseChannels.add(channel("main_arm", "zRot", "set", "handedness", value(0.0F, term("windup_sin", mainArmZ))));
        poseChannels.add(channel("off_arm", "xRot", "set", "none", value(-0.12F, term("windup_sin", offArmX))));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness", value(0.0F, term("windup_sin", offArmY))));
        poseChannels.add(channel("off_arm", "zRot", "set", "-handedness", value(0.0F, term("windup_sin", 0.04F))));
        poseChannels.add(channel("body", "xRot", "add", "none", value(0.0F, term("windup_sin", bodyX))));
        poseChannels.add(channel("body", "yRot", "set", "handedness", value(0.0F, term("windup_sin", bodyY))));
        poseChannels.add(channel("head", "xRot", "add", "none", value(0.0F, term("windup_sin", headX))));
        return poseJson.build();
    }

    private MeleeAnimationPose slashPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                 float mainYawBase, float mainYawSwing, float mainYawFollow,
                                 float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                 float mainRollBase, float mainRollSwing, float mainRollFollow, String mainRollSign,
                                 float offYawBase, float offYawSwing,
                                 float offPitchBase, float offPitchSwing, float offPitchImpact,
                                 float offRollBase, float offRollSwing, String offRollSign,
                                 float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        PoseBuilder poseJson = pose(true, swingDirection);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("body", "yRot", "set", "-handedness*swingDirection",
                swingValue(bodyBase, bodySwing, 0.0F, bodyFollow)));
        poseChannels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection",
                swingValue(mainYawBase, mainYawSwing, 0.0F, mainYawFollow)));
        poseChannels.add(channel("main_arm", "xRot", "set", "none",
                swingValue(mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow)));
        poseChannels.add(channel("main_arm", "zRot", "set", mainRollSign,
                swingValue(mainRollBase, mainRollSwing, 0.0F, mainRollFollow)));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection",
                swingValue(offYawBase, offYawSwing, 0.0F, 0.0F)));
        poseChannels.add(channel("off_arm", "xRot", "set", "none",
                swingValue(offPitchBase, offPitchSwing, offPitchImpact, 0.0F)));
        poseChannels.add(channel("off_arm", "zRot", "set", offRollSign,
                swingValue(offRollBase, offRollSwing, 0.0F, 0.0F)));
        poseChannels.add(channel("head", "xRot", "add", "none",
                swingValue(0.0F, headPitchSwing, 0.0F, headPitchFollow)));
        poseChannels.add(channel("head", "yRot", "add", "-handedness*swingDirection",
                swingValue(bodyBase * headYawFollowScale, bodySwing * headYawFollowScale, 0.0F, bodyFollow * headYawFollowScale)));
        return poseJson.build();
    }

    private MeleeAnimationPose compactSlashPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                        float mainYawBase, float mainYawSwing, float mainYawFollow,
                                        float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                        float mainRollBase, float mainRollSwing, float mainRollFollow, String mainRollSign,
                                        float offYawBase, float offYawSwing,
                                        float offPitchBase, float offPitchSwing, float offPitchImpact,
                                        float offRollBase, float offRollSwing, String offRollSign,
                                        float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        return slashPose(swingDirection, bodyBase, bodySwing, bodyFollow,
                mainYawBase, mainYawSwing, mainYawFollow, mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, mainRollFollow, mainRollSign,
                offYawBase, offYawSwing, offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSwing, offRollSign,
                headPitchSwing, headPitchFollow, headYawFollowScale);
    }

    private MeleeAnimationPose drawCutPose(float swingDirection, float mainPitchBase, float mainPitchSwing, float mainPitchImpact,
                                   float mainPitchFollow, float mainRollBase, float mainRollSwing, String mainRollSign,
                                   float offPitchBase, float offPitchSwing, float offPitchImpact,
                                   float offRollBase, float offRollSwing, String offRollSign,
                                   float headPitchSwing, float headPitchFollow) {
        return slashPose(swingDirection, 0.08F, 0.12F, 0.08F, 0.12F, 0.14F, 0.08F,
                mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, 0.08F, mainRollSign,
                0.04F, 0.06F, offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSwing, offRollSign,
                headPitchSwing, headPitchFollow, 0.14F);
    }

    private MeleeAnimationPose verticalCutPose(float swingDirection, float mainPitchBase, float mainPitchSwing, float mainPitchImpact,
                                       float mainPitchFollow, float mainRollBase, float mainRollSwing, String mainRollSign,
                                       float offPitchBase, float offPitchSwing, float offPitchImpact,
                                       float offRollBase, float offRollSwing, String offRollSign,
                                       float headPitchSwing, float headPitchFollow) {
        return slashPose(swingDirection, 0.04F, 0.08F, 0.12F, 0.08F, 0.10F, 0.12F,
                mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, 0.08F, mainRollSign,
                0.02F, 0.05F, offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSwing, offRollSign,
                headPitchSwing, headPitchFollow, 0.12F);
    }

    private MeleeAnimationPose spinSlashPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                     float mainYawBase, float mainYawSwing, float mainYawFollow,
                                     float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                     float mainRollBase, float mainRollSwing, float mainRollFollow, String mainRollSign,
                                     float offYawBase, float offYawSwing,
                                     float offPitchBase, float offPitchSwing, float offPitchImpact,
                                     float offRollBase, float offRollSwing, String offRollSign,
                                     float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        return slashPose(swingDirection, bodyBase, bodySwing, bodyFollow,
                mainYawBase, mainYawSwing, mainYawFollow, mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, mainRollFollow, mainRollSign,
                offYawBase, offYawSwing, offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSwing, offRollSign,
                headPitchSwing, headPitchFollow, headYawFollowScale);
    }

    private MeleeAnimationPose lowSweepPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                    float mainYawBase, float mainYawSwing, float mainYawFollow,
                                    float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                    float mainRollBase, float mainRollSwing, float mainRollFollow, String mainRollSign,
                                    float offYawBase, float offYawSwing,
                                    float offPitchBase, float offPitchSwing, float offPitchImpact,
                                    float offRollBase, float offRollSwing, String offRollSign,
                                    float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        return slashPose(swingDirection, bodyBase, bodySwing, bodyFollow,
                mainYawBase, mainYawSwing, mainYawFollow, mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, mainRollFollow, mainRollSign,
                offYawBase, offYawSwing, offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSwing, offRollSign,
                headPitchSwing, headPitchFollow, headYawFollowScale);
    }

    private MeleeAnimationPose thrustPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                  float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                  float mainRollBase, float mainRollSwing, String mainRollSign,
                                  float offPitchBase, float offPitchSwing, float offPitchImpact,
                                  float offRollBase, String offRollSign,
                                  float headPitchSwing, float headPitchFollow) {
        PoseBuilder poseJson = pose(true, swingDirection);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("body", "yRot", "set", "-handedness*swingDirection",
                swingValue(bodyBase, bodySwing, 0.0F, bodyFollow)));
        poseChannels.add(channel("body", "xRot", "add", "none", swingValue(0.0F, -0.04F, 0.0F, -0.05F)));
        poseChannels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection",
                swingValue(0.02F, 0.04F, 0.0F, 0.04F)));
        poseChannels.add(channel("main_arm", "xRot", "set", "none",
                swingValue(mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow)));
        poseChannels.add(channel("main_arm", "zRot", "set", mainRollSign,
                swingValue(mainRollBase, mainRollSwing, 0.0F, 0.04F)));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection",
                swingValue(0.0F, 0.04F, 0.0F, 0.0F)));
        poseChannels.add(channel("off_arm", "xRot", "set", "none",
                swingValue(offPitchBase, offPitchSwing, offPitchImpact, 0.0F)));
        poseChannels.add(channel("off_arm", "zRot", "set", offRollSign,
                swingValue(offRollBase, 0.02F, 0.0F, 0.0F)));
        poseChannels.add(channel("head", "xRot", "add", "none",
                swingValue(0.0F, headPitchSwing, 0.0F, headPitchFollow)));
        return poseJson.build();
    }

    private MeleeAnimationPose stabPose(float swingDirection, float mainPitchBase, float mainPitchSwing, float mainPitchImpact,
                                float mainPitchFollow, float mainRollBase, float mainRollSwing, String mainRollSign,
                                float offPitchBase, float offPitchSwing, float offPitchImpact,
                                float offRollBase, String offRollSign,
                                float headPitchSwing, float headPitchFollow) {
        return thrustPose(swingDirection, 0.02F, 0.05F, 0.06F,
                mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, mainRollSign,
                offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSign,
                headPitchSwing, headPitchFollow);
    }

    private MeleeAnimationPose underhandStabPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                         float mainYawBase, float mainYawSwing, float mainYawFollow,
                                         float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                         float mainRollBase, float mainRollSwing, String mainRollSign,
                                         float offPitchBase, float offPitchSwing, float offPitchImpact, float offRollBase,
                                         String offRollSign, float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        PoseBuilder poseJson = pose(true, swingDirection);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("body", "yRot", "set", "-handedness*swingDirection",
                swingValue(bodyBase, bodySwing, 0.0F, bodyFollow)));
        poseChannels.add(channel("body", "xRot", "add", "none",
                swingValue(0.02F, 0.06F, 0.0F, -0.04F)));
        poseChannels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection",
                swingValue(mainYawBase, mainYawSwing, 0.0F, mainYawFollow)));
        poseChannels.add(channel("main_arm", "xRot", "set", "none",
                swingValue(mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow)));
        poseChannels.add(channel("main_arm", "zRot", "set", mainRollSign,
                swingValue(mainRollBase, mainRollSwing, 0.0F, 0.10F)));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection",
                swingValue(0.04F, 0.08F, 0.0F, 0.0F)));
        poseChannels.add(channel("off_arm", "xRot", "set", "none",
                swingValue(offPitchBase, offPitchSwing, offPitchImpact, 0.0F)));
        poseChannels.add(channel("off_arm", "zRot", "set", offRollSign,
                swingValue(offRollBase, 0.04F, 0.0F, 0.0F)));
        poseChannels.add(channel("head", "xRot", "add", "none",
                swingValue(0.0F, headPitchSwing, 0.0F, headPitchFollow)));
        poseChannels.add(channel("head", "yRot", "add", "-handedness*swingDirection",
                swingValue(bodyBase * headYawFollowScale, bodySwing * headYawFollowScale, 0.0F, bodyFollow * headYawFollowScale)));
        return poseJson.build();
    }

    private MeleeAnimationPose polearmThrustPose(float swingDirection, float mainPitchBase, float mainPitchSwing, float mainPitchImpact,
                                         float mainPitchFollow, float offPitchBase, float bodySwing, float bodyFollow,
                                         float headPitchSwing, float mainRollSwing, float offRollSwing) {
        PoseBuilder poseJson = pose(true, swingDirection);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("body", "yRot", "set", "-handedness*swingDirection",
                swingValue(0.02F, bodySwing, 0.0F, bodyFollow)));
        poseChannels.add(channel("body", "xRot", "add", "none", swingValue(0.0F, -0.06F, 0.0F, -0.06F)));
        poseChannels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection",
                swingValue(0.0F, 0.05F, 0.0F, 0.04F)));
        poseChannels.add(channel("main_arm", "xRot", "set", "none",
                swingValue(mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow)));
        poseChannels.add(channel("main_arm", "zRot", "set", "handedness",
                swingValue(0.04F, mainRollSwing, 0.0F, 0.04F)));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection",
                swingValue(0.02F, 0.06F, 0.0F, 0.0F)));
        poseChannels.add(channel("off_arm", "xRot", "set", "none",
                swingValue(offPitchBase, 0.10F, -0.08F, 0.0F)));
        poseChannels.add(channel("off_arm", "zRot", "set", "-handedness",
                swingValue(0.05F, offRollSwing, 0.0F, 0.0F)));
        poseChannels.add(channel("head", "xRot", "add", "none",
                swingValue(0.0F, headPitchSwing, 0.0F, 0.05F)));
        return poseJson.build();
    }

    private MeleeAnimationPose underhandPolearmThrustPose(float swingDirection, float bodyBase, float bodySwing, float bodyFollow,
                                                  float mainYawBase, float mainYawSwing, float mainYawFollow,
                                                  float mainPitchBase, float mainPitchSwing, float mainPitchImpact, float mainPitchFollow,
                                                  float mainRollBase, float mainRollSwing,
                                                  float offPitchBase, float offPitchSwing, float offPitchImpact, float offRollBase,
                                                  String offRollSign, float headPitchSwing, float headPitchFollow, float headYawFollowScale) {
        PoseBuilder poseJson = pose(true, swingDirection);
        List<PoseChannel> poseChannels = poseJson.channels();
        poseChannels.add(channel("body", "yRot", "set", "-handedness*swingDirection",
                swingValue(bodyBase, bodySwing, 0.0F, bodyFollow)));
        poseChannels.add(channel("body", "xRot", "add", "none",
                swingValue(0.04F, 0.08F, 0.0F, -0.06F)));
        poseChannels.add(channel("main_arm", "yRot", "set", "-handedness*swingDirection",
                swingValue(mainYawBase, mainYawSwing, 0.0F, mainYawFollow)));
        poseChannels.add(channel("main_arm", "xRot", "set", "none",
                swingValue(mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow)));
        poseChannels.add(channel("main_arm", "zRot", "set", "handedness",
                swingValue(mainRollBase, mainRollSwing, 0.0F, 0.10F)));
        poseChannels.add(channel("off_arm", "yRot", "set", "handedness*swingDirection",
                swingValue(0.10F, 0.10F, 0.0F, 0.0F)));
        poseChannels.add(channel("off_arm", "xRot", "set", "none",
                swingValue(offPitchBase, offPitchSwing, offPitchImpact, 0.0F)));
        poseChannels.add(channel("off_arm", "zRot", "set", offRollSign,
                swingValue(offRollBase, 0.05F, 0.0F, 0.0F)));
        poseChannels.add(channel("head", "xRot", "add", "none",
                swingValue(0.0F, headPitchSwing, 0.0F, headPitchFollow)));
        poseChannels.add(channel("head", "yRot", "add", "-handedness*swingDirection",
                swingValue(bodyBase * headYawFollowScale, bodySwing * headYawFollowScale, 0.0F, bodyFollow * headYawFollowScale)));
        return poseJson.build();
    }

    private MeleeAnimationPose overheadPolearmPose(float swingDirection, float mainPitchBase, float mainPitchSwing, float mainPitchImpact,
                                           float mainPitchFollow, float offPitchBase, float bodySwing, float bodyFollow,
                                           float headPitchSwing, float mainRollSwing, float offRollSwing,
                                           float mainYawFollow, float bodyBase) {
        return slashPose(swingDirection, bodyBase, bodySwing, bodyFollow, 0.10F, 0.10F, mainYawFollow,
                mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                0.10F, mainRollSwing, 0.08F, "handedness",
                0.05F, 0.06F, offPitchBase, 0.08F, -0.05F, 0.05F, offRollSwing, "-handedness",
                headPitchSwing, 0.10F, 0.16F);
    }

    private MeleeAnimationPose crushPose(float swingDirection, float mainPitchBase, float mainPitchSwing, float mainPitchImpact,
                                 float mainPitchFollow, float mainRollBase, float mainRollSwing, String mainRollSign,
                                 float offPitchBase, float offPitchSwing, float offPitchImpact,
                                 float offRollBase, float offRollSwing, String offRollSign,
                                 float headPitchSwing, float headPitchFollow) {
        return slashPose(swingDirection, 0.10F, 0.16F, 0.12F, 0.12F, 0.14F, 0.10F,
                mainPitchBase, mainPitchSwing, mainPitchImpact, mainPitchFollow,
                mainRollBase, mainRollSwing, 0.08F, mainRollSign,
                0.04F, 0.06F, offPitchBase, offPitchSwing, offPitchImpact, offRollBase, offRollSwing, offRollSign,
                headPitchSwing, headPitchFollow, 0.16F);
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

    private record StrikeDef(ResourceLocation pose, float lunge) {
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKWeapons Melee Animation Assets";
    }
}
