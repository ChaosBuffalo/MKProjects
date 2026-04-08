package com.chaosbuffalo.mkcore.client.rendering.animations.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeleeAnimationManager {
    public static final ResourceLocation BIPED_FAMILY = MKCore.id("biped");
    public static final ResourceLocation BIPED_DEFAULT = MKCore.id("biped_default");
    public static final ResourceLocation MISSING = MKCore.id("missing");
    public static final String PROFILE_FOLDER = "melee_attack_animations";
    public static final String POSE_FOLDER = "melee_animation_poses";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, MeleeAnimationProfile> PROFILES = new HashMap<>();
    private static final Map<ResourceLocation, MeleeAnimationPose> POSES = new HashMap<>();
    private static final Map<ResourceLocation, MeleeAnimationFamilyAdapter> FAMILY_ADAPTERS = new HashMap<>();
    private static final List<MeleeAnimationProfileResolver> RESOLVERS = new CopyOnWriteArrayList<>();
    private static final MeleeAnimationProfile MISSING_PROFILE = new MeleeAnimationProfile(MISSING, MKCore.id("any"), List.of(),
            List.of(), MeleeAnimationProfile.Selection.CYCLE);
    private static final Set<ResourceLocation> LOGGED_MISSING_PROFILES = new HashSet<>();

    static {
        registerFamilyAdapter(BIPED_FAMILY, MeleeAnimationFamilyAdapter.BIPED);
    }

    public static void registerResolver(MeleeAnimationProfileResolver resolver) {
        RESOLVERS.add(resolver);
    }

    public static void registerFamilyAdapter(ResourceLocation family, MeleeAnimationFamilyAdapter adapter) {
        FAMILY_ADAPTERS.put(family, adapter);
    }

    public static MeleeAnimationFamilyAdapter getFamilyAdapter(ResourceLocation family) {
        return FAMILY_ADAPTERS.getOrDefault(family, MeleeAnimationFamilyAdapter.DEFAULT);
    }

    public static MeleeAnimationProfile resolveProfile(LivingEntity entity, ResourceLocation defaultProfile) {
        return resolveProfile(entity, defaultProfile, null);
    }

    public static MeleeAnimationProfile resolveProfile(LivingEntity entity, ResourceLocation defaultProfile,
                                                       @Nullable ResourceLocation requiredFamily) {
        MeleeAnimationProfile resolved = resolveProfileOverride(entity, requiredFamily);
        if (resolved != null) {
            return resolved;
        }
        MeleeAnimationProfile profile = getProfile(defaultProfile);
        if (profile != null && isFamilyMatch(profile, requiredFamily)) {
            return profile;
        }
        logMissingProfile(defaultProfile);
        return MISSING_PROFILE;
    }

    @Nullable
    public static MeleeAnimationProfile resolveProfileOverride(LivingEntity entity,
                                                               @Nullable ResourceLocation requiredFamily) {
        for (MeleeAnimationProfileResolver resolver : RESOLVERS) {
            ResourceLocation id = resolver.resolve(entity);
            MeleeAnimationProfile profile = id == null ? null : getProfile(id);
            if (profile != null && isFamilyMatch(profile, requiredFamily)) {
                return profile;
            }
        }
        return null;
    }

    private static boolean isFamilyMatch(MeleeAnimationProfile profile, @Nullable ResourceLocation requiredFamily) {
        return requiredFamily == null || profile.family().equals(requiredFamily);
    }

    @Nullable
    public static MeleeAnimationProfile getProfile(ResourceLocation id) {
        return PROFILES.get(id);
    }

    @Nullable
    public static MeleeAnimationPose getPose(ResourceLocation id) {
        return POSES.get(id);
    }

    @Nullable
    public static MeleeAnimationProfile.Strike resolveStrike(LivingEntity entity, ResourceLocation defaultProfile,
                                                             @Nullable ResourceLocation requiredFamily, int strikeIndex) {
        return resolveProfile(entity, defaultProfile, requiredFamily).getStrike(strikeIndex);
    }

    @Nullable
    public static MeleeAnimationPose resolveStrikePose(LivingEntity entity, ResourceLocation defaultProfile,
                                                       @Nullable ResourceLocation requiredFamily, int strikeIndex) {
        MeleeAnimationProfile.Strike strike = resolveStrike(entity, defaultProfile, requiredFamily, strikeIndex);
        return strike == null ? null : getPose(strike.pose());
    }

    @Nullable
    public static MeleeAnimationPose resolveWindupPose(LivingEntity entity, ResourceLocation defaultProfile,
                                                       @Nullable ResourceLocation requiredFamily, int windupIndex) {
        MeleeAnimationProfile profile = resolveProfile(entity, defaultProfile, requiredFamily);
        ResourceLocation windup = profile.getWindup(windupIndex);
        return windup == null ? null : getPose(windup);
    }

    public static boolean applyStrikePose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation defaultProfile,
                                          ResourceLocation requiredFamily, int strikeIndex, ModelPoseAnimator.Context context) {
        MeleeAnimationProfile profile = resolveProfile(entity, defaultProfile, requiredFamily);
        return applyStrikePose(skeleton, profile, strikeIndex, context);
    }

    public static boolean applyResolvedStrikePose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation requiredFamily,
                                                  int strikeIndex, ModelPoseAnimator.Context context) {
        MeleeAnimationProfile profile = resolveProfileOverride(entity, requiredFamily);
        return profile != null && applyStrikePose(skeleton, profile, strikeIndex, context);
    }

    private static boolean applyStrikePose(MCSkeleton skeleton, MeleeAnimationProfile profile,
                                           int strikeIndex, ModelPoseAnimator.Context context) {
        MeleeAnimationProfile.Strike strike = profile.getStrike(strikeIndex);
        MeleeAnimationPose pose = strike == null ? null : getPose(strike.pose());
        if (pose == null) {
            return false;
        }
        ModelPoseAnimator.apply(skeleton, profile.family(), pose, context);
        return true;
    }

    public static boolean applyWindupPose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation defaultProfile,
                                          ResourceLocation requiredFamily, int windupIndex, ModelPoseAnimator.Context context) {
        MeleeAnimationProfile profile = resolveProfile(entity, defaultProfile, requiredFamily);
        return applyWindupPose(skeleton, profile, windupIndex, context);
    }

    public static boolean applyResolvedWindupPose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation requiredFamily,
                                                  int windupIndex, ModelPoseAnimator.Context context) {
        MeleeAnimationProfile profile = resolveProfileOverride(entity, requiredFamily);
        return profile != null && applyWindupPose(skeleton, profile, windupIndex, context);
    }

    private static boolean applyWindupPose(MCSkeleton skeleton, MeleeAnimationProfile profile,
                                           int windupIndex, ModelPoseAnimator.Context context) {
        ResourceLocation windup = profile.getWindup(windupIndex);
        MeleeAnimationPose pose = windup == null ? null : getPose(windup);
        if (pose == null) {
            return false;
        }
        ModelPoseAnimator.apply(skeleton, profile.family(), pose, context);
        return true;
    }

    public static float resolveStrikeLunge(LivingEntity entity, ResourceLocation defaultProfile,
                                           ResourceLocation requiredFamily, int strikeIndex) {
        MeleeAnimationProfile.Strike strike = resolveStrike(entity, defaultProfile, requiredFamily, strikeIndex);
        return strike == null ? 0.0F : strike.lunge();
    }

    private static void logMissingProfile(ResourceLocation defaultProfile) {
        if (LOGGED_MISSING_PROFILES.add(defaultProfile)) {
            MKCore.LOGGER.error("Missing required melee animation profile {}", defaultProfile);
        }
    }

    public static class ProfileReloadListener extends SimpleJsonResourceReloadListener {
        public ProfileReloadListener() {
            super(GSON, PROFILE_FOLDER);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
            PROFILES.clear();
            for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
                try {
                    PROFILES.put(entry.getKey(), parseProfile(entry.getKey(), entry.getValue()));
                } catch (Exception e) {
                    MKCore.LOGGER.error("Failed to load melee animation profile {}", entry.getKey(), e);
                }
            }
            MKCore.LOGGER.info("Loaded {} melee animation profiles", PROFILES.size());
            validateLoadedData();
        }
    }

    public static class PoseReloadListener extends SimpleJsonResourceReloadListener {
        public PoseReloadListener() {
            super(GSON, POSE_FOLDER);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
            POSES.clear();
            for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
                try {
                    POSES.put(entry.getKey(), parsePose(entry.getValue()));
                } catch (Exception e) {
                    MKCore.LOGGER.error("Failed to load melee animation pose {}", entry.getKey(), e);
                }
            }
            MKCore.LOGGER.info("Loaded {} melee animation poses", POSES.size());
            validateLoadedData();
        }
    }

    private static MeleeAnimationProfile parseProfile(ResourceLocation id, JsonElement json) {
        return MeleeAnimationProfile.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().withId(id);
    }

    private static MeleeAnimationPose parsePose(JsonElement json) {
        return MeleeAnimationPose.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    private static void validateLoadedData() {
        if (PROFILES.isEmpty() || POSES.isEmpty()) {
            return;
        }
        for (MeleeAnimationProfile profile : PROFILES.values()) {
            for (ResourceLocation windup : profile.windups()) {
                if (!POSES.containsKey(windup)) {
                    MKCore.LOGGER.error("Melee animation profile {} references missing windup pose {}",
                            profile.id(), windup);
                }
            }
            if (profile.strikes().isEmpty()) {
                MKCore.LOGGER.error("Melee animation profile {} has no strike poses", profile.id());
            }
            for (MeleeAnimationProfile.Strike strike : profile.strikes()) {
                if (!POSES.containsKey(strike.pose())) {
                    MKCore.LOGGER.error("Melee animation profile {} references missing strike pose {}",
                            profile.id(), strike.pose());
                }
            }
        }
    }
}
